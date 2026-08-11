package ec.planprocons.backend.entity;

import ec.planprocons.backend.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "roles")

public class Role extends BaseEntity {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "perimission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

}
