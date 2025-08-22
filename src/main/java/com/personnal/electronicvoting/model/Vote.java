package com.personnal.electronicvoting.model;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"electeur_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "electeur_id")
    private Electeur electeur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    private LocalDate dateVote = LocalDate.now();
}

