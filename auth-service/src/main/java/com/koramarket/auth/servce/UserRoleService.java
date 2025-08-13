package com.koramarket.auth.servce;

import com.koramarket.auth.model.User;
import com.koramarket.auth.dto.UserRoleRequestDTO;
import com.koramarket.auth.model.Role;
import com.koramarket.auth.model.UserRole;
import com.koramarket.auth.repository.RoleRepository;
import com.koramarket.auth.repository.UserRepository;
import com.koramarket.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRole createUserRole(UserRoleRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id " + dto.getUserId()));
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id " + dto.getRoleId()));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        return userRoleRepository.save(userRole);
    }

    public List<UserRole> findAll() {
        return userRoleRepository.findAll();
    }

    public Optional<UserRole> findById(Long id) {
        return userRoleRepository.findById(id);
    }

    public UserRole save(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    public void delete(Long id) {
        userRoleRepository.deleteById(id);
    }
}
