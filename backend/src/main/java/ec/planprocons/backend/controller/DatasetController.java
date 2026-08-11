package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.DatasetGenerationRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.DatasetGenerationResponse;
import ec.planprocons.backend.service.interfaces.DatasetService;
import ec.planprocons.backend.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService service;

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<DatasetGenerationResponse>> generar(
            @Valid @RequestBody DatasetGenerationRequest request
    ) {

        return ResponseFactory.created(
                "Dataset sintético generado correctamente",
                service.generar(request)
        );
    }
}
