package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
    Optional<Administrateur> findByExternalIdAdministrateur(String externalId);
    Optional<Administrateur> findByEmail(String email);
    Optional<Administrateur> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
