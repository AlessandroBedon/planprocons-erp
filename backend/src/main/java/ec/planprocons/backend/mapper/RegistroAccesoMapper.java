package ec.planprocons.backend.mapper;

import ec.planprocons.backend.dto.request.RegistroAccesoRequest;
import ec.planprocons.backend.dto.response.RegistroAccesoResponse;
import ec.planprocons.backend.entity.Dispositivo;
import ec.planprocons.backend.entity.Persona;
import ec.planprocons.backend.entity.RegistroAcceso;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RegistroAccesoMapper {

    public RegistroAcceso toEntity(
            RegistroAccesoRequest request,
            Persona persona,
            Dispositivo dispositivo,
            String codigoEvento,
            LocalDateTime fechaRecepcion
    ) {

        RegistroAcceso registro = new RegistroAcceso();
        registro.setPersona(persona);
        registro.setDispositivo(dispositivo);
        registro.setFechaHora(request.getFechaHora());
        registro.setTipoEvento(request.getTipoEvento());
        registro.setMetodoVerificacion(request.getMetodoVerificacion());
        registro.setEstado(request.getEstado());
        registro.setCodigoEvento(codigoEvento);
        registro.setFechaRecepcion(fechaRecepcion);

        return registro;
    }

    public RegistroAccesoResponse toResponse(RegistroAcceso registro) {

        Persona persona = registro.getPersona();
        Dispositivo dispositivo = registro.getDispositivo();

        return RegistroAccesoResponse.builder()
                .id(registro.getId())
                .personaId(persona.getId())
                .codigoPersona(persona.getCodigoBiometrico())
                .nombrePersona(persona.getNombres() + " " + persona.getApellidos())
                .dispositivoId(dispositivo.getId())
                .codigoDispositivo(dispositivo.getCodigo())
                .nombreDispositivo(dispositivo.getNombre())
                .fechaHora(registro.getFechaHora())
                .tipoEvento(registro.getTipoEvento())
                .metodoVerificacion(registro.getMetodoVerificacion())
                .estado(registro.getEstado())
                .codigoEvento(registro.getCodigoEvento())
                .fechaRecepcion(registro.getFechaRecepcion())
                .build();
    }
}
