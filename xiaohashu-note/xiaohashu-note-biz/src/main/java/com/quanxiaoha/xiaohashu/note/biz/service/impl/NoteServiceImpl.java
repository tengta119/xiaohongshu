package com.quanxiaoha.xiaohashu.note.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.DateUtils;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.kv.dto.rsp.FindNoteContentRspDTO;
import com.quanxiaoha.xiaohashu.note.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteLikeDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.TopicDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.enums.*;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.*;
import com.quanxiaoha.xiaohashu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.KeyValueRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.note.biz.service.NoteService;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author lbwxxc
 * @date 2025/6/1 16:03
 * @description:
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private TopicDOMapper topicDOMapper;
    @Resource
    UserRpcService userRpcService;
    @Resource(name = "taskExecutor")
    private Executor executor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;

    private final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public Response<?> publishNote(PublishNoteReqVO publishNoteReqVO) {
        // 笔记类型
        Integer type = publishNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        // 笔记内容是否为空，默认值为 true，即空
        Boolean isContentEmpty = true;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT:
                List<String> imgUrisList = publishNoteReqVO.getImgUris();
                Preconditions.checkArgument(CollectionUtils.isNotEmpty(imgUrisList), "笔记图片不能为空");
                imgUris = StringUtils.join(imgUrisList, ",");
                break;
            case VIDEO:
                videoUri = publishNoteReqVO.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // 笔记内容 UUID
        String contentUuid = null;
        // 笔记内容
        String content = publishNoteReqVO.getContent();
        if (StringUtils.isNotBlank(content)) {
            isContentEmpty = false;
            contentUuid = UUID.randomUUID().toString();
            boolean saveNoteContent = keyValueRpcService.saveNoteContent(contentUuid, content);
            if (!saveNoteContent) {
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }

        }

        Long topicId = publishNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
        }

        long creatorId = LoginUserContextHolder.getUserId();

        // 构建笔记 DO 对象
        NoteDO noteDO = NoteDO.builder()
                .id(Long.valueOf(distributedIdGeneratorRpcService.getSnowflakeId()))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUris(imgUris)
                .title(publishNoteReqVO.getTitle())
                .topicId(publishNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(type)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.NORMAL.getCode())
                .isTop(Boolean.FALSE)
                .videoUri(videoUri)
                .contentUuid(contentUuid)
                .build();

        try {
            noteDOMapper.insert(noteDO);
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);

            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        return Response.success(noteDO);
    }

    @Override
    public Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {

        Long noteId = findNoteDetailReqVO.getId();

        // 先从本地缓存中查询
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRspVO.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVO(LoginUserContextHolder.getUserId(), findNoteDetailRspVO);
            return Response.success(findNoteDetailRspVO);
        }

        // 从 Redis 缓存中获取
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRspVO.class);
            // 可见性校验
            if (Objects.nonNull(findNoteDetailRspVO)) {
                Integer visible = findNoteDetailRspVO.getVisible();
                checkNoteVisible(visible, noteId, findNoteDetailRspVO.getCreatorId());
            }
            log.info("==> 从 redis 中获取笔记的详细信息  noteDetailJson:{}", noteDetailJson);
            // 写入本地缓存
            LOCAL_CACHE.put(noteId,
                    Objects.isNull(findNoteDetailRspVO) ? "null" : JsonUtils.toJsonString(findNoteDetailRspVO));
            return Response.success(findNoteDetailRspVO);
        }

        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);
        String contentUuid = noteDO.getContentUuid();
        // RPC 远程调用笔记服务
        FindNoteContentRspDTO noteContent = keyValueRpcService.findNoteContent(contentUuid);
        if (Objects.isNull(noteContent)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }
        String content = noteContent.getContent();
        log.info("==> RPC 远程调用笔记服务获取笔记详细内容：{}", noteContent);
        Long creatorId = noteDO.getCreatorId();
        // RPC 远程用户服务调用
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(creatorId);

        Integer visible = noteDO.getVisible();
        // 当前登录的用户
        Long userId = LoginUserContextHolder.getUserId();
        checkNoteVisible(visible, userId, creatorId);

        // 笔记类型
        Integer noteType = noteDO.getType();
        // 图文笔记图片链接(字符串)
        String imgUrisStr = noteDO.getImgUris();
        // 图文笔记图片链接(集合)
        List<String> imgUris = null;
        // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
        if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                && StringUtils.isNotBlank(imgUrisStr)) {
            imgUris = List.of(imgUrisStr.split(","));
        }

        // 构建返参 VO 实体类
        FindNoteDetailRspVO findNoteDetailRspVO = FindNoteDetailRspVO.builder()
                .id(noteDO.getId())
                .type(noteDO.getType())
                .title(noteDO.getTitle())
                .content(content)
                .imgUris(imgUris)
                .topicId(noteDO.getTopicId())
                .topicName(noteDO.getTopicName())
                .creatorId(noteDO.getCreatorId())
                .creatorName(findUserByIdRspDTO.getNickName())
                .avatar(findUserByIdRspDTO.getAvatar())
                .videoUri(noteDO.getVideoUri())
                .updateTime(noteDO.getUpdateTime())
                .visible(noteDO.getVisible())
                .build();
        executor.execute(() -> {

            String jsonStringFindNoteDetailRspVO = JsonUtils.toJsonString(findNoteDetailRspVO);
            long expireSeconds = 60 + RandomUtil.randomInt(60);
            redisTemplate.opsForValue().set(noteDetailRedisKey, jsonStringFindNoteDetailRspVO,  expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(findNoteDetailRspVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {
        // 笔记 ID
        Long noteId = updateNoteReqVO.getId();
        // 笔记类型
        Integer type = updateNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = updateNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = updateNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 话题
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);

            // 判断一下提交的话题, 是否是真实存在的
            if (StringUtils.isBlank(topicName)) throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 更新笔记元数据表 t_note
        String content = updateNoteReqVO.getContent();
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .title(updateNoteReqVO.getTitle())
                .topicId(updateNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUri(videoUri)
                .build();

        noteDOMapper.updateByPrimaryKey(noteDO);

        Message<String> message = MessageBuilder.withPayload(String.valueOf(noteId)).build();
        // 异步发送 MQ, 提升接口的响应时间
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message, new SendCallback() {
            @Override
            public void onException(Throwable throwable) {
                log.error("## 延时删除 Redis 笔记缓存消息发送失败...", throwable);
            }

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("## 延时删除 Redis 笔记缓存消息发送成功...");
            }

        }, 3000, 1);

        // 删除本地缓存
        //LOCAL_CACHE.invalidate(noteId);
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        // 笔记内容更新
        // 查询此篇笔记内容对应的 UUID
        NoteDO noteDO1 = noteDOMapper.selectByPrimaryKey(noteId);
        String contentUuid = noteDO1.getContentUuid();

        // 笔记内容是否更新成功
        boolean isUpdateContentSuccess = false;
        if (StringUtils.isBlank(content)) {
            // 若笔记内容为空，则删除 K-V 存储
            isUpdateContentSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
        } else {
            // 若将无内容的笔记，更新为了有内容的笔记，需要重新生成 UUID
            contentUuid = StringUtils.isBlank(contentUuid) ? UUID.randomUUID().toString() : contentUuid;
            // 调用 K-V 更新短文本
            isUpdateContentSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);
        }

        // 如果更新失败，抛出业务异常，回滚事务
        if (!isUpdateContentSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }


        return Response.success();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO) {

        Long noteId = deleteNoteReqVO.getId();
        NoteDO noteDO = new NoteDO();
        noteDO.setId(noteId);
        noteDO.setStatus(NoteStatusEnum.DELETED.getCode());
        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);
        // 影响的行数为 0 ，则表示该笔记不存在
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        String buildNoteDetailKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(buildNoteDetailKey);

        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();
    }

    @Override
    public Response<?> topNote(TopNoteReqVO topNoteReqVO) {
        Long noteId = topNoteReqVO.getId();
        Boolean isTop = topNoteReqVO.getIsTop();
        // 当前用户
        Long curUserId = LoginUserContextHolder.getUserId();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isTop(isTop)
                .creatorId(curUserId)
                .updateTime(LocalDateTime.now())
                .build();

        int count = noteDOMapper.updateIsTop(noteDO);
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        String buildNoteDetailKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(buildNoteDetailKey);

        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();
    }

    @Override
    public Response<?> likeNote(LikeNoteReqVO likeNoteReqVO) {

        //笔记 id
        Long noteId = likeNoteReqVO.getId();

        // 1. 校验被点赞的笔记是否存在
        checkNoteIsExist(noteId);

        // 2. 判断目标笔记，是否已经点赞过
        Long userId = LoginUserContextHolder.getUserId();
        String bloomUserNoteLikeListKey = RedisKeyConstants.buildBloomUserNoteLikeListKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_like_check.lua")));
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId);
        NoteLikeLuaResultEnum noteLikeLuaResultEnum = NoteLikeLuaResultEnum.valueOf(result);
        if (noteLikeLuaResultEnum == null) {
            throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
        }

        // 用户点赞列表 ZSet Key
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);
        switch (noteLikeLuaResultEnum) {
            case NOT_EXIST -> {
                long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
                // 从数据库中校验笔记是否被点赞，并异步初始化布隆过滤器，设置过期时间
                int count = noteLikeDOMapper.selectCountByUserIdAndNoteId(userId, noteId);
                // 目标笔记已经被点赞
                if (count > 0) {
                    // 异步初始化布隆过滤器
                    asynBatchAddNoteLike2BloomAndExpire(userId, expireSeconds, bloomUserNoteLikeListKey);
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }

                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_like_and_expire.lua")));
                script.setResultType(Long.class);
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId, expireSeconds);
            }

            case NOTE_LIKED -> {
                log.info("用户点赞在布隆过滤器中存在，进一步通过 zset 查询");
                // 校验 ZSet 列表中是否包含被点赞的笔记Id
                Double score = redisTemplate.opsForZSet().score(userNoteLikeZSetKey, noteId);
                if (Objects.nonNull(score)) {
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }
                // 若 Score 为空，则表示 ZSet 点赞列表中不存在，查询数据库校验
                int count = noteLikeDOMapper.selectCountByUserIdAndNoteId(userId, noteId);
                if (count > 0) {
                    asynInitUserNoteLikesZSet(userId, userNoteLikeZSetKey);
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }

            }

            default -> throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
        }

        // 3. 更新用户 ZSET 点赞列表
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_like_check_and_update_zset.lua")));
        script.setResultType(Long.class);
        LocalDateTime now = LocalDateTime.now();
        result = redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));

        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, NoteLikeLuaResultEnum.NOT_EXIST.getCode())) {
            log.info("====> 用户的点赞列表 zset 不存在");
            // 查询当前用户最新点赞的 100 篇笔记
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);

            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);

            DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
            // Lua 脚本路径
            script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
            // 返回值类型
            script2.setResultType(Long.class);

            // 若数据库中存在点赞记录，需要批量同步
            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                // 构建 Lua 参数
                Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireSeconds);

                redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);

                // 再次调用 note_like_check_and_update_zset.lua 脚本，将点赞的笔记添加到 zset 中
                redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
                log.info("====> 将用户的点赞过的笔记前100条同步到 redis 的 zset 中 {}", luaArgs);
            } else { // 若数据库中，无点赞过的笔记记录，则直接将当前点赞的笔记 ID 添加到 ZSet 中，随机过期时间
                List<Object> luaArgs = Lists.newArrayList();
                luaArgs.add(DateUtils.localDateTime2Timestamp(LocalDateTime.now())); // score ：点赞时间戳
                luaArgs.add(noteId); // 当前点赞的笔记 ID
                luaArgs.add(expireSeconds); // 随机过期时间

                redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs.toArray());
                log.info("用户之前未点赞过，只同步一条数据到 redis 的 zset {}", luaArgs);
            }
        }

        // 4. 发送 MQ, 将点赞数据落库

        return Response.success();
    }

    /**
     * 异步初始化用户点赞笔记 ZSet
     * @param userId
     * @param userNoteLikeZSetKey
     */
    private void asynInitUserNoteLikesZSet(Long userId, String userNoteLikeZSetKey) {
        executor.execute(() -> {
            // 判断用户笔记点赞 ZSET 是否存在
            boolean hasKey = redisTemplate.hasKey(userNoteLikeZSetKey);

            // 不存在，则重新初始化
            if (!hasKey) {
                // 查询当前用户最新点赞的 100 篇笔记
                List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);
                if (CollUtil.isNotEmpty(noteLikeDOS)) {
                    // 保底1天+随机秒数
                    long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
                    // 构建 Lua 参数
                    Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireSeconds);

                    DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                    // Lua 脚本路径
                    script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
                    // 返回值类型
                    script2.setResultType(Long.class);

                    redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);
                }
            }
        });
    }

    /**
     * 构建 Lua 脚本参数
     *
     * @param noteLikeDOS
     * @param expireSeconds
     * @return
     */
    private static Object[] buildNoteLikeZSetLuaArgs(List<NoteLikeDO> noteLikeDOS, long expireSeconds) {
        int argsLength = noteLikeDOS.size() * 2 + 1; // 每个笔记点赞关系有 2 个参数（score 和 value），最后再跟一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (NoteLikeDO noteLikeDO : noteLikeDOS) {
            luaArgs[i] = DateUtils.dateTime2Timestamp(noteLikeDO.getCreateTime()); // 点赞时间作为 score
            luaArgs[i + 1] = noteLikeDO.getNoteId();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    /**
     * 异步初始化布隆过滤器
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteLikeListKey
     */
    private void asynBatchAddNoteLike2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteLikeListKey) {
        executor.execute(() -> {
            try {
                List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);

                if (CollUtil.isNotEmpty(noteLikeDOS)) {
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_like_and_expire.lua")));
                    script.setResultType(Long.class);

                    // 构建 Lua 参数
                    List<Object> luaArgs = Lists.newArrayList();
                    noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getId()));
                    luaArgs.add(expireSeconds);

                    redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());
                }
            } catch (Exception e) {
                log.error("## 异步初始化布隆过滤器异常: ", e);
            }
        });
    }


    /**
     * 校验笔记是否存在
     * @param noteId
     */
    private void checkNoteIsExist(Long noteId) {
        String cacheIfPresent = LOCAL_CACHE.getIfPresent(noteId);
        // 解析 Json 字符串为 VO 对象
        FindNoteDetailRspVO findNoteDetailRspVO = null;
        if (Objects.nonNull(cacheIfPresent)) {
             findNoteDetailRspVO = JsonUtils.parseObject(cacheIfPresent, FindNoteDetailRspVO.class);
        }

        if (Objects.isNull(findNoteDetailRspVO)) {
            // 再从 Redis 中校验
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
            String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

            // 解析 Json 字符串为 VO 对象
            findNoteDetailRspVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRspVO.class);

            // 都不存在，再查询数据库校验是否存在
            if (Objects.isNull(findNoteDetailRspVO)) {
                int count = noteDOMapper.selectCountByNoteId(noteId);

                if (count == 0) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
                }

                // 若数据库中存在，异步同步一下缓存
                executor.execute(() -> {
                    FindNoteDetailReqVO findNoteDetailReqVO = FindNoteDetailReqVO.builder().id(noteId).build();
                    findNoteDetail(findNoteDetailReqVO);
                });

            }
        }
    }

    /**
     * 校验笔记的可见性
     * @param visible 是否可见
     * @param currUserId 当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long currUserId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(currUserId, creatorId)) { // 仅自己可见, 并且访问用户为笔记创建者
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     * @param userId
     * @param findNoteDetailRspVO
     */
    private void checkNoteVisibleFromVO(Long userId, FindNoteDetailRspVO findNoteDetailRspVO) {
        if (Objects.nonNull(findNoteDetailRspVO)) {
            Integer visible = findNoteDetailRspVO.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
        }
    }

    /**
     * 删除本地笔记缓存
     * @param noteId
     */
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }
}
