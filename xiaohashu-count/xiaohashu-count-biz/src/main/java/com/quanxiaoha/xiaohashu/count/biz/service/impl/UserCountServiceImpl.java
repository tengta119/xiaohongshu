package com.quanxiaoha.xiaohashu.count.biz.service.impl;


import com.google.common.collect.Maps;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.dataobject.UserCountDO;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.service.UserCountService;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdReqDTO;
import com.quanxiaoha.xiaohashu.count.dto.FindUserCountsByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lbwxxc
 * @date 2025/7/7 10:43
 * @description:
 */
@Service
public class UserCountServiceImpl implements UserCountService {

    @Resource
    UserCountDOMapper userCountDOMapper;
    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) {
        // 目标用户 ID
        Long userId = findUserCountsByIdReqDTO.getUserId();
        FindUserCountsByIdRspDTO findUserCountByIdRspDTO = FindUserCountsByIdRspDTO.builder()
                .userId(userId)
                .build();

        String key = RedisKeyConstants.buildCountUserKey(userId);
        List<Object> counts = redisTemplate.opsForHash().multiGet(key, List.of(
                RedisKeyConstants.FIELD_COLLECT_TOTAL,
                RedisKeyConstants.FIELD_FANS_TOTAL,
                RedisKeyConstants.FIELD_NOTE_TOTAL,
                RedisKeyConstants.FIELD_FOLLOWING_TOTAL,
                RedisKeyConstants.FIELD_LIKE_TOTAL
        ));

        // 若 Hash 中计数不为空，优先以其为主（实时性更高）
        Object collectTotal = counts.get(0);
        Object fansTotal = counts.get(1);
        Object noteTotal = counts.get(2);
        Object followingTotal = counts.get(3);
        Object likeTotal = counts.get(4);
        findUserCountByIdRspDTO.setCollectTotal(Objects.isNull(collectTotal) ? 0 : Long.parseLong(String.valueOf(collectTotal)));
        findUserCountByIdRspDTO.setFansTotal(Objects.isNull(fansTotal) ? 0 : Long.parseLong(String.valueOf(fansTotal)));
        findUserCountByIdRspDTO.setNoteTotal(Objects.isNull(noteTotal) ? 0 : Long.parseLong(String.valueOf(noteTotal)));
        findUserCountByIdRspDTO.setFollowingTotal(Objects.isNull(followingTotal) ? 0 : Long.parseLong(String.valueOf(followingTotal)));
        findUserCountByIdRspDTO.setLikeTotal(Objects.isNull(likeTotal) ? 0 : Long.parseLong(String.valueOf(likeTotal)));

        boolean isAnyNull = counts.stream().anyMatch(Objects::isNull);
        if (isAnyNull) {
            // 从数据库查询该用户的计数
            UserCountDO userCountDO = userCountDOMapper.selectByUserId(userId);

            // 判断 Redis 中对应计数，若为空，则使用 DO 中的计数
            if (Objects.nonNull(userCountDO) && Objects.isNull(collectTotal)) {
                findUserCountByIdRspDTO.setCollectTotal(userCountDO.getCollectTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(fansTotal)) {
                findUserCountByIdRspDTO.setFansTotal(userCountDO.getFansTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(noteTotal)) {
                findUserCountByIdRspDTO.setNoteTotal(userCountDO.getNoteTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(followingTotal)) {
                findUserCountByIdRspDTO.setFollowingTotal(userCountDO.getFollowingTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(likeTotal)) {
                findUserCountByIdRspDTO.setLikeTotal(userCountDO.getLikeTotal());
            }

            syncHashCount2Redis(key, userCountDO, collectTotal, fansTotal, noteTotal, followingTotal, likeTotal);
        }



        return Response.success(findUserCountByIdRspDTO);
    }

    /**
     * 将该用户的 Hash 计数同步到 Redis 中
     */
    private void syncHashCount2Redis(String userCountHashKey, UserCountDO userCountDO,
                                     Object collectTotal, Object fansTotal, Object noteTotal, Object followingTotal, Object likeTotal) {
        if (Objects.nonNull(userCountDO)) {
            threadPoolTaskExecutor.submit(() -> {
                // 存放计数
                Map<String, Long> userCountMap = Maps.newHashMap();
                if (Objects.isNull(collectTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_COLLECT_TOTAL, Objects.isNull(userCountDO.getCollectTotal()) ? 0 : userCountDO.getCollectTotal());

                if (Objects.isNull(fansTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_FANS_TOTAL, Objects.isNull(userCountDO.getFansTotal()) ? 0 : userCountDO.getFansTotal());

                if (Objects.isNull(noteTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_NOTE_TOTAL, Objects.isNull(userCountDO.getNoteTotal()) ? 0 : userCountDO.getNoteTotal());

                if (Objects.isNull(followingTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_FOLLOWING_TOTAL, Objects.isNull(userCountDO.getFollowingTotal()) ? 0 : userCountDO.getFollowingTotal());

                if (Objects.isNull(likeTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_LIKE_TOTAL, Objects.isNull(userCountDO.getLikeTotal()) ? 0 : userCountDO.getLikeTotal());

                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        operations.opsForHash().putAll(userCountHashKey, userCountMap);
                        operations.expire(userCountHashKey, 60 * 60, TimeUnit.SECONDS);
                        return null;
                    }
                });
            });
        }
    }
}
