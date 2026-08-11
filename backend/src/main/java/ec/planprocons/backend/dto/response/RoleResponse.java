package ec.planprocons.backend.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder

public class RoleResponse {

    private Long id;

    private String nombre;

    private String descripcion;

    private Set<String> permissions;
}
