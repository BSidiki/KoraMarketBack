package com.koramarket.auth.controller;

import com.koramarket.auth.servce.OauthClientService;
import com.koramarket.auth.dto.OauthClientRequestDTO;
import com.koramarket.auth.dto.OauthClientResponseDTO;
import com.koramarket.auth.mapper.OauthClientMapper;
import com.koramarket.auth.model.OauthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/oauth-clients")
@RequiredArgsConstructor
public class OauthClientController {

    private final OauthClientService oauthClientService;

    @GetMapping
    public List<OauthClientResponseDTO> getAllOauthClients() {
        return oauthClientService.findAll().stream()
                .map(OauthClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OauthClientResponseDTO> getOauthClientById(@PathVariable Long id) {
        return oauthClientService.findById(id)
                .map(OauthClientMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OauthClientResponseDTO> createOauthClient(@RequestBody OauthClientRequestDTO dto) {
        OauthClient client = OauthClientMapper.toEntity(dto);
        OauthClient saved = oauthClientService.save(client);
        return ResponseEntity.ok(OauthClientMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOauthClient(@PathVariable Long id) {
        if (oauthClientService.findById(id).isPresent()) {
            oauthClientService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
