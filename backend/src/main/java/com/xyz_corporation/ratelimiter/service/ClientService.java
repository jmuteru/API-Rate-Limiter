package com.corporation.ratelimiter.service;

import com.corporation.ratelimiter.model.Client;
import com.corporation.ratelimiter.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    
    private final ClientRepository repository;
    
    public Client createClient(Client client) {
        if (repository.existsByClientId(client.getClientId())) {
            throw new IllegalArgumentException("Client with ID already exists: " + client.getClientId());
        }
        return repository.save(client);
    }
    
    public Optional<Client> getClient(String clientId) {
        return repository.findByClientId(clientId);
    }
    
    public List<Client> getAllClients() {
        return repository.findAll();
    }
    
    @Transactional
    public Client updateClient(String clientId, Client updatedClient) {
        Client existing = repository.findByClientId(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
        
        // don't allow changing clientId
        existing.setName(updatedClient.getName());
        existing.setDescription(updatedClient.getDescription());
        existing.setContactEmail(updatedClient.getContactEmail());
        
        return repository.save(existing);
    }
    
    public void deleteClient(String clientId) {
        repository.findByClientId(clientId)
            .ifPresent(repository::delete);
    }
}

