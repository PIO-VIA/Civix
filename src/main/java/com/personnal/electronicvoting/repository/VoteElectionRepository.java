package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.VoteElection;
import com.personnal.electronicvoting.model.VoteElection.StatutVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteElectionRepository extends JpaRepository<VoteElection, Long> {

    Optional<VoteElection> findByElection_ExternalIdElectionAndElecteur_ExternalIdElecteur(
            String electionId, String electeurId);

    List<VoteElection> findByElection_ExternalIdElection(String electionId);

    List<VoteElection> findByElecteur_ExternalIdElecteur(String electeurId);

    List<VoteElection> findByCandidat_ExternalIdCandidat(String candidatId);

    boolean existsByElection_ExternalIdElectionAndElecteur_ExternalIdElecteur(
            String electionId, String electeurId);

    @Query("SELECT COUNT(v) FROM VoteElection v WHERE v.election.externalIdElection = :electionId AND v.statutVote = :statut")
    Long countVotesParElection(@Param("electionId") String electionId, @Param("statut") StatutVote statut);

    @Query("SELECT v.candidat.externalIdCandidat, COUNT(v) FROM VoteElection v " +
           "WHERE v.election.externalIdElection = :electionId AND v.statutVote = :statut " +
           "GROUP BY v.candidat.externalIdCandidat")
    List<Object[]> countVotesParCandidatPourElection(@Param("electionId") String electionId, @Param("statut") StatutVote statut);

    @Query("SELECT v FROM VoteElection v WHERE v.dateVote BETWEEN :dateDebut AND :dateFin")
    List<VoteElection> findVotesEntre(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT COUNT(v) FROM VoteElection v WHERE v.election.externalIdElection = :electionId AND v.electeur.externalIdElecteur = :electeurId AND v.statutVote = :statut")
    Long countVotesElecteurPourElection(@Param("electionId") String electionId, @Param("electeurId") String electeurId, @Param("statut") StatutVote statut);

    List<VoteElection> findByStatutVote(StatutVote statutVote);

    @Query("SELECT v FROM VoteElection v WHERE v.election.externalIdElection = :electionId ORDER BY v.dateVote DESC")
    List<VoteElection> findVotesParElectionOrderByDateDesc(@Param("electionId") String electionId);
}