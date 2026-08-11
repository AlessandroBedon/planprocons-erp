package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.PermissionRequest;
import ec.planprocons.backend.dto.response.PermissionResponse;
import ec.planprocons.backend.entity.Permission;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> listar();

    PermissionResponse guardar(PermissionRequest request);
}
