package com.landgo.userservice.security;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
        return UserPrincipal.create(user);
    }
    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
