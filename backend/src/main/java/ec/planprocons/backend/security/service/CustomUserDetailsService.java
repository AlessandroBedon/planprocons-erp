package ec.planprocons.backend.security.service;

import ec.planprocons.backend.entity.User;
import ec.planprocons.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado.")
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                                .stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(permission ->
                                        new SimpleGrantedAuthority(permission.getNombre())
                                )
                                .collect(Collectors.toSet())
                )
                    .accountLocked(user.getBloqueado())
                    .disabled(!user.getActivo())
                .build();
    }
}