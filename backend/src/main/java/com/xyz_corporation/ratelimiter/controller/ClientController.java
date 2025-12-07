package com.corporation.ratelimiter.controller;

import com.corporation.ratelimiter.dto.ClientDTO;
import com.corporation.ratelimiter.model.Client;
import com.corporation.ratelimiter.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO dto) {
        try {
            Client client = clientService.createClient(dto.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED).body(ClientDTO.fromEntity(client));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage(), "status", HttpStatus.CONFLICT.value()));
        }
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable String clientId) {
        return clientService.getClient(clientId)
            .map(client -> ResponseEntity.ok(ClientDTO.fromEntity(client)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = clientService.getAllClients().stream()
            .map(ClientDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }
    
    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody ClientDTO dto) {
        try {
            Client client = clientService.updateClient(clientId, dto.toEntity());
            return ResponseEntity.ok(ClientDTO.fromEntity(client));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage(), "status", HttpStatus.NOT_FOUND.value()));
        }
    }
    
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }
}

