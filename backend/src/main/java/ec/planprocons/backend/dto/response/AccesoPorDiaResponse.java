package ec.planprocons.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AccesoPorDiaResponse {

    private LocalDate fecha;
    private long cantidad;
}
