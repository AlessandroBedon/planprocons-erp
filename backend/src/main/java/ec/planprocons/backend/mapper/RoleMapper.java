package ec.planprocons.backend.mapper;

import ec.planprocons.backend.dto.request.RoleRequest;
import ec.planprocons.backend.dto.response.RoleResponse;
import ec.planprocons.backend.entity.Permission;
import ec.planprocons.backend.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;



@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public Role toEntity(RoleRequest request) {

        Role role = new Role();

        role.setNombre(request.getNombre());
        role.setDescripcion(request.getDescripcion());

        return role;

    }

    public RoleResponse toResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .nombre(role.getNombre())
                .descripcion(role.getDescripcion())
                .permissions(
                        permissionMapper.toNameSet(
                                role.getPermissions()
                        )
                )
                .build();

    }
}