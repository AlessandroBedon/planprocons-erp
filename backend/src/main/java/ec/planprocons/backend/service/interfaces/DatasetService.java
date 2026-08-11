package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.request.DatasetGenerationRequest;
import ec.planprocons.backend.dto.response.DatasetGenerationResponse;

public interface DatasetService {

    DatasetGenerationResponse generar(DatasetGenerationRequest request);
}
