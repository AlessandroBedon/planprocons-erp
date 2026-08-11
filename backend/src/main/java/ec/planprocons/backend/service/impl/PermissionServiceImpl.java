package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.PermissionRequest;
import ec.planprocons.backend.dto.response.PermissionResponse;
import ec.planprocons.backend.entity.Permission;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.mapper.PermissionMapper;
import ec.planprocons.backend.repository.PermissionRepository;
import ec.planprocons.backend.service.interfaces.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository repository;
    private final PermissionMapper mapper;

    @Override
    public List<PermissionResponse> listar() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

    }

    @Override
    public PermissionResponse guardar(PermissionRequest request) {

        if (repository.findByNombre(request.getNombre()).isPresent()) {
            throw new BusinessException("El permiso ya existe.");
        }

        Permission permission = mapper.toEntity(request);

        permission = repository.save(permission);

        return mapper.toResponse(permission);

    }

}