package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.UserRequest;
import ec.planprocons.backend.dto.response.UserResponse;

import java.util.List;

public interface UserServices {

    List<UserResponse> listar();

    UserResponse guardar(UserRequest request);

}
