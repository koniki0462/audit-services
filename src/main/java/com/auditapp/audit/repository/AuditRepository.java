package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Audit;
import com.auditapp.audit.domain.AuditStatus;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/** Every lookup takes the caller's organization. Don't use the inherited id-only methods from services. */
public interface AuditRepository extends JpaRepository<Audit, String>, JpaSpecificationExecutor<Audit> {

    Optional<Audit> findByIdAndOrganizationId(String id, String organizationId);

    boolean existsByIdAndOrganizationId(String id, String organizationId);

    long countByOrganizationId(String organizationId);

    long countByOrganizationIdAndStatusIn(String organizationId, Collection<AuditStatus> statuses);

    long countByOrganizationIdAndStatus(String organizationId, AuditStatus status);

    @Query(value = "select nextval('audit_seq')", nativeQuery = true)
    long nextSequence();
}
