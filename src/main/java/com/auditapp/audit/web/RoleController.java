package com.auditapp.audit.web;

import com.auditapp.audit.service.RoleService;
import com.auditapp.audit.web.dto.RoleRequest;
import com.auditapp.audit.web.dto.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles")
public class RoleController {

    private final RoleService roles;

    public RoleController(RoleService roles) {
        this.roles = roles;
    }

    @GetMapping
    @Operation(summary = "List roles by name, optionally only active (true) or inactive (false) ones")
    public List<RoleResponse> list(@RequestParam(required = false) Boolean active) {
        return roles.list(active);
    }

    @GetMapping("/{id}")
    public RoleResponse get(@PathVariable long id) {
        return roles.get(id);
    }

    @PostMapping
    @Operation(summary = "Create a role; the name is stored upper-case and must be unique")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = roles.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a role's name, description and isActive")
    public RoleResponse update(@PathVariable long id, @Valid @RequestBody RoleRequest request) {
        return roles.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        roles.delete(id);
        return ResponseEntity.noContent().build();
    }
}
