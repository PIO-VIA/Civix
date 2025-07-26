package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Election;
import com.personnal.electronicvoting.model.Election.StatutElection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {

    Optional<Election> findByExternalIdElection(String externalIdElection);

    List<Election> findByStatut(StatutElection statut);

    List<Election> findByAdministrateur_ExternalIdAdministrateur(String administrateurId);

    @Query("SELECT e FROM Election e WHERE e.dateDebut <= :maintenant AND e.dateFin >= :maintenant AND e.statut = :statut")
    List<Election> findElectionsActives(@Param("maintenant") LocalDateTime maintenant, @Param("statut") StatutElection statut);

    @Query("SELECT e FROM Election e WHERE e.dateDebut > :maintenant AND e.statut = :statut")
    List<Election> findElectionsFutures(@Param("maintenant") LocalDateTime maintenant, @Param("statut") StatutElection statut);

    @Query("SELECT e FROM Election e WHERE e.dateFin < :maintenant AND e.statut = :statut")
    List<Election> findElectionsTerminees(@Param("maintenant") LocalDateTime maintenant, @Param("statut") StatutElection statut);

    @Query("SELECT e FROM Election e JOIN e.electeursAutorises ea WHERE ea.externalIdElecteur = :electeurId")
    List<Election> findElectionsPourElecteur(@Param("electeurId") String electeurId);

    @Query("SELECT e FROM Election e JOIN e.candidats c WHERE c.externalIdCandidat = :candidatId")
    List<Election> findElectionsPourCandidat(@Param("candidatId") String candidatId);

    @Query("SELECT e FROM Election e WHERE " +
           "LOWER(e.titre) LIKE LOWER(CONCAT('%', :motCle, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    List<Election> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("motCle") String motCle);

    @Query("SELECT COUNT(e) FROM Election e WHERE e.statut = :statut")
    Long countByStatut(@Param("statut") StatutElection statut);

    @Query("SELECT e FROM Election e WHERE e.dateCreation BETWEEN :dateDebut AND :dateFin")
    List<Election> findElectionsCreeesEntre(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT DISTINCT e FROM Election e JOIN e.electeursAutorises ea WHERE ea.externalIdElecteur = :electeurId AND e.statut = :statut")
    List<Election> findElectionsDisponiblesPourElecteur(@Param("electeurId") String electeurId, @Param("statut") StatutElection statut);

    boolean existsByExternalIdElection(String externalIdElection);
}