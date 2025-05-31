package com.quanxiaoha.xiaohashu.kv.biz.domain.repository;


import com.quanxiaoha.xiaohashu.kv.biz.domain.dataobject.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * @author lbwxxc
 * @date 2025/5/31 19:51
 * @description:
 */
public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {
}
