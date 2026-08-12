package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.response.RegistroAccesoResponse;
import ec.planprocons.backend.service.event.AccessCreatedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccessEventStreamService {

    private static final long STREAM_TIMEOUT_MILLIS = 30 * 60 * 1000L;
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    public SseEmitter subscribe() {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        clients.put(clientId, emitter);

        emitter.onCompletion(() -> clients.remove(clientId));
        emitter.onTimeout(() -> removeAndComplete(clientId, emitter));
        emitter.onError(error -> clients.remove(clientId));

        try {
            emitter.send(SseEmitter.event().name("connected").data("CONNECTED"));
        } catch (IOException | IllegalStateException exception) {
            removeAndComplete(clientId, emitter);
        }

        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcastAccessCreated(AccessCreatedEvent event) {
        sendToAll("access-created", event.acceso());
    }

    @Scheduled(fixedRate = 25_000L)
    public void heartbeat() {
        sendToAll("heartbeat", System.currentTimeMillis());
    }

    private void sendToAll(String eventName, Object payload) {
        clients.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException | IllegalStateException exception) {
                removeAndComplete(clientId, emitter);
            }
        });
    }

    private void removeAndComplete(String clientId, SseEmitter emitter) {
        if (clients.remove(clientId, emitter)) {
            try {
                emitter.complete();
            } catch (IllegalStateException ignored) {
                // El contenedor ya cerró la respuesta.
            }
        }
    }
}
