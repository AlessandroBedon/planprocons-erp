package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class PermissionResponse {

    private Long id;

    private String nombre;

    private String descripcion;

}
