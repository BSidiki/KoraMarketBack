package com.koramarket.auth.servce;

import com.koramarket.auth.model.User;
import com.koramarket.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmailWithRolesAndPerms(String email) {
        return userRepository.findByEmailWithRolesAndPerms(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
