package ec.planprocons.backend.mapper;

import ec.planprocons.backend.dto.request.DispositivoRequest;
import ec.planprocons.backend.dto.response.DispositivoResponse;
import ec.planprocons.backend.entity.Dispositivo;
import org.springframework.stereotype.Component;

@Component
public class DispositivoMapper {

    public Dispositivo toEntity(DispositivoRequest request) {

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setCodigo(request.getCodigo());
        dispositivo.setNombre(request.getNombre());
        dispositivo.setModelo(request.getModelo());
        dispositivo.setSerial(request.getSerial());
        dispositivo.setIp(request.getIp());
        dispositivo.setUbicacion(request.getUbicacion());
        dispositivo.setUltimoContacto(request.getUltimoContacto());

        return dispositivo;
    }

    public DispositivoResponse toResponse(Dispositivo dispositivo) {

        return DispositivoResponse.builder()
                .id(dispositivo.getId())
                .codigo(dispositivo.getCodigo())
                .nombre(dispositivo.getNombre())
                .modelo(dispositivo.getModelo())
                .serial(dispositivo.getSerial())
                .ip(dispositivo.getIp())
                .ubicacion(dispositivo.getUbicacion())
                .ultimoContacto(dispositivo.getUltimoContacto())
                .activo(dispositivo.getActivo())
                .fechaCreacion(dispositivo.getFechaCreacion())
                .fechaActualizacion(dispositivo.getFechaActualizacion())
                .build();
    }
}
