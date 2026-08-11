package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.DispositivoRequest;
import ec.planprocons.backend.dto.response.DispositivoResponse;

import java.util.List;

public interface DispositivoService {

    DispositivoResponse guardar(DispositivoRequest request);

    List<DispositivoResponse> listar();

    DispositivoResponse obtenerPorId(Long id);

    DispositivoResponse obtenerPorCodigo(String codigo);
}
