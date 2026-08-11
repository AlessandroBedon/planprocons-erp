package ec.planprocons.backend.service.domain;

import ec.planprocons.backend.entity.User;

public interface UserDomainService {

    User obtenerPorUsuario(String usuario);

    User obtenerPorCedula(String cedula);

    User obtenerPorCorreo(String correo);
}
