package ec.planprocons.backend.entity;

import ec.planprocons.backend.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "dispositivos",
        indexes = {
                @Index(name = "idx_dispositivos_activo", columnList = "activo"),
                @Index(name = "idx_dispositivos_ultimo_contacto", columnList = "ultimo_contacto")
        }
)
public class Dispositivo extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String modelo;

    @Column(nullable = false, unique = true, length = 100)
    private String serial;

    @Column(length = 45)
    private String ip;

    @Column(length = 150)
    private String ubicacion;

    @Column(name = "ultimo_contacto")
    private LocalDateTime ultimoContacto;
}
