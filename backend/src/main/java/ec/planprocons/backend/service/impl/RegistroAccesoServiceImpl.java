package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.RegistroAccesoRequest;
import ec.planprocons.backend.dto.response.RegistroAccesoResponse;
import ec.planprocons.backend.entity.Dispositivo;
import ec.planprocons.backend.entity.Persona;
import ec.planprocons.backend.entity.RegistroAcceso;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.exception.ResourceNotFoundException;
import ec.planprocons.backend.mapper.RegistroAccesoMapper;
import ec.planprocons.backend.repository.DispositivoRepository;
import ec.planprocons.backend.repository.PersonaRepository;
import ec.planprocons.backend.repository.RegistroAccesoRepository;
import ec.planprocons.backend.service.interfaces.RegistroAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistroAccesoServiceImpl implements RegistroAccesoService {

    private final RegistroAccesoRepository repository;
    private final PersonaRepository personaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final RegistroAccesoMapper mapper;

    @Override
    @Transactional
    public RegistroAccesoResponse registrar(RegistroAccesoRequest request) {

        Persona persona = personaRepository.findByCodigoBiometrico(request.getCodigoPersona())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Persona no encontrada")
                );

        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw new BusinessException("La persona está inactiva");
        }

        Dispositivo dispositivo = dispositivoRepository.findByCodigo(request.getCodigoDispositivo())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Dispositivo no encontrado")
                );

        if (!Boolean.TRUE.equals(dispositivo.getActivo())) {
            throw new BusinessException("El dispositivo está inactivo");
        }

        String codigoEvento = normalizarCodigoEvento(request.getCodigoEvento());

        if (codigoEvento != null
                && repository.existsByDispositivoIdAndCodigoEvento(
                        dispositivo.getId(),
                        codigoEvento
                )) {
            throw new BusinessException("El evento ya fue registrado para este dispositivo");
        }

        RegistroAcceso registro = mapper.toEntity(
                request,
                persona,
                dispositivo,
                codigoEvento,
                LocalDateTime.now()
        );

        return mapper.toResponse(repository.save(registro));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAccesoResponse> listar(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroAccesoResponse obtenerPorId(Long id) {

        RegistroAcceso registro = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Registro de acceso no encontrado")
                );

        return mapper.toResponse(registro);
    }

    private String normalizarCodigoEvento(String codigoEvento) {

        return StringUtils.hasText(codigoEvento)
                ? codigoEvento.trim()
                : null;
    }
}
