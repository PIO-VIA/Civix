package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByElecteur_externalIdElecteur(String externalId);

    int countByCandidat_externalIdCandidat(String candidatExternalId);
}
