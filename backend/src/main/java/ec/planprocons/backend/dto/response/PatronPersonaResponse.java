package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PatronPersonaResponse {

    private Long personaId;
    private String codigoBiometrico;
    private String nombre;
    private LocalDate desde;
    private LocalDate hasta;
    private String horaHabitualEntrada;
    private String horaHabitualSalida;
    private double promedioAccesosDiarios;
    private long diasAnalizados;
    private long diasConEntrada;
    private boolean informacionSuficiente;
}
