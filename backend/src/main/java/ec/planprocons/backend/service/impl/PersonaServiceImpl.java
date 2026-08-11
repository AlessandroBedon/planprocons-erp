package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.PersonaRequest;
import ec.planprocons.backend.dto.response.PersonaResponse;
import ec.planprocons.backend.entity.Persona;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.exception.ResourceNotFoundException;
import ec.planprocons.backend.mapper.PersonaMapper;
import ec.planprocons.backend.repository.PersonaRepository;
import ec.planprocons.backend.service.interfaces.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository repository;
    private final PersonaMapper mapper;

    @Override
    @Transactional
    public PersonaResponse guardar(PersonaRequest request) {

        if (repository.existsByCodigoBiometrico(request.getCodigoBiometrico())) {
            throw new BusinessException("El código biométrico ya está registrado");
        }

        if (repository.existsByCedula(request.getCedula())) {
            throw new BusinessException("La cédula ya está registrada");
        }

        Persona persona = mapper.toEntity(request);

        return mapper.toResponse(repository.save(persona));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtenerPorId(Long id) {

        Persona persona = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Persona no encontrada")
                );

        return mapper.toResponse(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtenerPorCodigo(String codigo) {

        Persona persona = repository.findByCodigoBiometrico(codigo)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Persona no encontrada")
                );

        return mapper.toResponse(persona);
    }
}
