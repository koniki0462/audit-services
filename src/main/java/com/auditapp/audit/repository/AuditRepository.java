package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AuditRepository extends JpaRepository<Audit, String>, JpaSpecificationExecutor<Audit> {

    long countByStatusIn(Collection<AuditStatus> statuses);

    long countByStatus(AuditStatus status);

    @Query(value = "select nextval('audit_seq')", nativeQuery = true)
    long nextSequence();
}
