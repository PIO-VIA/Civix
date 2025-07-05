package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Campagne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CampagneRepository extends JpaRepository<Campagne, Long> {
    Optional<Campagne> findByExternalIdCampagne(String externalId);
    List<Campagne> findByCandidat_ExternalIdCandidat(String candidatId);
}
