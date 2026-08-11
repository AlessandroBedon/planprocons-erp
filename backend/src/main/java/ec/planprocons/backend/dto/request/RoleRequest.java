package ec.planprocons.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data

public class RoleRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50)
    private String nombre;

    @Size(max = 255)
    private String  descripcion;

    private Set<Long> permissionIds = new HashSet<>();


}
