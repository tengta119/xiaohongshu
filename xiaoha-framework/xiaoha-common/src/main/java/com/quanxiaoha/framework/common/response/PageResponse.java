package com.quanxiaoha.framework.common.response;


import lombok.Data;

import java.util.List;

/**
 * @author lbwxxc
 * @date 2025/6/4 19:12
 * @description:
 */
@Data
public class PageResponse<T> extends Response<List<T>> {

    // 当前页码
    private long pageNo;
    // 总数据量
    private long totalCount;
    // 每页展示的数据量
    private long pageSize;
    // 总页数
    private long totalPage;


    public static <T> PageResponse<T> success(List<T> data, long pageNo, long totalCount) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setSuccess(true);
        pageResponse.setData(data);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        // 每页展示的数据量
        long pageSize = 10L;
        pageResponse.setPageSize(pageSize);
        // 计算总页数
        long totalPage = (totalCount + pageSize - 1) / pageSize;
        pageResponse.setTotalPage(totalPage);
        return pageResponse;
    }


    public static <T> PageResponse<T> success(List<T> data, long pageNo, long totalCount, long pageSize) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setSuccess(true);
        pageResponse.setData(data);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        pageResponse.setPageSize(pageSize);
        // 计算总页数
        long totalPage = pageSize == 0 ? 0 : (totalCount + pageSize - 1) / pageSize;
        pageResponse.setTotalPage(totalPage);
        return pageResponse;
    }

    /**
     * 获取总页数
     * @return
     */
    public static long getTotalPage(long totalCount, long pageSize) {

        return pageSize == 0 ? 0 : (totalCount + pageSize - 1) / pageSize;
    }

    /**
     * 计算分页查询的 offset
     * @param pageNo
     * @param pageSize
     * @return
     */
    public static long getOffset(long pageNo, long pageSize) {
        // 如果页码小于 1，默认返回第一页的 offset
        if (pageNo < 1) {
            pageNo = 1;
        }
        return (pageNo - 1) * pageSize;
    }
}
