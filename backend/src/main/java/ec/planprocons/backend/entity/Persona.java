package ec.planprocons.backend.entity;

import ec.planprocons.backend.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "personas",
        indexes = {
                @Index(name = "idx_personas_apellidos", columnList = "apellidos"),
                @Index(name = "idx_personas_activo", columnList = "activo")
        }
)
public class Persona extends BaseEntity {

    @Column(name = "codigo_biometrico", nullable = false, unique = true, length = 50)
    private String codigoBiometrico;

    @Column(nullable = false, unique = true, length = 10)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String cargo;
}
