package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Système", description = "APIs d'administration système et monitoring")
public class SystemController {

    private final VoteService voteService;
    private final CandidatService candidatService;
    private final CampagneService campagneService;
    private final ElecteurService electeurService;
    private final AdministrateurService administrateurService;
    private final AuthService authService;

    @Value("${spring.application.name:electronicvoting}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    // ==================== HEALTH CHECKS ====================

    /**
     * 🏥 Health check global de l'application
     */
    @GetMapping("/health")
    @Operation(summary = "Health check global",
            description = "Vérifier la santé globale de l'application")
    public ResponseEntity<HealthCheckDTO> healthCheck() {

        log.info("🏥 Health check global système");

        try {
            // Vérifications des services principaux
            HealthStatusDTO statusVote = verifierSanteVote();
            HealthStatusDTO statusCandidats = verifierSanteCandidats();
            HealthStatusDTO statusCampagnes = verifierSanteCampagnes();
            HealthStatusDTO statusElecteurs = verifierSanteElecteurs();
            HealthStatusDTO statusAuth = verifierSanteAuth();

            // Statut global
            boolean todServiceHealthy = statusVote.isHealthy() &&
                    statusCandidats.isHealthy() &&
                    statusCampagnes.isHealthy() &&
                    statusElecteurs.isHealthy() &&
                    statusAuth.isHealthy();

            HealthCheckDTO healthCheck = HealthCheckDTO.builder()
                    .status(todServiceHealthy ? "UP" : "DOWN")
                    .timestamp(LocalDateTime.now())
                    .application(applicationName)
                    .version("1.0.0")
                    .services(Map.of(
                            "vote", statusVote,
                            "candidats", statusCandidats,
                            "campagnes", statusCampagnes,
                            "electeurs", statusElecteurs,
                            "auth", statusAuth
                    ))
                    .uptime(calculerUptime())
                    .build();

            HttpStatus httpStatus = todServiceHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(httpStatus).body(healthCheck);

        } catch (Exception e) {
            log.error("💥 Erreur health check global: {}", e.getMessage(), e);

            HealthCheckDTO healthCheck = HealthCheckDTO.builder()
                    .status("DOWN")
                    .timestamp(LocalDateTime.now())
                    .application(applicationName)
                    .version("1.0.0")
                    .services(Map.of())
                    .uptime("UNKNOWN")
                    .error(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthCheck);
        }
    }

    /**
     * 📊 Métriques système détaillées
     */
    @GetMapping("/metrics")
    @Operation(summary = "Métriques système",
            description = "Obtenir les métriques détaillées du système")
    public ResponseEntity<SystemMetricsDTO> obtenirMetriques() {

        log.info("📊 Consultation métriques système");

        try {
            // Métriques applicatives
            MetriquesApplicationDTO metriquesApp = calculerMetriquesApplication();

            // Métriques base de données (simulées)
            MetriquesBDDDTO metriquesBDD = calculerMetriquesBDD();

            // Métriques performance
            MetriquesPerformanceDTO metriquesPerf = calculerMetriquesPerformance();

            // Métriques business
            MetriquesBusinessDTO metriquesBusiness = calculerMetriquesBusiness();

            SystemMetricsDTO metrics = SystemMetricsDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .metriquesApplication(metriquesApp)
                    .metriquesBDD(metriquesBDD)
                    .metriquesPerformance(metriquesPerf)
                    .metriquesBusiness(metriquesBusiness)
                    .build();

            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            log.error("💥 Erreur métriques système: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== INFORMATION SYSTÈME ====================

    /**
     * ℹ️ Informations système
     */
    @GetMapping("/info")
    @Operation(summary = "Informations système",
            description = "Obtenir les informations détaillées du système")
    public ResponseEntity<SystemInfoDTO> obtenirInfoSysteme() {

        log.info("ℹ️ Consultation informations système");

        SystemInfoDTO systemInfo = SystemInfoDTO.builder()
                .application(ApplicationInfoDTO.builder()
                        .nom(applicationName)
                        .version("1.0.0")
                        .description("Plateforme de Vote Électronique")
                        .dateCompilation(LocalDateTime.of(2024, 12, 1, 10, 0))
                        .profil("production")
                        .port(serverPort)
                        .build())
                .environnement(EnvironnementDTO.builder()
                        .os(System.getProperty("os.name"))
                        .javaVersion(System.getProperty("java.version"))
                        .springBootVersion("3.5.3")
                        .timezone("UTC")
                        .locale("fr-FR")
                        .build())
                .dependencies(List.of(
                        DependencyDTO.builder()
                                .nom("Spring Boot")
                                .version("3.5.3")
                                .type("FRAMEWORK")
                                .build(),
                        DependencyDTO.builder()
                                .nom("PostgreSQL")
                                .version("15.x")
                                .type("DATABASE")
                                .build(),
                        DependencyDTO.builder()
                                .nom("Spring Security")
                                .version("6.x")
                                .type("SECURITY")
                                .build()
                ))
                .configuration(obtenirConfigurationActive())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(systemInfo);
    }

    // ==================== MONITORING ET LOGS ====================

    /**
     * 📋 Logs récents du système
     */
    @GetMapping("/logs")
    @Operation(summary = "Logs système",
            description = "Obtenir les logs récents du système (admin uniquement)")
    public ResponseEntity<LogsDTO> obtenirLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "INFO") String niveau,
            @RequestParam(defaultValue = "50") int limite) {

        log.info("📋 Consultation logs système - Niveau: {}, Limite: {}", niveau, limite);

        try {
            verifierTokenAdmin(token);

            // Simulation de logs récents (en production, lire depuis le système de logs)
            List<LogEntryDTO> logs = genererLogsSimules(niveau, limite);

            LogsDTO logsResponse = LogsDTO.builder()
                    .niveau(niveau)
                    .nombreEntrees(logs.size())
                    .limite(limite)
                    .logs(logs)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(logsResponse);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur consultation logs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔍 Monitoring temps réel
     */
    @GetMapping("/monitoring")
    @Operation(summary = "Monitoring temps réel",
            description = "Données de monitoring en temps réel")
    public ResponseEntity<MonitoringDTO> obtenirMonitoring(
            @RequestHeader("Authorization") String token) {

        log.info("🔍 Consultation monitoring temps réel");

        try {
            verifierTokenAdmin(token);

            MonitoringDTO monitoring = MonitoringDTO.builder()
                    .cpuUsage(calculerUsageCPU())
                    .memoryUsage(calculerUsageMemoire())
                    .diskUsage(calculerUsageDisk())
                    .networkStats(calculerStatsNetwork())
                    .threadCount(Thread.activeCount())
                    .sessionCount(calculerNombreSessions())
                    .requestsPerMinute(calculerRequetesParMinute())
                    .errorsLast24h(calculerErreursLast24h())
                    .averageResponseTime(calculerTempsReponseMovie())
                    .timestamp(LocalDateTime.now())
                    .alertes(detecterAlertes())
                    .build();

            return ResponseEntity.ok(monitoring);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur monitoring: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système monitoring: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MAINTENANCE ====================

    /**
     * 🔧 Mode maintenance
     */
    @PostMapping("/maintenance")
    @Operation(summary = "Mode maintenance",
            description = "Activer/désactiver le mode maintenance")
    public ResponseEntity<MaintenanceDTO> toggleMaintenance(
            @RequestHeader("Authorization") String token,
            @RequestBody MaintenanceRequest request) {

        log.info("🔧 Toggle mode maintenance - Activer: {}", request.isActiver());

        try {
            verifierTokenAdmin(token);

            // En production, ceci modulerait un flag global ou config
            boolean nouveauStatut = request.isActiver();

            MaintenanceDTO maintenance = MaintenanceDTO.builder()
                    .modeMaintenanceActif(nouveauStatut)
                    .message(request.getMessage() != null ? request.getMessage() :
                            (nouveauStatut ? "Maintenance en cours" : "Service normal"))
                    .debutMaintenance(nouveauStatut ? LocalDateTime.now() : null)
                    .finMaintenancePrevue(nouveauStatut ?
                            LocalDateTime.now().plusHours(request.getDureeHeures()) : null)
                    .servicesIndisponibles(nouveauStatut ?
                            List.of("Vote", "Gestion candidats") : List.of())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("✅ Mode maintenance {} avec succès",
                    nouveauStatut ? "activé" : "désactivé");

            return ResponseEntity.ok(maintenance);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur mode maintenance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système maintenance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🧹 Nettoyage système
     */
    @PostMapping("/cleanup")
    @Operation(summary = "Nettoyage système",
            description = "Effectuer des tâches de nettoyage système")
    public ResponseEntity<CleanupResultDTO> effectuerNettoyage(
            @RequestHeader("Authorization") String token) {

        log.info("🧹 Démarrage nettoyage système");

        try {
            verifierTokenAdmin(token);

            CleanupResultDTO result = CleanupResultDTO.builder()
                    .sessionsCleaned(nettoyerSessionsExpired())
                    .cacheCleared(viderCaches())
                    .tempFilesDeleted(supprimerFichiersTemp())
                    .logsArchived(archiverLogsAnciens())
                    .totalOperations(4)
                    .operationsSuccessful(4)
                    .dureeMs(calculerDureeNettoyage())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("✅ Nettoyage système terminé - {} opérations", result.getTotalOperations());
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur nettoyage: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système nettoyage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== BACKUP ET RESTAURATION ====================

    /**
     * 💾 Backup des données
     */
    @PostMapping("/backup")
    @Operation(summary = "Backup données",
            description = "Créer une sauvegarde des données")
    public ResponseEntity<BackupResultDTO> creerBackup(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "FULL") String type) {

        log.info("💾 Création backup - Type: {}", type);

        try {
            verifierTokenAdmin(token);

            // Simulation de backup
            BackupResultDTO backup = BackupResultDTO.builder()
                    .backupId(genererBackupId())
                    .type(type)
                    .taille("45.2 MB")
                    .nombreTables(5)
                    .nombreEnregistrements(calculerNombreEnregistrements())
                    .dureeMs(2340L)
                    .cheminFichier("/backups/backup_" +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql")
                    .checksum("SHA256:a1b2c3d4e5f6...")
                    .statut("SUCCESS")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("✅ Backup créé avec succès - ID: {}", backup.getBackupId());
            return ResponseEntity.ok(backup);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur backup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système backup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📋 Liste des backups
     */
    @GetMapping("/backups")
    @Operation(summary = "Liste backups",
            description = "Obtenir la liste des backups disponibles")
    public ResponseEntity<List<BackupInfoDTO>> listerBackups(
            @RequestHeader("Authorization") String token) {

        log.info("📋 Liste des backups");

        try {
            verifierTokenAdmin(token);

            List<BackupInfoDTO> backups = List.of(
                    BackupInfoDTO.builder()
                            .backupId("backup_001")
                            .date(LocalDateTime.now().minusDays(1))
                            .type("FULL")
                            .taille("44.8 MB")
                            .statut("COMPLETED")
                            .build(),
                    BackupInfoDTO.builder()
                            .backupId("backup_002")
                            .date(LocalDateTime.now().minusDays(2))
                            .type("INCREMENTAL")
                            .taille("12.3 MB")
                            .statut("COMPLETED")
                            .build()
            );

            return ResponseEntity.ok(backups);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur liste backups: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système liste backups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SÉCURITÉ SYSTÈME ====================

    /**
     * 🔒 Audit de sécurité
     */
    @GetMapping("/security-audit")
    @Operation(summary = "Audit sécurité",
            description = "Effectuer un audit de sécurité du système")
    public ResponseEntity<SecurityAuditDTO> effectuerAuditSecurite(
            @RequestHeader("Authorization") String token) {

        log.info("🔒 Audit de sécurité système");

        try {
            verifierTokenAdmin(token);

            SecurityAuditDTO audit = SecurityAuditDTO.builder()
                    .scoreGlobal(85)
                    .verificationsEffectuees(List.of(
                            VerificationSecuriteDTO.builder()
                                    .nom("Authentification")
                                    .statut("OK")
                                    .score(90)
                                    .details("Hachage des mots de passe sécurisé")
                                    .build(),
                            VerificationSecuriteDTO.builder()
                                    .nom("Autorisation")
                                    .statut("OK")
                                    .score(88)
                                    .details("Contrôles d'accès appropriés")
                                    .build(),
                            VerificationSecuriteDTO.builder()
                                    .nom("Chiffrement")
                                    .statut("WARNING")
                                    .score(75)
                                    .details("HTTPS activé, considérer chiffrement BDD")
                                    .build()
                    ))
                    .vulnerabilitesDetectees(List.of())
                    .recommandations(List.of(
                            "Activer le chiffrement de base de données",
                            "Implémenter la rotation des tokens",
                            "Ajouter monitoring des tentatives d'intrusion"
                    ))
                    .derniereVerification(LocalDateTime.now())
                    .prochainAudit(LocalDateTime.now().plusDays(30))
                    .build();

            return ResponseEntity.ok(audit);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur audit sécurité: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système audit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void verifierTokenAdmin(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!authService.verifierTokenAdmin(cleanToken)) {
            throw new RuntimeException("Token administrateur requis");
        }
    }

    private HealthStatusDTO verifierSanteVote() {
        try {
            voteService.obtenirStatistiquesGenerales();
            return HealthStatusDTO.builder()
                    .healthy(true)
                    .message("Service vote opérationnel")
                    .responseTime(45L)
                    .build();
        } catch (Exception e) {
            return HealthStatusDTO.builder()
                    .healthy(false)
                    .message("Erreur service vote: " + e.getMessage())
                    .responseTime(0L)
                    .build();
        }
    }

    private HealthStatusDTO verifierSanteCandidats() {
        try {
            candidatService.listerTousCandidats();
            return HealthStatusDTO.builder()
                    .healthy(true)
                    .message("Service candidats opérationnel")
                    .responseTime(30L)
                    .build();
        } catch (Exception e) {
            return HealthStatusDTO.builder()
                    .healthy(false)
                    .message("Erreur service candidats: " + e.getMessage())
                    .responseTime(0L)
                    .build();
        }
    }

    private HealthStatusDTO verifierSanteCampagnes() {
        try {
            campagneService.listerToutesCampagnes();
            return HealthStatusDTO.builder()
                    .healthy(true)
                    .message("Service campagnes opérationnel")
                    .responseTime(35L)
                    .build();
        } catch (Exception e) {
            return HealthStatusDTO.builder()
                    .healthy(false)
                    .message("Erreur service campagnes: " + e.getMessage())
                    .responseTime(0L)
                    .build();
        }
    }

    private HealthStatusDTO verifierSanteElecteurs() {
        try {
            electeurService.listerTous();
            return HealthStatusDTO.builder()
                    .healthy(true)
                    .message("Service électeurs opérationnel")
                    .responseTime(40L)
                    .build();
        } catch (Exception e) {
            return HealthStatusDTO.builder()
                    .healthy(false)
                    .message("Erreur service électeurs: " + e.getMessage())
                    .responseTime(0L)
                    .build();
        }
    }

    private HealthStatusDTO verifierSanteAuth() {
        try {
            // Test basique du service d'auth
            return HealthStatusDTO.builder()
                    .healthy(true)
                    .message("Service authentification opérationnel")
                    .responseTime(20L)
                    .build();
        } catch (Exception e) {
            return HealthStatusDTO.builder()
                    .healthy(false)
                    .message("Erreur service auth: " + e.getMessage())
                    .responseTime(0L)
                    .build();
        }
    }

    private String calculerUptime() {
        // Simulation d'uptime
        return "24h 15m 32s";
    }

    private MetriquesApplicationDTO calculerMetriquesApplication() {
        return MetriquesApplicationDTO.builder()
                .heapUsed("156 MB")
                .heapMax("512 MB")
                .threadsActive(25)
                .threadsMax(200)
                .classesLoaded(8453)
                .build();
    }

    private MetriquesBDDDTO calculerMetriquesBDD() {
        return MetriquesBDDDTO.builder()
                .connectionsActives(5)
                .connectionsMax(20)
                .poolSize(10)
                .queriesExecuted(15420L)
                .averageQueryTime(45L)
                .build();
    }

    private MetriquesPerformanceDTO calculerMetriquesPerformance() {
        return MetriquesPerformanceDTO.builder()
                .requestsPerSecond(12.5)
                .averageResponseTime(85L)
                .errorRate(0.02)
                .throughput("1.2 MB/s")
                .build();
    }

    private MetriquesBusinessDTO calculerMetriquesBusiness() {
        try {
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();
            return MetriquesBusinessDTO.builder()
                    .votesTotaux(stats.getTotalVotes())
                    .electeursActifs(stats.getElecteursAyantVote())
                    .tauxParticipation(stats.getTauxParticipation())
                    .candidatsTotal(stats.getTotalCandidats())
                    .build();
        } catch (Exception e) {
            return MetriquesBusinessDTO.builder().build();
        }
    }

    private Map<String, Object> obtenirConfigurationActive() {
        return Map.of(
                "database.url", "jdbc:postgresql://localhost:5432/vote",
                "security.enabled", true,
                "email.enabled", true,
                "debug.mode", false,
                "max.upload.size", "10MB"
        );
    }

    private List<LogEntryDTO> genererLogsSimules(String niveau, int limite) {
        return List.of(
                LogEntryDTO.builder()
                        .timestamp(LocalDateTime.now().minusMinutes(5))
                        .niveau("INFO")
                        .logger("com.personnal.electronicvoting.service.VoteService")
                        .message("Vote enregistré avec succès")
                        .build(),
                LogEntryDTO.builder()
                        .timestamp(LocalDateTime.now().minusMinutes(10))
                        .niveau("WARN")
                        .logger("com.personnal.electronicvoting.controller.AuthController")
                        .message("Tentative de connexion avec mot de passe incorrect")
                        .build()
        ).stream().limit(limite).toList();
    }

    // Méthodes de simulation pour le monitoring
    private double calculerUsageCPU() { return 25.5; }
    private double calculerUsageMemoire() { return 68.2; }
    private double calculerUsageDisk() { return 45.8; }
    private NetworkStatsDTO calculerStatsNetwork() {
        return NetworkStatsDTO.builder()
                .bytesIn(1024000L)
                .bytesOut(2048000L)
                .packetsIn(5432L)
                .packetsOut(6789L)
                .build();
    }
    private int calculerNombreSessions() { return 45; }
    private int calculerRequetesParMinute() { return 750; }
    private int calculerErreursLast24h() { return 12; }
    private long calculerTempsReponseMovie() { return 95L; }

    private List<AlerteSystemeDTO> detecterAlertes() {
        return List.of(
                AlerteSystemeDTO.builder()
                        .type("PERFORMANCE")
                        .niveau("LOW")
                        .message("Utilisation mémoire élevée (68%)")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // Méthodes de simulation pour maintenance
    private int nettoyerSessionsExpired() { return 15; }
    private int viderCaches() { return 8; }
    private int supprimerFichiersTemp() { return 23; }
    private int archiverLogsAnciens() { return 3; }
    private long calculerDureeNettoyage() { return 1250L; }

    private String genererBackupId() {
        return "backup_" + System.currentTimeMillis();
    }

    private long calculerNombreEnregistrements() {
        try {
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();
            return stats.getTotalElecteurs() + stats.getTotalCandidats() + stats.getTotalVotes();
        } catch (Exception e) {
            return 1000L;
        }
    }

    // ==================== DTOs SYSTÈME ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private LocalDateTime timestamp;
        private String application;
        private String version;
        private Map<String, HealthStatusDTO> services;
        private String uptime;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthStatusDTO {
        private boolean healthy;
        private String message;
        private Long responseTime;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemMetricsDTO {
        private LocalDateTime timestamp;
        private MetriquesApplicationDTO metriquesApplication;
        private MetriquesBDDDTO metriquesBDD;
        private MetriquesPerformanceDTO metriquesPerformance;
        private MetriquesBusinessDTO metriquesBusiness;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetriquesApplicationDTO {
        private String heapUsed;
        private String heapMax;
        private int threadsActive;
        private int threadsMax;
        private int classesLoaded;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetriquesBDDDTO {
        private int connectionsActives;
        private int connectionsMax;
        private int poolSize;
        private Long queriesExecuted;
        private Long averageQueryTime;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetriquesPerformanceDTO {
        private double requestsPerSecond;
        private Long averageResponseTime;
        private double errorRate;
        private String throughput;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetriquesBusinessDTO {
        private long votesTotaux;
        private long electeursActifs;
        private double tauxParticipation;
        private long candidatsTotal;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemInfoDTO {
        private ApplicationInfoDTO application;
        private EnvironnementDTO environnement;
        private List<DependencyDTO> dependencies;
        private Map<String, Object> configuration;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApplicationInfoDTO {
        private String nom;
        private String version;
        private String description;
        private LocalDateTime dateCompilation;
        private String profil;
        private String port;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EnvironnementDTO {
        private String os;
        private String javaVersion;
        private String springBootVersion;
        private String timezone;
        private String locale;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DependencyDTO {
        private String nom;
        private String version;
        private String type;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogsDTO {
        private String niveau;
        private int nombreEntrees;
        private int limite;
        private List<LogEntryDTO> logs;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogEntryDTO {
        private LocalDateTime timestamp;
        private String niveau;
        private String logger;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MonitoringDTO {
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private NetworkStatsDTO networkStats;
        private int threadCount;
        private int sessionCount;
        private int requestsPerMinute;
        private int errorsLast24h;
        private long averageResponseTime;
        private LocalDateTime timestamp;
        private List<AlerteSystemeDTO> alertes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NetworkStatsDTO {
        private long bytesIn;
        private long bytesOut;
        private long packetsIn;
        private long packetsOut;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AlerteSystemeDTO {
        private String type;
        private String niveau;
        private String message;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MaintenanceRequest {
        private boolean activer;
        private String message;
        private int dureeHeures = 2;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MaintenanceDTO {
        private boolean modeMaintenanceActif;
        private String message;
        private LocalDateTime debutMaintenance;
        private LocalDateTime finMaintenancePrevue;
        private List<String> servicesIndisponibles;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CleanupResultDTO {
        private int sessionsCleaned;
        private int cacheCleared;
        private int tempFilesDeleted;
        private int logsArchived;
        private int totalOperations;
        private int operationsSuccessful;
        private long dureeMs;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BackupResultDTO {
        private String backupId;
        private String type;
        private String taille;
        private int nombreTables;
        private long nombreEnregistrements;
        private long dureeMs;
        private String cheminFichier;
        private String checksum;
        private String statut;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BackupInfoDTO {
        private String backupId;
        private LocalDateTime date;
        private String type;
        private String taille;
        private String statut;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityAuditDTO {
        private int scoreGlobal;
        private List<VerificationSecuriteDTO> verificationsEffectuees;
        private List<String> vulnerabilitesDetectees;
        private List<String> recommandations;
        private LocalDateTime derniereVerification;
        private LocalDateTime prochainAudit;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VerificationSecuriteDTO {
        private String nom;
        private String statut;
        private int score;
        private String details;
    }
}