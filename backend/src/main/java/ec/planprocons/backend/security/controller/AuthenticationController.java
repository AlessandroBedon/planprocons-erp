package ec.planprocons.backend.security.controller;

import ec.planprocons.backend.security.dto.LoginRequest;
import ec.planprocons.backend.security.dto.LoginResponse;
import ec.planprocons.backend.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        System.out.println("ENTRO AL CONTROLLER");
        System.out.println("USUARIO: " + request.getUsername());

        return ResponseEntity.ok(
                authenticationService.login(request)
        );

    }
}
