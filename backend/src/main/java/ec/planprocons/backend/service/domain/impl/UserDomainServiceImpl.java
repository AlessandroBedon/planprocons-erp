package ec.planprocons.backend.service.domain.impl;

import ec.planprocons.backend.entity.User;
import ec.planprocons.backend.exception.ResourceNotFoundException;
import ec.planprocons.backend.repository.UserRepository;
import ec.planprocons.backend.service.domain.UserDomainService;
import org.springframework.stereotype.Service;

@Service

public class UserDomainServiceImpl implements UserDomainService {

    private final UserRepository repository;

    public UserDomainServiceImpl(UserRepository repository){

        this.repository = repository;
    }

    @Override
    public User obtenerPorUsuario(String usuario){

        return repository.findByUsername(usuario)

                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );
    }

    @Override
    public User obtenerPorCedula(String cedula){

        return repository.findByCedula(cedula)

                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );
    }

    @Override
    public User obtenerPorCorreo(String correo){

        return repository.findByCorreo(correo)

                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );
    }

}
