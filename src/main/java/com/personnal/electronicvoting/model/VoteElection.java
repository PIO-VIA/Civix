package com.personnal.electronicvoting.model;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes_election", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"election_id", "electeur_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteElection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(optional = false)
    @JoinColumn(name = "electeur_id", nullable = false)
    private Electeur electeur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @Column(name = "date_vote", nullable = false)
    private LocalDateTime dateVote;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_vote", nullable = false)
    @Builder.Default
    private StatutVote statutVote = StatutVote.VALIDE;

    @PrePersist
    public void prePersist() {
        if (this.dateVote == null) {
            this.dateVote = LocalDateTime.now();
        }
    }

    public enum StatutVote {
        VALIDE("Valide"),
        ANNULE("Annulé"),
        EN_ATTENTE_VALIDATION("En attente de validation"),
        REJETE("Rejeté");

        private final String libelle;

        StatutVote(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }
}