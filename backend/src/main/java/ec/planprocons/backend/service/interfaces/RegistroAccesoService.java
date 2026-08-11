package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.RegistroAccesoRequest;
import ec.planprocons.backend.dto.response.RegistroAccesoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroAccesoService {

    RegistroAccesoResponse registrar(RegistroAccesoRequest request);

    Page<RegistroAccesoResponse> listar(Pageable pageable);

    RegistroAccesoResponse obtenerPorId(Long id);
}
