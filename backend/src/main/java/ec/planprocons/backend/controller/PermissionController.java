package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.PermissionRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.PermissionResponse;
import ec.planprocons.backend.service.interfaces.PermissionService;
import ec.planprocons.backend.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor

public class PermissionController {

    private final PermissionService service;

    @PreAuthorize("hasAuthority('PERMISO_VER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listar() {

        return ResponseFactory.ok(
                "Permisos obtenidos correctamente",
                service.listar()
        );
    }

    @PreAuthorize("hasAuthority('PERMISO_CREAR')")
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> guardar(
            @Valid @RequestBody PermissionRequest request
    ) {

        return ResponseFactory.created(
                "Permiso creado correctamente",
                service.guardar(request)
        );
    }

}