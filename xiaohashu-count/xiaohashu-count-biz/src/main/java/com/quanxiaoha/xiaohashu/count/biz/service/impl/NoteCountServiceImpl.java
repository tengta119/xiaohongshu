package com.quanxiaoha.xiaohashu.count.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.dataobject.NoteCountDO;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.NoteCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.service.NoteCountService;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdRspDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindNoteCountsByIdsReqDTO;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/7/9 11:42
 * @description:
 */
@Service
public class NoteCountServiceImpl implements NoteCountService {

    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO) {
        List<Long> noteIds = findNoteCountsByIdsReqDTO.getNoteIds();
        // 1. 先查询 Redis 缓存
        List<String> hashKeys = noteIds.stream().map(RedisKeyConstants::buildCountNoteKey).toList();
        List<Object> countHashes = getCountHashesByPipelineFromRedis(hashKeys);

        // 返参 DTO
        List<FindNoteCountsByIdRspDTO> findNoteCountsByIdRspDTOS = Lists.newArrayList();

        // 用于存储缓存中不存在，需要查数据库的笔记 ID
        List<Long> noteIdsNeedQuery = Lists.newArrayList();

        // 循环入参中需要查询的笔记 ID 集合，构建对应 DTO, 并设置缓存中已存在的计数，以及过滤出需要查数据库的笔记 ID
        for (int i = 0; i < noteIds.size(); i++) {
            Long currNoteId = noteIds.get(i);
            List<Integer> currCountHash = (List<Integer>) countHashes.get(i);
            // 点赞数、收藏数、评论数
            Integer likeTotal = currCountHash.get(0);
            Integer collectTotal = currCountHash.get(1);
            Integer commentTotal = currCountHash.get(2);

            // Hash 中存在任意一个 Field 为 null, 都需要查询数据库
            if (Objects.isNull(likeTotal) || Objects.isNull(collectTotal) || Objects.isNull(commentTotal)) {
                noteIdsNeedQuery.add(currNoteId);
            }

            // 构建 DTO
            FindNoteCountsByIdRspDTO findNoteCountsByIdRspDTO = FindNoteCountsByIdRspDTO.builder()
                    .noteId(currNoteId)
                    .likeTotal(Objects.nonNull(likeTotal) ? Long.valueOf(likeTotal) : null)
                    .collectTotal(Objects.nonNull(collectTotal) ? Long.valueOf(collectTotal) : null)
                    .commentTotal(Objects.nonNull(commentTotal) ? Long.valueOf(commentTotal) : null)
                    .build();

            findNoteCountsByIdRspDTOS.add(findNoteCountsByIdRspDTO);
        }

        if (CollUtil.isEmpty(noteIdsNeedQuery)) {
            return Response.success(findNoteCountsByIdRspDTOS);
        }

        // 2. 若缓存中无，则查询数据库
        List<NoteCountDO> noteCountDOS = noteCountDOMapper.selectByNoteIds(noteIdsNeedQuery);
        if (CollUtil.isNotEmpty(noteCountDOS)) {
            // DO 集合转 Map, 方便后续查询对应笔记 ID 的计数
            Map<Long, NoteCountDO> noteIdAndDOMap = noteCountDOS.stream()
                    .collect(Collectors.toMap(NoteCountDO::getNoteId, noteCountDO -> noteCountDO));

            // 将笔记 Hash 计数同步到 Redis 中
            syncNoteHash2Redis(findNoteCountsByIdRspDTOS, noteIdAndDOMap);

            // 针对 DTO 中为 null 的计数字段，循环设置从数据库中查询到的计数
            for (FindNoteCountsByIdRspDTO findNoteCountsByIdRspDTO : findNoteCountsByIdRspDTOS) {
                Long noteId = findNoteCountsByIdRspDTO.getNoteId();
                Long likeTotal = findNoteCountsByIdRspDTO.getLikeTotal();
                Long collectTotal = findNoteCountsByIdRspDTO.getCollectTotal();
                Long commentTotal = findNoteCountsByIdRspDTO.getCommentTotal();
                NoteCountDO noteCountDO = noteIdAndDOMap.get(noteId);
                if (Objects.isNull(likeTotal))
                    findNoteCountsByIdRspDTO.setLikeTotal(Objects.nonNull(noteCountDO) ? noteCountDO.getLikeTotal() : 0);
                if (Objects.isNull(collectTotal))
                    findNoteCountsByIdRspDTO.setCollectTotal(Objects.nonNull(noteCountDO) ? noteCountDO.getCollectTotal() : 0);
                if (Objects.isNull(commentTotal))
                    findNoteCountsByIdRspDTO.setCommentTotal(Objects.nonNull(noteCountDO) ? noteCountDO.getCommentTotal() : 0);
            }
        }

        return Response.success(findNoteCountsByIdRspDTOS);
    }

    /**
     * 从 Redis 中批量查询笔记 Hash 计数
     */
    private List<Object> getCountHashesByPipelineFromRedis(List<String> hashKeys) {
        return redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                for (String hashKey : hashKeys) {
                    operations.opsForHash().multiGet(hashKey, List.of(
                            RedisKeyConstants.FIELD_LIKE_TOTAL,
                            RedisKeyConstants.FIELD_COLLECT_TOTAL,
                            RedisKeyConstants.FIELD_COMMENT_TOTAL));
                }

                return null;
            }
        });
    }

    /**
     * 将笔记 Hash 计数同步到 Redis 中
     */
    private void syncNoteHash2Redis(List<FindNoteCountsByIdRspDTO> findNoteCountsByIdRspDTOS, Map<Long, NoteCountDO> noteIdAndDOMap) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 循环已构建好的返参 DTO 集合
                for (FindNoteCountsByIdRspDTO findNoteCountsByIdRspDTO : findNoteCountsByIdRspDTOS) {
                    Long likeTotal = findNoteCountsByIdRspDTO.getLikeTotal();
                    Long collectTotal = findNoteCountsByIdRspDTO.getCollectTotal();
                    Long commentTotal = findNoteCountsByIdRspDTO.getCommentTotal();

                    // 若当前 DTO 的所有计数都不为空，则无需同步 Hash
                    if (Objects.nonNull(likeTotal) && Objects.nonNull(collectTotal) && Objects.nonNull(commentTotal)) {
                        continue;
                    }

                    // 否则，若有任意一个 Field 计数为空，则需要同步对应的 Field
                    Long noteId = findNoteCountsByIdRspDTO.getNoteId();
                    // 构建 Hash Key
                    String noteCountHashKey = RedisKeyConstants.buildCountNoteKey(noteId);

                    // 设置 Field 计数
                    Map<String, Long> countMap = Maps.newHashMap();
                    NoteCountDO noteCountDO = noteIdAndDOMap.get(noteId);

                    if (Objects.isNull(likeTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_LIKE_TOTAL,
                                Objects.nonNull(noteCountDO) ? noteCountDO.getLikeTotal() : 0);
                    }
                    if (Objects.isNull(collectTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_COLLECT_TOTAL,
                                Objects.nonNull(noteCountDO) ? noteCountDO.getCollectTotal() : 0);
                    }
                    if (Objects.isNull(commentTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_COMMENT_TOTAL,
                                Objects.nonNull(noteCountDO) ? noteCountDO.getCommentTotal() : 0);
                    }

                    operations.opsForHash().putAll(noteCountHashKey, countMap);
                    // 设置随机过期时间 (1小时以内)
                    long expireTime = 60*30 + RandomUtil.randomInt(60 * 30);
                    operations.expire(noteCountHashKey, expireTime, TimeUnit.SECONDS);
                }


                return null;
            }
        });
    }
}
