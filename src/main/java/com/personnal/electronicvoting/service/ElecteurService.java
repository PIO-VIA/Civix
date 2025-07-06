package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.ElecteurDTO;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.repository.ElecteurRepository;
import com.personnal.electronicvoting.mapper.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ElecteurService {
    private final ElecteurRepository electeurRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public List<ElecteurDTO> listerTous() {
        return electeurRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }
    public void marquerCommeAyantVote(String externalId) {
        Electeur electeur = electeurRepository.findByExternalIdElecteur(externalId)
                .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));
        electeur.setAVote(true);
        electeurRepository.save(electeur);
    }

    @Transactional(readOnly = true)
    public Optional<ElecteurDTO> trouverParExternalId(String externalId) {
        return electeurRepository.findByExternalIdElecteur(externalId)
                .map(userMapper::toDTO);
    }


}
