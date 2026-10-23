package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FindingRepository extends JpaRepository<Finding, String>, JpaSpecificationExecutor<Finding> {

    List<Finding> findByAuditIdOrderByIdAsc(String auditId);

    long countByResolved(boolean resolved);

    long countByResolvedFalseAndSeverityIn(Collection<Severity> severities);

    @Modifying
    @Query("delete from Finding f where f.auditId = :auditId")
    int deleteByAuditId(String auditId);

    @Query(value = "select nextval('finding_seq')", nativeQuery = true)
    long nextSequence();
}
