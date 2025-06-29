package com.quanxiaoha.xiaohashu.comment.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.constant.DateConstants;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.PageResponse;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.DateUtils;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.comment.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.comment.biz.constants.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.comment.biz.domain.dataobject.CommentDO;
import com.quanxiaoha.xiaohashu.comment.biz.domain.mapper.CommentDOMapper;
import com.quanxiaoha.xiaohashu.comment.biz.domain.mapper.NoteCountDOMapper;
import com.quanxiaoha.xiaohashu.comment.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.comment.biz.model.dto.PublishCommentMqDTO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.FindCommentItemRspVO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.FindCommentPageListReqVO;
import com.quanxiaoha.xiaohashu.comment.biz.model.vo.PublishCommentReqVO;
import com.quanxiaoha.xiaohashu.comment.biz.retry.SendMqRetryHelper;
import com.quanxiaoha.xiaohashu.comment.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.comment.biz.rpc.KeyValueRpcService;
import com.quanxiaoha.xiaohashu.comment.biz.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.comment.biz.service.CommentService;
import com.quanxiaoha.xiaohashu.kv.dto.req.FindCommentContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.dto.rsp.FindCommentContentRspDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/6/22 09:41
 * @description:
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    @Resource
    private UserRpcService  userRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {

        String content = publishCommentReqVO.getContent();
        String imageUrl = publishCommentReqVO.getImageUrl();
        // content 和 imageUrl 不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl), "评论正文和图片不能同时为空");


        // 发布者 ID
        Long creatorId = LoginUserContextHolder.getUserId();
        // RPC: 调用分布式 ID 生成服务，生成评论 ID
        String commentId = distributedIdGeneratorRpcService.generateCommentId();
        // 发送 MQ
        // 构建消息体 DTO
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .commentId(Long.valueOf(commentId))
                .build();


        // 发送 MQ (包含重试机制)
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success();
    }

    @Override
    public PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        // 笔记 ID
        Long noteId = findCommentPageListReqVO.getNoteId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        // 每页展示一级评论数
        long pageSize = 10;

        // 构建评论总数 Redis Key
        String noteCommentTotalKey = RedisKeyConstants.buildNoteCommentTotalKey(noteId);
        // 先从 Redis 中查询该笔记的评论总数
        Number commentTotal = (Number) redisTemplate.opsForHash()
                .get(noteCommentTotalKey, RedisKeyConstants.FIELD_COMMENT_TOTAL);

        Long count = Objects.isNull(commentTotal) ? 0 : commentTotal.longValue();
        if (Objects.isNull(commentTotal)) {
            Long dbCount = noteCountDOMapper.selectCommentTotalByNoteId(noteId);
            if (Objects.isNull(dbCount)) {
                throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
            }
            count = dbCount;
            // 异步将评论总数同步到 Redis 中
            threadPoolTaskExecutor.execute(() ->
                    syncNoteCommentTotal2Redis(noteCommentTotalKey, dbCount)
            );
        }

        // 若评论总数为 0，则直接响应
        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        // 分页返参
        List<FindCommentItemRspVO> commentRspVOS = null;
        if (count > 0) {
            commentRspVOS = Lists.newArrayList();
            long offset = PageResponse.getOffset(pageNo, pageSize);

            // 评论分页缓存使用 ZSET + STRING 实现
            // 构建评论 ZSET Key
            String commentZSetKey = RedisKeyConstants.buildCommentListKey(noteId);
            // 先判断 ZSET 是否存在
            boolean hasKey = redisTemplate.hasKey(commentZSetKey);
            if (!hasKey) {
                threadPoolTaskExecutor.execute(() -> syncHeatComments2Redis(commentZSetKey, noteId));
            }

            if (hasKey && offset < 500) {
                Set<Object> commentIds = redisTemplate
                        .opsForZSet().reverseRangeByScore(commentZSetKey, -Double.MAX_VALUE, Double.MAX_VALUE, offset, pageSize);
                // Set 转 List
                List<Object> commentIdList = Lists.newArrayList(commentIds);

                // 构建 MGET 批量查询评论详情的 Key 集合
                List<String> commentIdKeys = commentIdList.stream()
                        .map(RedisKeyConstants::buildCommentDetailKey)
                        .toList();

                // MGET 批量获取评论数据
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentIdKeys);

                // 可能存在部分评论不在缓存中，已经过期被删除，这些评论 ID 需要提取出来，等会查数据库
                List<Long> expiredCommentIds = Lists.newArrayList();
                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    if (Objects.nonNull(commentJson)) {
                        // 缓存中存在的评论 Json，直接转换为 VO 添加到返参集合中
                        FindCommentItemRspVO commentRspVO = JsonUtils.parseObject(commentJson, FindCommentItemRspVO.class);
                        commentRspVOS.add(commentRspVO);
                    } else {
                        expiredCommentIds.add(Long.valueOf(commentIdList.get(i).toString()));
                    }
                }

                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDOS, noteId, commentRspVOS);
                }

                // 按热度值进行降序排列
                commentRspVOS = commentRspVOS.stream()
                        .sorted(Comparator.comparing(FindCommentItemRspVO::getHeat).reversed())
                        .collect(Collectors.toList());

                return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
            }

            // 缓存中没有，则查询数据库
            // 查询一级评论
            List<CommentDO> oneLevelCommentDOS = commentDOMapper.selectPageList(noteId, offset, pageSize);
            getCommentDataAndSync2Redis(oneLevelCommentDOS, noteId, commentRspVOS);
            commentRspVOS = commentRspVOS.stream()
                                .sorted(Comparator.comparing(FindCommentItemRspVO::getHeat).reversed())
                                .collect(Collectors.toList());
        }

        return PageResponse.success(commentRspVOS, pageNo, pageSize);
    }

    /**
     * 获取全部评论数据，并将评论详情同步到 Redis 中
     */
    private void getCommentDataAndSync2Redis(List<CommentDO> oneLevelCommentDOS, Long noteId, List<FindCommentItemRspVO> commentRspVOS) {
        // 过滤出所有最早回复的二级评论 ID
        List<Long> twoLevelCommentIds = oneLevelCommentDOS.stream()
                .map(CommentDO::getFirstReplyCommentId)
                .filter(firstReplyCommentId -> firstReplyCommentId != 0)
                .toList();

        // 查询二级评论
        Map<Long, CommentDO> commentIdAndDOMap = null;
        List<CommentDO> twoLevelCommonDOS = null;
        if (CollUtil.isNotEmpty(twoLevelCommentIds)) {
            twoLevelCommonDOS = commentDOMapper.selectTwoLevelCommentByIds(twoLevelCommentIds);

            // 转 Map 集合，方便后续拼装数据
            commentIdAndDOMap = twoLevelCommonDOS.stream()
                    .collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
        }

        // 调用 KV 服务需要的入参
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        List<Long> userIds = Lists.newArrayList();

        // 将一级评论和二级评论合并到一起
        List<CommentDO> allCommentDOS = Lists.newArrayList();
        CollUtil.addAll(allCommentDOS, oneLevelCommentDOS);
        CollUtil.addAll(allCommentDOS, twoLevelCommonDOS);

        // 循环提取 RPC 调用需要的入参数据
        allCommentDOS.forEach(commentDO -> {
            // 构建调用 KV 服务批量查询评论内容的入参
            boolean isContentEmpty = commentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(commentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }

            // 构建调用用户服务批量查询用户信息的入参
            userIds.add(commentDO.getUserId());
        });

        // RPC: 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRspDTO> findCommentContentRspDTOS =
                keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);

        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentUuidAndContentMap = findCommentContentRspDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
        }

        // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

        // DTO 集合转 Map, 方便后续拼装数据
        Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            userIdAndDTOMap = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
        }

        // DO 转 VO, 组合拼装一二级评论数据
        for (CommentDO commentDO : oneLevelCommentDOS) {
            // 一级评论
            Long userId = commentDO.getUserId();
            FindCommentItemRspVO oneLevelCommentRspVO = FindCommentItemRspVO.builder()
                    .userId(userId)
                    .commentId(commentDO.getId())
                    .imageUrl(commentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(commentDO.getCreateTime()))
                    .likeTotal(commentDO.getLikeTotal())
                    .childCommentTotal(commentDO.getChildCommentTotal())
                    .heat(commentDO.getHeat())
                    .build();

            // 用户信息
            setUserInfo(commentIdAndDOMap, userIdAndDTOMap, userId, oneLevelCommentRspVO);
            // 笔记内容
            setCommentContent(commentUuidAndContentMap, commentDO, oneLevelCommentRspVO);


            // 二级评论
            Long firstReplyCommentId = commentDO.getFirstReplyCommentId();
            if (CollUtil.isNotEmpty(commentIdAndDOMap)) {
                CommentDO firstReplyCommentDO = commentIdAndDOMap.get(firstReplyCommentId);
                if (Objects.nonNull(firstReplyCommentDO)) {
                    Long firstReplyCommentUserId = firstReplyCommentDO.getUserId();
                    FindCommentItemRspVO firstReplyCommentRspVO = FindCommentItemRspVO.builder()
                            .userId(firstReplyCommentDO.getUserId())
                            .commentId(firstReplyCommentDO.getId())
                            .imageUrl(firstReplyCommentDO.getImageUrl())
                            .createTime(DateUtils.formatRelativeTime(firstReplyCommentDO.getCreateTime()))
                            .likeTotal(firstReplyCommentDO.getLikeTotal())
                            .heat(firstReplyCommentDO.getHeat())
                            .build();

                    setUserInfo(commentIdAndDOMap, userIdAndDTOMap, firstReplyCommentUserId, firstReplyCommentRspVO);

                    // 用户信息
                    oneLevelCommentRspVO.setFirstReplyComment(firstReplyCommentRspVO);
                    // 笔记内容
                    setCommentContent(commentUuidAndContentMap, firstReplyCommentDO, firstReplyCommentRspVO);
                }
            }
            commentRspVOS.add(oneLevelCommentRspVO);
        }

        // 异步将笔记详情，同步到 Redis 中
        threadPoolTaskExecutor.execute(() -> {
            // 准备批量写入的数据
            Map<String, String> data = Maps.newHashMap();
            commentRspVOS.forEach(commentRspVO -> {
                // 评论 ID
                Long commentId = commentRspVO.getCommentId();
                // 构建 Key
                String key = RedisKeyConstants.buildCommentDetailKey(commentId);
                data.put(key, JsonUtils.toJsonString(commentRspVO));
            });

            // 使用 Redis Pipeline 提升写入性能
            redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    // 将 Java 对象序列化为 JSON 字符串
                    String jsonStr = JsonUtils.toJsonString(entry.getValue());

                    // 随机生成过期时间 (5小时以内)
                    int randomExpire = RandomUtil.randomInt(5 * 60 * 60);

                    // 批量写入并设置过期时间
                    connection.setEx(
                            Objects.requireNonNull(redisTemplate.getStringSerializer().serialize(entry.getKey())),
                            randomExpire,
                            Objects.requireNonNull(redisTemplate.getStringSerializer().serialize(jsonStr))
                    );
                }
                return null;
            });
        });
    }

    /**
     * 同步热点评论至 Redis
     */
    private void syncHeatComments2Redis(String key, Long noteId) {
        List<CommentDO> commentDOS = commentDOMapper.selectHeatComments(noteId);
        if (CollUtil.isNotEmpty(commentDOS)) {
            // 使用 Redis Pipeline 提升写入性能
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
                // 遍历评论数据并批量写入 ZSet
                for (CommentDO commentDO : commentDOS) {
                    Long commentId = commentDO.getId();
                    Double heat = commentDO.getHeat();
                    zSetOps.add(key, commentId, heat);
                }

                // 设置随机过期时间，单位：秒
                int expireTime = RandomUtil.randomInt(5 * 60 * 60);
                redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);

                return null;
            });
        }
    }

    /**
     * 同步笔记评论总数到 Redis 中
     */
    private void syncNoteCommentTotal2Redis(String noteCommentTotalKey, Long dbCount) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForHash().put(noteCommentTotalKey, RedisKeyConstants.FIELD_COMMENT_TOTAL, dbCount);
                // 随机过期时间 (保底1小时 + 随机时间)，单位：秒
                long expireTime = 60*60 + RandomUtil.randomInt(4*60*60);
                operations.expire(noteCommentTotalKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    /**
     * 设置评论内容
     */
    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentDO commentDO1, FindCommentItemRspVO firstReplyCommentRspVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentDO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }

    /**
     * 设置用户信息
     */
    private static void setUserInfo(Map<Long, CommentDO> commentIdAndDOMap, Map<Long, FindUserByIdRspDTO> userIdAndDTOMap, Long userId, FindCommentItemRspVO oneLevelCommentRspVO) {
        FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
        if (Objects.nonNull(findUserByIdRspDTO)) {
            oneLevelCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
            oneLevelCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
        }
    }

}
