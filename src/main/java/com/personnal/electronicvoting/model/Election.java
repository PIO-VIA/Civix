package com.personnal.electronicvoting.model;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Table(name = "elections")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Election {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_election")
    private Long idElection;

    @NotBlank
    @Column(name = "external_id_election", unique = true, nullable = false, updatable = false)
    private String externalIdElection;

    @NotBlank(message = "Le titre de l'élection est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description", length = 5000)
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "date_debut_validite")
    private LocalDateTime dateDebutValidite;

    @Column(name = "date_fin_validite")
    private LocalDateTime dateFinValidite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutElection statut = StatutElection.PLANIFIEE;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(optional = false)
    @JoinColumn(name = "administrateur_id", nullable = false)
    private Administrateur administrateur;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "election_electeurs_autorises",
        joinColumns = @JoinColumn(name = "election_id"),
        inverseJoinColumns = @JoinColumn(name = "electeur_id")
    )
    @Builder.Default
    private Set<Electeur> electeursAutorises = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "election_candidats",
        joinColumns = @JoinColumn(name = "election_id"),
        inverseJoinColumns = @JoinColumn(name = "candidat_id")
    )
    @Builder.Default
    private Set<Candidat> candidats = new HashSet<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VoteElection> votes = new HashSet<>();

    @Column(name = "autoriser_vote_multiple")
    @Builder.Default
    private Boolean autoriserVoteMultiple = false;

    @Column(name = "nombre_max_votes_par_electeur")
    @Builder.Default
    private Integer nombreMaxVotesParElecteur = 1;

    @Column(name = "resultats_visibles")
    @Builder.Default
    private Boolean resultatsVisibles = false;

    @PrePersist
    public void prePersist() {
        if (this.externalIdElection == null) {
            this.externalIdElection = UUID.randomUUID().toString();
        }
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public boolean estActive() {
        LocalDateTime maintenant = LocalDateTime.now();
        return this.statut == StatutElection.EN_COURS &&
               maintenant.isAfter(this.dateDebut) &&
               maintenant.isBefore(this.dateFin);
    }

    public boolean estDansLaPeriodeDeValidite() {
        if (dateDebutValidite == null && dateFinValidite == null) {
            return true;
        }
        
        LocalDateTime maintenant = LocalDateTime.now();
        
        if (dateDebutValidite != null && maintenant.isBefore(dateDebutValidite)) {
            return false;
        }
        
        if (dateFinValidite != null && maintenant.isAfter(dateFinValidite)) {
            return false;
        }
        
        return true;
    }

    public boolean electeurEstAutorise(String electeurId) {
        return this.electeursAutorises.stream()
                .anyMatch(electeur -> electeur.getExternalIdElecteur().equals(electeurId));
    }

    public boolean candidatEstParticipant(String candidatId) {
        return this.candidats.stream()
                .anyMatch(candidat -> candidat.getExternalIdCandidat().equals(candidatId));
    }

    public enum StatutElection {
        PLANIFIEE("Planifiée"),
        OUVERTE("Ouverte aux inscriptions"),
        EN_COURS("En cours"),
        TERMINEE("Terminée"),
        ANNULEE("Annulée"),
        RESULTATS_PUBLIES("Résultats publiés");

        private final String libelle;

        StatutElection(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }
}