package com.auditapp.audit.service;

import com.auditapp.audit.domain.Role;
import com.auditapp.audit.repository.RoleRepository;
import com.auditapp.audit.web.dto.RoleRequest;
import com.auditapp.audit.web.dto.RoleResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD for the global role catalogue. Roles aren't tied to an organization, so there is no tenant scoping here. */
@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roles;

    RoleService(RoleRepository roles) {
        this.roles = roles;
    }

    public List<RoleResponse> list(Boolean active) {
        List<Role> found = active == null ? roles.findAllByOrderByNameAsc() : roles.findAllByActiveOrderByNameAsc(active);
        return found.stream().map(RoleResponse::from).toList();
    }

    public RoleResponse get(long id) {
        return RoleResponse.from(find(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        String name = normaliseName(request.name());
        if (roles.existsByNameIgnoreCase(name)) {
            throw duplicate(name);
        }
        Role role = new Role(name);
        apply(role, request);
        return RoleResponse.from(roles.saveAndFlush(role));
    }

    @Transactional
    public RoleResponse update(long id, RoleRequest request) {
        Role role = find(id);
        String name = normaliseName(request.name());
        if (roles.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicate(name);
        }
        role.setName(name);
        apply(role, request);
        return RoleResponse.from(roles.saveAndFlush(role));
    }

    @Transactional
    public void delete(long id) {
        roles.delete(find(id));
    }

    private Role find(long id) {
        return roles.findById(id).orElseThrow(() -> new NotFoundException("Role", String.valueOf(id)));
    }

    private static void apply(Role role, RoleRequest r) {
        role.setDescription(r.description() == null || r.description().isBlank() ? null : r.description().trim());
        role.setActive(r.isActive() == null || r.isActive());
    }

    private static String normaliseName(String name) {
        return name.trim().toUpperCase(Locale.ROOT);
    }

    private static ConflictException duplicate(String name) {
        return new ConflictException("A role named " + name + " already exists.");
    }
}
