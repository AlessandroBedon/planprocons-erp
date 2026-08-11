package ec.planprocons.backend.mapper;


import ec.planprocons.backend.dto.request.UserRequest;
import ec.planprocons.backend.dto.response.UserResponse;
import ec.planprocons.backend.entity.Role;
import ec.planprocons.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserRequest request){

        User user = new User();

        user.setNombres(request.getNombres());
        user.setApellidos(request.getApellidos());
        user.setCedula(request.getCedula());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setCorreo(request.getCorreo());
        user.setTelefono(request.getTelefono());
        user.setFechaNacimiento(request.getFechaNacimiento());

        return user;
    }

    public UserResponse toResponse(User user){

        return UserResponse.builder()
                .id(user.getId())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .cedula(user.getCedula())
                .usuario(user.getUsername())
                .correo(user.getCorreo())
                .telefono(user.getTelefono())
                .fechaNacimiento(user.getFechaNacimiento())
                .activo(user.getActivo())
                .bloqueado(user.getBloqueado())
                .ultimoLogin(user.getUltimoLogin())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getNombre)
                                .collect(Collectors.toSet())
                )
                .build();
    }

}
