package com.corporation.ratelimiter.repository;

import com.corporation.ratelimiter.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}

