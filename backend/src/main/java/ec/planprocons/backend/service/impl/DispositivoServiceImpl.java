package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.DispositivoRequest;
import ec.planprocons.backend.dto.response.DispositivoResponse;
import ec.planprocons.backend.entity.Dispositivo;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.exception.ResourceNotFoundException;
import ec.planprocons.backend.mapper.DispositivoMapper;
import ec.planprocons.backend.repository.DispositivoRepository;
import ec.planprocons.backend.service.interfaces.DispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispositivoServiceImpl implements DispositivoService {

    private final DispositivoRepository repository;
    private final DispositivoMapper mapper;

    @Override
    @Transactional
    public DispositivoResponse guardar(DispositivoRequest request) {

        if (repository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("El código del dispositivo ya está registrado");
        }

        if (repository.existsBySerial(request.getSerial())) {
            throw new BusinessException("El serial del dispositivo ya está registrado");
        }

        Dispositivo dispositivo = mapper.toEntity(request);

        return mapper.toResponse(repository.save(dispositivo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispositivoResponse> listar() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DispositivoResponse obtenerPorId(Long id) {

        Dispositivo dispositivo = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Dispositivo no encontrado")
                );

        return mapper.toResponse(dispositivo);
    }

    @Override
    @Transactional(readOnly = true)
    public DispositivoResponse obtenerPorCodigo(String codigo) {

        Dispositivo dispositivo = repository.findByCodigo(codigo)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Dispositivo no encontrado")
                );

        return mapper.toResponse(dispositivo);
    }
}
