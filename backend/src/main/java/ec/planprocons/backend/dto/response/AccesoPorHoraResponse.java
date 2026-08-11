package ec.planprocons.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccesoPorHoraResponse {

    private int hora;
    private long cantidad;
}
