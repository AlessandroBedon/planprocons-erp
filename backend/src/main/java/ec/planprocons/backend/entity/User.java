package ec.planprocons.backend.entity;

import ec.planprocons.backend.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")

public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 10)
    private String cedula;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column( name ="correo", nullable = false, unique = true, length = 50)
    private String correo;

    @Column(length = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    private LocalDateTime ultimoLogin;

    @Column(nullable = false)
    private Boolean bloqueado=false;

    @Column(nullable = false)
    private Integer intentosFallidos=0;

    @ManyToMany(fetch = FetchType.EAGER)

    @JoinTable(

            name = "user_roles",

            joinColumns = @JoinColumn(name = "user_id"),

            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Set<Role> roles = new HashSet<>();


}
