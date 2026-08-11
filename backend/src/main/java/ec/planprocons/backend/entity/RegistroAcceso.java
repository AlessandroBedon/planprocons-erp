package ec.planprocons.backend.entity;

import ec.planprocons.backend.entity.common.BaseEntity;
import ec.planprocons.backend.entity.enums.EstadoAcceso;
import ec.planprocons.backend.entity.enums.MetodoVerificacion;
import ec.planprocons.backend.entity.enums.TipoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "registros_acceso",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_registro_dispositivo_evento",
                        columnNames = {"dispositivo_id", "codigo_evento"}
                )
        },
        indexes = {
                @Index(name = "idx_registros_fecha_hora", columnList = "fecha_hora"),
                @Index(name = "idx_registros_persona_fecha", columnList = "persona_id, fecha_hora"),
                @Index(name = "idx_registros_dispositivo_fecha", columnList = "dispositivo_id, fecha_hora"),
                @Index(name = "idx_registros_tipo_fecha", columnList = "tipo_evento, fecha_hora"),
                @Index(name = "idx_registros_estado_fecha", columnList = "estado, fecha_hora")
        }
)
public class RegistroAcceso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_verificacion", nullable = false, length = 20)
    private MetodoVerificacion metodoVerificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAcceso estado;

    @Column(name = "codigo_evento", length = 100)
    private String codigoEvento;

    @Column(name = "fecha_recepcion", nullable = false, updatable = false)
    private LocalDateTime fechaRecepcion;
}
