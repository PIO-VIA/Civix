package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Candidat;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

import java.util.*;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByExternalIdCandidat(String externalId);
    Optional<Candidat> findByUsername(String nom);
    boolean existsByUsername(String nom);
    List<Candidat> findByUsernameContaining(String nomPart);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.candidat.externalIdCandidat = :candidatId")
    long countVotesByCandidat(@Param("candidatId") String candidatId);

    @Query("SELECT c FROM Candidat c LEFT JOIN c.votes v GROUP BY c ORDER BY COUNT(v) DESC")
    List<Candidat> findAllOrderByVoteCountDesc();
    
    List<Candidat> findByExternalIdCandidatIn(Set<String> externalIds);
}
