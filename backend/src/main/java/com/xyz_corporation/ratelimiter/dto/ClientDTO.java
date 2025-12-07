package com.corporation.ratelimiter.dto;

import com.corporation.ratelimiter.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
     //client input validation annotations
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Client name is required")
    private String name;
    
    private String description;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    public Client toEntity() {
        Client client = new Client();
        client.setClientId(this.clientId);
        client.setName(this.name);
        client.setDescription(this.description);
        client.setContactEmail(this.contactEmail);
        return client;
    }
    
    public static ClientDTO fromEntity(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setClientId(client.getClientId());
        dto.setName(client.getName());
        dto.setDescription(client.getDescription());
        dto.setContactEmail(client.getContactEmail());
        return dto;
    }
}

