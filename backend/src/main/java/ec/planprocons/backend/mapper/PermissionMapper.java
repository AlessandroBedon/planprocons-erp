package ec.planprocons.backend.mapper;

import ec.planprocons.backend.dto.request.PermissionRequest;
import ec.planprocons.backend.dto.response.PermissionResponse;
import ec.planprocons.backend.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionRequest request) {

        Permission permission = new Permission();

        permission.setNombre(request.getNombre());
        permission.setDescription(request.getDescripcion());

        return permission;

    }

    public PermissionResponse toResponse(Permission permission) {

        return PermissionResponse.builder()
                .id(permission.getId())
                .nombre(permission.getNombre())
                .descripcion(permission.getDescription())
                .build();

    }

    public Set<String> toNameSet(Set<Permission> permissions) {

        return permissions.stream()
                .map(Permission::getNombre)
                .collect(Collectors.toSet());

    }

}