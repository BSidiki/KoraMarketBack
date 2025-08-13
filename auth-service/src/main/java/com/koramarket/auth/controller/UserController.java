package com.koramarket.auth.controller;

import com.koramarket.auth.model.User;
import com.koramarket.auth.audit.AuditedAction;
import com.koramarket.auth.dto.UserRequestDTO;
import com.koramarket.auth.dto.UserResponseDTO;
import com.koramarket.auth.mapper.UserMapper;
import com.koramarket.auth.servce.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @AuditedAction("LISTE_USER")
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @AuditedAction("DETAIL_USER")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @AuditedAction("CREATE_USER")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto) {
        User user = UserMapper.toEntity(dto);
        User saved = userService.save(user);
        return ResponseEntity.ok(UserMapper.toResponse(saved));
    }

    @AuditedAction("MISE_A_JOUR_USER")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        return userService.findById(id)
                .map(existing -> {
                    existing.setNom(dto.getNom());
                    existing.setPrenom(dto.getPrenom());
                    existing.setEmail(dto.getEmail());
                    existing.setMotDePasse(dto.getMotDePasse());
                    existing.setTelephone(dto.getTelephone());
                    existing.setStatut(dto.getStatut());
                    User updated = userService.save(existing);
                    return ResponseEntity.ok(UserMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @AuditedAction("DELETE_USER")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.findById(id).isPresent()) {
            userService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
