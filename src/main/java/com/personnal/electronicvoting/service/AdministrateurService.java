package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.repository.ElecteurRepository;
import com.personnal.electronicvoting.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdministrateurService {

    private final ElecteurRepository electeurRepository;

    public void deleteElecteur(String externalId) {
        Electeur electeur = electeurRepository.findByExternalIdElecteur(externalId)
                .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));
        electeurRepository.delete(electeur);
    }
}
