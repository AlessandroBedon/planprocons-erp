package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.RoleRequest;
import ec.planprocons.backend.dto.response.RoleResponse;

import java.util.List;

public interface RoleService  {

    List<RoleResponse> listar();

    RoleResponse guardar(RoleRequest request);
}
