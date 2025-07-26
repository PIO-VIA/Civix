package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Electeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ElecteurRepository extends JpaRepository<Electeur, Long> {
    Optional<Electeur> findByExternalIdElecteur(String externalId);
    Optional<Electeur> findByEmail(String email);
    Optional<Electeur> findByUsername(String username);
    boolean existsByEmail(String email);
    List<Electeur> findByaVoteTrue();
    List<Electeur> findByExternalIdElecteurIn(Set<String> externalIds);
}
