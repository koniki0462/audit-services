package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Finding;
import com.auditapp.audit.domain.Severity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Every lookup takes the caller's organization. Don't use the inherited id-only methods from services. */
public interface FindingRepository extends JpaRepository<Finding, String>, JpaSpecificationExecutor<Finding> {

    Optional<Finding> findByIdAndOrganizationId(String id, String organizationId);

    List<Finding> findByOrganizationIdAndAuditIdOrderByIdAsc(String organizationId, String auditId);

    long countByOrganizationIdAndResolved(String organizationId, boolean resolved);

    long countByOrganizationIdAndResolvedFalseAndSeverityIn(String organizationId, Collection<Severity> severities);

    @Modifying
    @Query("delete from Finding f where f.organizationId = :organizationId and f.auditId = :auditId")
    int deleteByOrganizationIdAndAuditId(String organizationId, String auditId);

    @Query(value = "select nextval('finding_seq')", nativeQuery = true)
    long nextSequence();
}
