package com.quanxiaoha.xiaohashu.distributed.id.generator.api;


import com.quanxiaoha.xiaohashu.distributed.id.generator.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lbwxxc
 * @date 2025/6/1 10:48
 * @description:
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface DistributedIdGeneratorFeignApi {

    String PREFIX = "/id";

    @GetMapping(value = PREFIX + "/segment/get/{key}")
    String getSegmentId(@PathVariable("key") String key);

    @GetMapping(value = PREFIX +  "/snowflake/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);

}
