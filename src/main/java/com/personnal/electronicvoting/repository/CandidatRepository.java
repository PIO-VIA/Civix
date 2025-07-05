package com.personnal.electronicvoting.repository;

import com.personnal.electronicvoting.model.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {
}
