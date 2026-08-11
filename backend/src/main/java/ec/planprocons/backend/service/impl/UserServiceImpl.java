package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.UserRequest;
import ec.planprocons.backend.dto.response.UserResponse;
import ec.planprocons.backend.entity.Role;
import ec.planprocons.backend.entity.User;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.mapper.UserMapper;
import ec.planprocons.backend.repository.RoleRepository;
import ec.planprocons.backend.repository.UserRepository;
import ec.planprocons.backend.service.interfaces.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserServices {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> listar(){

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse guardar(UserRequest request){

        if(repository.findByUsername(request.getUsername()).isPresent()){
            throw new BusinessException("El nombre de usuario ya existe");
        }

        if (repository.findByCedula(request.getCedula()).isPresent()){
            throw new BusinessException("La cédula ya está registrada");
        }

        if (repository.findByCorreo(request.getCorreo()).isPresent()){
            throw new BusinessException("El correo ya está registrado");
        }

        User user = mapper.toEntity(request);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user .setRoles(
                new HashSet<Role>(
                        roleRepository.findAllById(
                                request.getRoleIds()
                        )
                )
        );

        user = repository.save(user);

        return mapper.toResponse(user);
    }
}
