package ec.planprocons.backend.service.event;

import ec.planprocons.backend.dto.response.RegistroAccesoResponse;

public record AccessCreatedEvent(RegistroAccesoResponse acceso) {
}
