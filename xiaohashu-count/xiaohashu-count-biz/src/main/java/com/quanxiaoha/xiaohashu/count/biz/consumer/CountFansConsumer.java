package com.quanxiaoha.xiaohashu.count.biz.consumer;


import ch.qos.logback.core.model.INamedModel;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.FollowUnfollowTypeEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lbwxxc
 * @date 2025/6/9 16:08
 * @description:
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_FANS, // Group 组
        topic = MQConstants.TOPIC_COUNT_FANS // 主题 Topic
)
@Slf4j
public class CountFansConsumer implements RocketMQListener<String> {

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 聚合消息, size: {}", bodys.size());
        log.info("==> 聚合消息, {}", JsonUtils.toJsonString(bodys));

        // List<String> 转 List<CountFollowUnfollowMqDTO>
        List<CountFollowUnfollowMqDTO> countFollowUnfollowMqDTOS = bodys.stream()
                .map(body -> JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class)).toList();

        // 按目标用户进行分组
        Map<Long, List<CountFollowUnfollowMqDTO>> groupMap = countFollowUnfollowMqDTOS.stream()
                .collect(Collectors.groupingBy(CountFollowUnfollowMqDTO::getUserId));

        // 按组汇总数据，统计出最终的计数
        // key 为目标用户ID, value 为最终操作的计数
        HashMap<Long, Integer> countMap = new HashMap<>();

        for (Map.Entry<Long, List<CountFollowUnfollowMqDTO>> entry : groupMap.entrySet()) {
            List<CountFollowUnfollowMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (CountFollowUnfollowMqDTO countFollowUnfollowMqDTO : list) {
                Integer type = countFollowUnfollowMqDTO.getType();
                FollowUnfollowTypeEnum followUnfollowTypeEnum = FollowUnfollowTypeEnum.valueOf(type);
                if (followUnfollowTypeEnum == null) {
                    continue;
                }
                switch (followUnfollowTypeEnum) {
                    case FOLLOW -> finalCount++;
                    case UNFOLLOW -> finalCount--;
                }
            }
            // 将分组后统计出的最终计数，存入 countMap 中
            countMap.put(entry.getKey(), finalCount);
        }

        countMap.forEach((k, v) -> {
            String redisKey = RedisKeyConstants.buildCountUserKey(k);
            Boolean isExisted = redisTemplate.hasKey(redisKey);
            if (isExisted) {
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_FANS_TOTAL, v);
            }
        });

        // TODO: 发送 MQ, 计数数据落库
    }
}
