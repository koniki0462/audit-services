package com.auditapp.audit.repository;

import com.auditapp.audit.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    /** Emails are stored lower-case, so pass a normalised email. */
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    /** The members of an organization, by name. */
    @Query("""
            select u from UserAccount u
            where u.id in (select m.userId from Membership m where m.organizationId = :organizationId)
            order by lower(u.name), u.id""")
    List<UserAccount> findMembers(String organizationId);
}
