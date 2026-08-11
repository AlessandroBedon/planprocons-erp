package ec.planprocons.backend.config;

import ec.planprocons.backend.entity.Permission;
import ec.planprocons.backend.entity.Role;
import ec.planprocons.backend.entity.User;
import ec.planprocons.backend.repository.PermissionRepository;
import ec.planprocons.backend.repository.RoleRepository;
import ec.planprocons.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner cargarDatosIniciales() {

        return args -> {

            Permission usuarioVer =
                    crearPermisoSiNoExiste(
                            "USUARIO_VER",
                            "Permite consultar usuarios"
                    );

            Permission usuarioCrear =
                    crearPermisoSiNoExiste(
                            "USUARIO_CREAR",
                            "Permite crear usuarios"
                    );

            Permission rolVer =
                    crearPermisoSiNoExiste(
                            "ROL_VER",
                            "Permite consultar roles"
                    );

            Permission rolCrear =
                    crearPermisoSiNoExiste(
                            "ROL_CREAR",
                            "Permite crear roles"
                    );

            Permission permisoVer =
                    crearPermisoSiNoExiste(
                            "PERMISO_VER",
                            "Permite consultar permisos"
                    );

            Permission permisoCrear =
                    crearPermisoSiNoExiste(
                            "PERMISO_CREAR",
                            "Permite crear permisos"
                    );

            Role adminRole = roleRepository.findByNombre("ADMIN")
                    .orElseGet(() -> {

                        Role role = new Role();
                        role.setNombre("ADMIN");
                        role.setDescripcion(
                                "Administrador general del sistema"
                        );

                        return roleRepository.save(role);
                    });

            adminRole.getPermissions().addAll(
                    List.of(
                            usuarioVer,
                            usuarioCrear,
                            rolVer,
                            rolCrear,
                            permisoVer,
                            permisoCrear
                    )
            );

            adminRole = roleRepository.save(adminRole);

            if (userRepository.findByUsername("diego").isEmpty()) {

                User user = new User();

                user.setNombres("Diego");
                user.setApellidos("Bedón");
                user.setCedula("1723456789");
                user.setUsername("diego");

                user.setPassword(
                        passwordEncoder.encode("Admin123*")
                );

                user.setCorreo("diego@planprocons.com");
                user.setTelefono("0999999999");
                user.setBloqueado(false);

                user.setRoles(new HashSet<>());
                user.getRoles().add(adminRole);

                userRepository.save(user);
            }
        };
    }

    private Permission crearPermisoSiNoExiste(
            String nombre,
            String descripcion
    ) {

        return permissionRepository.findByNombre(nombre)
                .orElseGet(() -> {

                    Permission permission = new Permission();

                    permission.setNombre(nombre);
                    permission.setDescription(descripcion);

                    return permissionRepository.save(permission);
                });
    }
}