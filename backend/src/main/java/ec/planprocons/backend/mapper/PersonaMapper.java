package ec.planprocons.backend.mapper;

import ec.planprocons.backend.dto.request.PersonaRequest;
import ec.planprocons.backend.dto.response.PersonaResponse;
import ec.planprocons.backend.entity.Persona;
import org.springframework.stereotype.Component;

@Component
public class PersonaMapper {

    public Persona toEntity(PersonaRequest request) {

        Persona persona = new Persona();
        persona.setCodigoBiometrico(request.getCodigoBiometrico());
        persona.setCedula(request.getCedula());
        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setDepartamento(request.getDepartamento());
        persona.setCargo(request.getCargo());

        return persona;
    }

    public PersonaResponse toResponse(Persona persona) {

        return PersonaResponse.builder()
                .id(persona.getId())
                .codigoBiometrico(persona.getCodigoBiometrico())
                .cedula(persona.getCedula())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidos())
                .departamento(persona.getDepartamento())
                .cargo(persona.getCargo())
                .activo(persona.getActivo())
                .fechaCreacion(persona.getFechaCreacion())
                .fechaActualizacion(persona.getFechaActualizacion())
                .build();
    }
}
