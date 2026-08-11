package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder

public class UserResponse {

    private Long id;

    private String nombres;

    private String apellidos;

    private String cedula;

    private String usuario;

    private String correo;

    private String telefono;

    private LocalDate fechaNacimiento;

    private Boolean activo;

    private Boolean bloqueado;

    private LocalDateTime ultimoLogin;

    private Set<String> roles;

}
