package com.quanxiaoha.xiaohashu.search.canal;


import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.quanxiaoha.framework.common.enums.StatusEnum;
import com.quanxiaoha.xiaohashu.search.domain.mapper.SelectMapper;
import com.quanxiaoha.xiaohashu.search.enums.NoteStatusEnum;
import com.quanxiaoha.xiaohashu.search.enums.NoteVisibleEnum;
import com.quanxiaoha.xiaohashu.search.index.NoteIndex;
import com.quanxiaoha.xiaohashu.search.index.UserIndex;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lbwxxc
 * @date 2025/6/21 10:40
 * @description:
 */
@Component
@Slf4j
public class CanalSchedule implements Runnable {

    @Resource
    private CanalConnector canalConnector;
    @Resource
    private CanalProperties canalProperties;
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    @Scheduled(fixedDelay = 100)
    public void run() {
        // 初始化批次 ID，-1 表示未开始或未获取到数据
        long batchId = -1;
        try {
            Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize());
            batchId = message.getId();

            // 获取当前批次中的数据条数
            long size = message.getEntries().size();
            if (batchId == -1 || size == 0) {
                try {
                    // 拉取数据为空，休眠 1s, 防止频繁拉取
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {}
            } else {
                // 如果当前批次有数据，打印这批次中的数据条目
                processEntry(message.getEntries());
            }

            canalConnector.ack(batchId);
        } catch (Exception e) {
            log.error("消费 Canal 批次数据异常", e);
            // 如果出现异常，需要进行数据回滚，以便重新消费这批次的数据
            canalConnector.rollback(batchId);
        }
    }

    /**
     * 处理这一批次数据
     */
    private void processEntry(List<CanalEntry.Entry> entrys) throws Exception {
        // 循环处理批次数据
        for (CanalEntry.Entry entry : entrys) {
            // 只处理 ROWDATA 行数据类型的 Entry，忽略事务等其他类型
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 获取事件类型（如：INSERT、UPDATE、DELETE 等等）
                CanalEntry.EventType eventType = entry.getHeader().getEventType();
                // 获取数据库名称
                String database = entry.getHeader().getSchemaName();
                // 获取表名称
                String table = entry.getHeader().getTableName();

                // 解析出 RowChange 对象，包含 RowData 和事件相关信息
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                // 遍历所有行数据（RowData）
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    // 获取行中所有列的最新值（AfterColumns）
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();

                    // 将列数据解析为 Map，方便后续处理
                    Map<String, Object> columnMap = parseColumns2Map(columns);

                    //自定义处理
                    log.info("EventType: {}, Database: {}, Table: {}, Columns: {}", eventType, database, table, columnMap);
                    processEvent(columnMap, table, eventType);
                }
            }
        }
    }

    /**
     * 处理事件
     */
    private void processEvent(Map<String, Object> columnMap, String table, CanalEntry.EventType eventType) throws Exception {
        switch (table) {
            case "t_note" -> handleNoteEvent(columnMap, eventType); // 笔记表
            case "t_user" -> handleUserEvent(columnMap, eventType); // 用户表
            default -> log.warn("Table: {} not support", table);
        }
    }

    /**
     * 处理笔记表事件
     */
    private void handleNoteEvent(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        // 获取笔记 ID
        Long noteId = Long.parseLong(columnMap.get("id").toString());

        // 不同的事件，处理逻辑不同
        switch (eventType) {
            case INSERT -> syncNoteIndex(noteId); // 记录新增事件
            case UPDATE -> { // 记录更新事件
                // 笔记变更后的状态
                Integer status = Integer.parseInt(columnMap.get("status").toString());
                // 笔记可见范围
                Integer visible = Integer.parseInt(columnMap.get("visible").toString());

                if (Objects.equals(status, NoteStatusEnum.NORMAL.getCode())
                        && Objects.equals(visible, NoteVisibleEnum.PUBLIC.getCode())) { // 正常展示，并且可见性为公开
                    // 对索引进行覆盖更新
                    syncNoteIndex(noteId);
                } else if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode()) // 仅对自己可见
                        || Objects.equals(status, NoteStatusEnum.DELETED.getCode())
                        || Objects.equals(status, NoteStatusEnum.DOWNED.getCode())) { // 被逻辑删除、被下架
                    // 删除笔记文档
                    deleteNoteDocument(String.valueOf(noteId));
                }
            }
            default -> log.warn("Unhandled event type for t_note: {}", eventType);
        }
    }

    /**
     * 处理用户表事件
     */
    private void handleUserEvent(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        // 获取用户 ID
        Long userId = Long.parseLong(columnMap.get("id").toString());

        // 不同的事件，处理逻辑不同
        switch (eventType) {
            case INSERT -> syncUserIndex(userId); // 记录新增事件
            case UPDATE -> { // 记录更新事件
                // 用户变更后的状态
                Integer status = Integer.parseInt(columnMap.get("status").toString());
                // 逻辑删除
                Integer isDeleted = Integer.parseInt(columnMap.get("is_deleted").toString());

                if (Objects.equals(status, StatusEnum.ENABLE.getValue())
                        && Objects.equals(isDeleted, 0)) { // 用户状态为已启用，并且未被逻辑删除
                    // 更新用户索引、笔记索引
                    syncNotesIndexAndUserIndex(userId);
                } else if (Objects.equals(status, StatusEnum.DISABLED.getValue()) // 用户状态为禁用
                        || Objects.equals(isDeleted, 1)) { // 被逻辑删除
                    // 删除用户文档
                    deleteUserDocument(String.valueOf(userId));
                }
            }
            default -> log.warn("Unhandled event type for t_user: {}", eventType);
        }
    }

    /**
     * 删除指定 ID 的用户文档
     * @param documentId
     * @throws Exception
     */
    private void deleteUserDocument(String documentId) throws Exception {
        // 创建删除请求对象，指定索引名称和文档 ID
        DeleteRequest deleteRequest = new DeleteRequest(UserIndex.NAME, documentId);
        // 执行删除操作，将指定文档从 Elasticsearch 索引中删除
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 同步用户索引、笔记索引（可能是多条）
     * @param userId
     */
    private void syncNotesIndexAndUserIndex(Long userId) throws Exception {
        // 创建一个 BulkRequest
        BulkRequest bulkRequest = new BulkRequest();

        // 1. 用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : userResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(UserIndex.FIELD_USER_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将每个 IndexRequest 加入到 BulkRequest
            bulkRequest.add(indexRequest);
        }

        // 2. 笔记索引
        List<Map<String, Object>> noteResult = selectMapper.selectEsNoteIndexData(null, userId);
        for (Map<String, Object> recordMap : noteResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(NoteIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(NoteIndex.FIELD_NOTE_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将每个 IndexRequest 加入到 BulkRequest
            bulkRequest.add(indexRequest);
        }

        // 执行批量请求
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 同步用户索引
     * @param userId
     */
    private void syncUserIndex(Long userId) throws Exception {
        // 1. 同步用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : userResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(UserIndex.FIELD_USER_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 同步笔记索引
     */
    private void syncNoteIndex(Long noteId) throws Exception {
        List<Map<String, Object>> result = selectMapper.selectEsNoteIndexData(noteId, null);

        for (Map<String, Object> recordMap : result) {
            IndexRequest indexRequest = new IndexRequest(NoteIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(NoteIndex.FIELD_NOTE_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 删除指定 ID 的文档
     */
    private void deleteNoteDocument(String documentId) throws Exception {
        // 创建删除请求对象，指定索引名称和文档 ID
        DeleteRequest deleteRequest = new DeleteRequest(NoteIndex.NAME, documentId);
        // 执行删除操作，将指定文档从 Elasticsearch 索引中删除
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 将列数据解析为 Map
     */
    private Map<String, Object> parseColumns2Map(List<CanalEntry.Column> columns) {
        Map<String, Object> map = Maps.newHashMap();
        columns.forEach(column -> {
            if (Objects.isNull(column)) return;
            map.put(column.getName(), column.getValue());
        });
        return map;
    }

    /**
     * 打印这一批次中的数据条目（和官方示例代码一致，后续小节中会自定义这块）
     */
    private void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                    || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }


    }

    /**
     * 打印字段信息
     */
    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
