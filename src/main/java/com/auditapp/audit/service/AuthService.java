package com.auditapp.audit.service;

import com.auditapp.audit.domain.Organization;
import com.auditapp.audit.domain.UserAccount;
import com.auditapp.audit.repository.OrganizationRepository;
import com.auditapp.audit.repository.UserAccountRepository;
import com.auditapp.audit.security.CurrentUser;
import com.auditapp.audit.security.TokenService;
import com.auditapp.audit.web.dto.LoginRequest;
import com.auditapp.audit.web.dto.OrganizationDetailsRequest;
import com.auditapp.audit.web.dto.OrganizationResponse;
import com.auditapp.audit.web.dto.SessionResponse;
import com.auditapp.audit.web.dto.SignupRequest;
import com.auditapp.audit.web.dto.TokenResponse;
import com.auditapp.audit.web.dto.UserResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserAccountRepository users;
    private final OrganizationRepository organizations;
    private final OrganizationWriter organizationWriter;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final CurrentUser currentUser;
    /** Compared against when the email is unknown, so a login takes as long whether or not the account exists. */
    private final String unknownUserHash;

    AuthService(UserAccountRepository users, OrganizationRepository organizations, OrganizationWriter organizationWriter,
                PasswordEncoder passwordEncoder, TokenService tokens, CurrentUser currentUser) {
        this.users = users;
        this.organizations = organizations;
        this.organizationWriter = organizationWriter;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.currentUser = currentUser;
        this.unknownUserHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /** Creates a user and their first organization, with the user as its only member. */
    @Transactional
    public TokenResponse signup(SignupRequest r) {
        String email = normaliseEmail(r.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }
        UserAccount user = users.save(new UserAccount(newId(), email, r.name().trim(), passwordEncoder.encode(r.password())));
        Organization organization = organizationWriter.create(user.getId(),
                OrganizationDetailsRequest.withLegalName(r.organizationLegalName()));
        return tokenFor(user, organization);
    }

    public TokenResponse login(LoginRequest r) {
        UserAccount user = users.findByEmail(normaliseEmail(r.email())).orElse(null);
        String hash = user == null ? unknownUserHash : user.getPasswordHash();
        if (!passwordEncoder.matches(r.password(), hash) || user == null) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        List<Organization> mine = organizations.findForUser(user.getId());
        if (mine.isEmpty()) {
            throw new ForbiddenException("Your account isn't a member of any organization. Ask a member to add you.");
        }
        Organization organization = r.organizationId() == null || r.organizationId().isBlank()
                ? mine.getFirst()
                : mine.stream().filter(o -> o.getId().equals(r.organizationId())).findFirst()
                        .orElseThrow(() -> new NotFoundException("Organization", r.organizationId()));
        return tokenFor(user, organization, mine);
    }

    public SessionResponse session() {
        UserAccount user = requireUser(currentUser.userId());
        List<Organization> mine = organizations.findForUser(user.getId());
        Organization organization = find(mine, currentUser.organizationId());
        return new SessionResponse(UserResponse.from(user), OrganizationResponse.from(organization), toResponses(mine));
    }

    /** A new token for another organization the caller belongs to. */
    public TokenResponse switchOrganization(String organizationId) {
        UserAccount user = requireUser(currentUser.userId());
        List<Organization> mine = organizations.findForUser(user.getId());
        return tokenFor(user, find(mine, organizationId), mine);
    }

    public TokenResponse tokenFor(UserAccount user, Organization organization) {
        return tokenFor(user, organization, organizations.findForUser(user.getId()));
    }

    UserAccount requireUser(String userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
    }

    static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String newId() {
        return UUID.randomUUID().toString();
    }

    private TokenResponse tokenFor(UserAccount user, Organization organization, List<Organization> mine) {
        TokenService.IssuedToken token = tokens.issue(user.getId(), organization.getId());
        return new TokenResponse(token.value(), token.expiresAt(), UserResponse.from(user),
                OrganizationResponse.from(organization), toResponses(mine));
    }

    private static Organization find(List<Organization> mine, String organizationId) {
        return mine.stream().filter(o -> o.getId().equals(organizationId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Organization", organizationId));
    }

    private static List<OrganizationResponse> toResponses(List<Organization> organizations) {
        return organizations.stream().map(OrganizationResponse::from).toList();
    }
}
