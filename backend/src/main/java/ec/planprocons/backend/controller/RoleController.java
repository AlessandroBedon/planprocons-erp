package ec.planprocons.backend.controller;

import ec.planprocons.backend.util.ResponseFactory;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.request.RoleRequest;
import ec.planprocons.backend.dto.response.RoleResponse;
import ec.planprocons.backend.service.interfaces.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;


import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor

public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('ROL_VER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listar() {

        return ResponseFactory.ok(
                "Roles obtenidos correctamente",
                roleService.listar()
        );
    }

    @PreAuthorize("hasAuthority('ROL_CREAR')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> guardar(
            @Valid @RequestBody RoleRequest request
    ) {

        return ResponseFactory.created(
                "Rol creado correctamente",
                roleService.guardar(request)
        );
    }
}
