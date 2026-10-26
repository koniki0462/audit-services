package com.auditapp.audit.repository;

import com.auditapp.audit.domain.MemberRole;
import com.auditapp.audit.domain.Membership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, String> {

    boolean existsByUserIdAndOrganizationId(String userId, String organizationId);

    Optional<Membership> findByUserIdAndOrganizationId(String userId, String organizationId);

    List<Membership> findByOrganizationId(String organizationId);

    long countByOrganizationId(String organizationId);

    long countByOrganizationIdAndRole(String organizationId, MemberRole role);
}
