package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.UserRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.UserResponse;
import ec.planprocons.backend.service.interfaces.UserServices;
import ec.planprocons.backend.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServices service;

    @PreAuthorize("hasAuthority('USUARIO_VER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listar() {

        return ResponseFactory.ok(
                "Usuarios obtenidos correctamente",
                service.listar()
        );
    }
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> guardar(
            @Valid @RequestBody UserRequest request
    ) {

        return ResponseFactory.created(
                "Usuario creado correctamente",
                service.guardar(request)
        );
    }
}