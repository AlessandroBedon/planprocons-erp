package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.PersonaRequest;
import ec.planprocons.backend.dto.response.PersonaResponse;

import java.util.List;

public interface PersonaService {

    PersonaResponse guardar(PersonaRequest request);

    List<PersonaResponse> listar();

    PersonaResponse obtenerPorId(Long id);

    PersonaResponse obtenerPorCodigo(String codigo);
}
