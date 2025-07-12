package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.model.Campagne;
import com.personnal.electronicvoting.model.Candidat;
import com.personnal.electronicvoting.repository.*;
import com.personnal.electronicvoting.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CampagneService {

    private final CampagneRepository campagneRepository;
    private final CandidatRepository candidatRepository;
    private final CampagneMapper campagneMapper;
    private final CandidatMapper candidatMapper;

    // ==================== CONSULTATION PUBLIQUE ====================

    /**
     *  Lister toutes les campagnes (vue électeur)
     */
    public List<CampagneDTO> listerToutesCampagnes() {
        log.info(" Consultation publique - Liste de toutes les campagnes");

        List<Campagne> campagnes = campagneRepository.findAll();
        log.info(" {} campagnes trouvées", campagnes.size());

        return campagnes.stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    /**
     * Trouver campagne par ID
     */
    public CampagneDTO trouverCampagneParId(String externalId) {
        log.info(" Recherche campagne: {}", externalId);

        return campagneRepository.findByExternalIdCampagne(externalId)
                .map(campagneMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée: " + externalId));
    }

    /**
     *  Obtenir campagnes d'un candidat spécifique
     */
    public List<CampagneDTO> obtenirCampagnesParCandidat(String candidatId) {
        log.info(" Recherche campagnes du candidat: {}", candidatId);

        // Vérifier que le candidat existe
        candidatRepository.findByExternalIdCandidat(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + candidatId));

        List<Campagne> campagnes = campagneRepository.findByCandidat_ExternalIdCandidat(candidatId);
        log.info(" {} campagnes trouvées pour le candidat {}", campagnes.size(), candidatId);

        return campagnes.stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    // ==================== CONSULTATION ENRICHIE ====================

    /**
     * Obtenir toutes les campagnes avec informations candidat
     */
    public List<CampagneAvecCandidatDTO> obtenirCampagnesAvecCandidats() {
        log.info("Consultation campagnes avec informations candidats");

        List<Campagne> campagnes = campagneRepository.findAll();

        return campagnes.stream()
                .map(campagne -> CampagneAvecCandidatDTO.builder()
                        .campagne(campagneMapper.toDTO(campagne))
                        .candidat(candidatMapper.toDTO(campagne.getCandidat()))
                        .build())
                .toList();
    }

    /**
     * Obtenir campagnes groupées par candidat
     */
    public Map<String, List<CampagneDTO>> obtenirCampagnesGroupeesParCandidat() {
        log.info(" Regroupement des campagnes par candidat");

        List<Campagne> toutesCampagnes = campagneRepository.findAll();

        Map<String, List<CampagneDTO>> campagnesGroupees = toutesCampagnes.stream()
                .collect(Collectors.groupingBy(
                        campagne -> campagne.getCandidat().getExternalIdCandidat(),
                        Collectors.mapping(campagneMapper::toDTO, Collectors.toList())
                ));

        log.info(" Campagnes regroupées pour {} candidats", campagnesGroupees.size());
        return campagnesGroupees;
    }

    /**
     *  Obtenir détails enrichis d'une campagne
     */
    public CampagneDetailDTO obtenirDetailCampagne(String campagneId) {
        log.info(" Consultation détails campagne: {}", campagneId);

        Campagne campagne = campagneRepository.findByExternalIdCampagne(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée: " + campagneId));

        // Récupérer les autres campagnes du même candidat
        List<Campagne> autresCampagnes = campagneRepository
                .findByCandidat_ExternalIdCandidat(campagne.getCandidat().getExternalIdCandidat())
                .stream()
                .filter(c -> !c.getExternalIdCampagne().equals(campagneId))
                .toList();

        return CampagneDetailDTO.builder()
                .campagne(campagneMapper.toDTO(campagne))
                .candidat(candidatMapper.toDTO(campagne.getCandidat()))
                .autresCampagnesCandidat(autresCampagnes.stream()
                        .map(campagneMapper::toDTO)
                        .toList())
                .nombreCampagnesCandidat(autresCampagnes.size() + 1) // +1 pour la campagne actuelle
                .build();
    }

    // ==================== RECHERCHE ET FILTRES ====================

    /**
     *  Rechercher campagnes par mot-clé dans la description
     */
    public List<CampagneDTO> rechercherCampagnesParMotCle(String motCle) {
        log.info(" Recherche campagnes par mot-clé: '{}'", motCle);

        if (motCle == null || motCle.trim().isEmpty()) {
            return listerToutesCampagnes();
        }

        String motCleNormalise = motCle.trim().toLowerCase();

        List<Campagne> campagnesTrouvees = campagneRepository.findAll()
                .stream()
                .filter(campagne ->
                        campagne.getDescription().toLowerCase().contains(motCleNormalise) ||
                                campagne.getCandidat().getUsername().toLowerCase().contains(motCleNormalise))
                .toList();

        log.info(" {} campagnes trouvées pour '{}'", campagnesTrouvees.size(), motCle);

        return campagnesTrouvees.stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    /**
     *  Obtenir campagnes avec photos uniquement
     */
    public List<CampagneDTO> obtenirCampagnesAvecPhotos() {
        log.info(" Recherche campagnes avec photos");

        List<Campagne> campagnesAvecPhotos = campagneRepository.findAll()
                .stream()
                .filter(campagne -> campagne.getPhoto() != null &&
                        !campagne.getPhoto().trim().isEmpty())
                .toList();

        log.info(" {} campagnes avec photos trouvées", campagnesAvecPhotos.size());

        return campagnesAvecPhotos.stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    // ==================== STATISTIQUES ====================

    /**
     *  Obtenir statistiques des campagnes
     */
    public StatistiquesCampagnesDTO obtenirStatistiquesCampagnes() {
        log.info(" Calcul statistiques des campagnes");

        List<Campagne> toutesCampagnes = campagneRepository.findAll();
        long totalCampagnes = toutesCampagnes.size();

        long campagnesAvecPhotos = toutesCampagnes.stream()
                .filter(c -> c.getPhoto() != null && !c.getPhoto().trim().isEmpty())
                .count();

        // Calculer longueur moyenne des descriptions
        double longueurMoyenneDescription = toutesCampagnes.stream()
                .mapToInt(c -> c.getDescription().length())
                .average()
                .orElse(0);

        // Compter candidats avec campagnes
        long candidatsAvecCampagnes = toutesCampagnes.stream()
                .map(c -> c.getCandidat().getExternalIdCandidat())
                .distinct()
                .count();

        return StatistiquesCampagnesDTO.builder()
                .totalCampagnes(totalCampagnes)
                .campagnesAvecPhotos(campagnesAvecPhotos)
                .candidatsAvecCampagnes(candidatsAvecCampagnes)
                .longueurMoyenneDescription(Math.round(longueurMoyenneDescription))
                .tauxCampagnesAvecPhotos(totalCampagnes > 0 ?
                        Math.round((double) campagnesAvecPhotos / totalCampagnes * 100.0) : 0)
                .build();
    }

    /**
     *  Obtenir répartition des campagnes par candidat
     */
    public List<RepartitionCampagnesDTO> obtenirRepartitionParCandidat() {
        log.info(" Calcul répartition campagnes par candidat");

        Map<String, List<CampagneDTO>> repartition = obtenirCampagnesGroupeesParCandidat();

        return repartition.entrySet().stream()
                .map(entry -> {
                    String candidatId = entry.getKey();
                    List<CampagneDTO> campagnes = entry.getValue();

                    // Récupérer le nom du candidat
                    String nomCandidat = candidatRepository.findByExternalIdCandidat(candidatId)
                            .map(Candidat::getUsername)
                            .orElse("Candidat inconnu");

                    return RepartitionCampagnesDTO.builder()
                            .candidatId(candidatId)
                            .nomCandidat(nomCandidat)
                            .nombreCampagnes(campagnes.size())
                            .campagnes(campagnes)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getNombreCampagnes(), a.getNombreCampagnes()))
                .toList();
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     *  DTO pour campagne avec candidat
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampagneAvecCandidatDTO {
        private CampagneDTO campagne;
        private CandidatDTO candidat;
    }

    /**
     * DTO pour détails complets d'une campagne
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampagneDetailDTO {
        private CampagneDTO campagne;
        private CandidatDTO candidat;
        private List<CampagneDTO> autresCampagnesCandidat;
        private int nombreCampagnesCandidat;
    }

    /**
     *  DTO pour statistiques des campagnes
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesCampagnesDTO {
        private long totalCampagnes;
        private long campagnesAvecPhotos;
        private long candidatsAvecCampagnes;
        private long longueurMoyenneDescription;
        private double tauxCampagnesAvecPhotos;
    }

    /**
     *  DTO pour répartition des campagnes par candidat
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RepartitionCampagnesDTO {
        private String candidatId;
        private String nomCandidat;
        private int nombreCampagnes;
        private List<CampagneDTO> campagnes;
    }
}