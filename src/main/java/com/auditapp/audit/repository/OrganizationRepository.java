package com.auditapp.audit.repository;

import com.auditapp.audit.domain.Organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

    /** The organizations a user belongs to, by legal name. */
    @Query("""
            select o from Organization o
            where o.id in (select m.organizationId from Membership m where m.userId = :userId)
            order by lower(o.legalName), o.id""")
    List<Organization> findForUser(String userId);

    boolean existsByLegalNameIgnoreCase(String legalName);

    boolean existsByLegalNameIgnoreCaseAndIdNot(String legalName, String id);
}
