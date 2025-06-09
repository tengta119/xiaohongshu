package com.quanxiaoha.xiaohashu.count.biz.consumer;


import cn.hutool.core.collection.CollUtil;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.dataobject.UserCountDO;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lbwxxc
 * @date 2025/6/9 16:59
 * @description:
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_FANS_2_DB,
        topic = MQConstants.TOPIC_COUNT_FANS_2_DB
)
public class CountFans2DBConsumer implements RocketMQListener<String> {

    @Resource
    UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(String body) {
        log.info("## 消费到了 MQ 【计数: 粉丝数入库】, {}...", body);
        Map<Long, Integer> countMap = new HashMap<>();
        try {
            countMap = JsonUtils.parseMap(body, Long.class, Integer.class);
        } catch (Exception e) {
            log.error("## 解析 JSON 字符串异常", e);
        }

        if (CollUtil.isNotEmpty(countMap)) {
            countMap.forEach((k, v) -> {
                userCountDOMapper.insertOrUpdateFansTotalByUserId(v, k);
            });
        }
    }
}
