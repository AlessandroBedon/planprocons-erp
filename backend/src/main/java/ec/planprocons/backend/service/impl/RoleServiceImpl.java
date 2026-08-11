package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.RoleRequest;
import ec.planprocons.backend.dto.response.RoleResponse;
import ec.planprocons.backend.entity.Role;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.mapper.RoleMapper;
import ec.planprocons.backend.repository.PermissionRepository;
import ec.planprocons.backend.repository.RoleRepository;
import ec.planprocons.backend.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper mapper;

    @Override
    public List<RoleResponse> listar() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

    }

    @Override
    public RoleResponse guardar(RoleRequest request) {

        if (repository.findByNombre(request.getNombre()).isPresent()) {
            throw new BusinessException("Ya existe un rol con ese nombre.");
        }

        Role role = mapper.toEntity(request);

        role.setPermissions(
                new HashSet<>(
                        permissionRepository.findAllById(
                                request.getPermissionIds()
                        )
                )
        );

        role = repository.save(role);

        return mapper.toResponse(role);

    }

}