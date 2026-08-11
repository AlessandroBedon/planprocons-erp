package ec.planprocons.backend.security.service;

import ec.planprocons.backend.security.dto.LoginRequest;
import ec.planprocons.backend.security.dto.LoginResponse;
import ec.planprocons.backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        System.out.println("LOGIN RECIBIDO: " + request.getUsername());

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUsername(),
                                    request.getPassword()
                            )
                    );

            System.out.println("USUARIO AUTENTICADO");

            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails);

            System.out.println("TOKEN GENERADO");

            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .build();

        } catch (AuthenticationException e) {

            System.out.println("ERROR AUTENTICACION:");
            System.out.println(e.getClass().getSimpleName());
            System.out.println(e.getMessage());

            throw e;
        }
    }
}