package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Membership;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, String> {

    boolean existsByUserIdAndOrganizationId(String userId, String organizationId);

    Optional<Membership> findByUserIdAndOrganizationId(String userId, String organizationId);

    long countByOrganizationId(String organizationId);
}
