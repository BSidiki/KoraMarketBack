package com.koramarket.auth.mapper;

import com.koramarket.auth.model.User;
import com.koramarket.auth.dto.UserRequestDTO;
import com.koramarket.auth.dto.UserResponseDTO;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {
    public static User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setMotDePasse(dto.getMotDePasse());
        user.setTelephone(dto.getTelephone());
        user.setStatut(dto.getStatut());
        return user;
    }

    public static UserResponseDTO toResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setStatut(user.getStatut());
        dto.setDateInscription(user.getDateInscription());
        dto.setLastLogin(user.getLastLogin());
        if (user.getUserRoles() != null) {
            dto.setRoles(
                    user.getUserRoles()
                            .stream()
                            .map(userRole -> userRole.getRole().getNom())
                            .collect(Collectors.toSet())
            );
        }
        return dto;
    }
}
