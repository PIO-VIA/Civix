package com.personnal.electronicvoting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🗳️ Plateforme de Vote Électronique - API")
                        .description("""
                                ## 📋 Documentation API Complète
                                
                                Cette API permet de gérer une plateforme de vote électronique sécurisée avec les fonctionnalités suivantes :
                                
                                ### 🎯 Fonctionnalités Principales
                                - **Vote sécurisé** : Un vote unique par électeur avec vérifications
                                - **Gestion des candidats** : CRUD complet des candidats et leurs campagnes
                                - **Administration** : Interface complète pour les administrateurs
                                - **Résultats temps réel** : Consultation des résultats en direct
                                - **Rapports avancés** : Génération de rapports détaillés
                                - **Monitoring** : Surveillance système complète
                                
                                ### 🔐 Authentification
                                L'API utilise un système de tokens Bearer pour l'authentification :
                                - **Électeurs** : Token électeur pour voter et consulter
                                - **Administrateurs** : Token admin pour la gestion complète
                                - **Public** : Accès libre pour consultation générale
                                
                                ### 📊 Types d'Utilisateurs
                                1. **👥 Électeurs** : Votent et consultent les résultats
                                2. **👨‍💼 Administrateurs** : Gèrent la plateforme
                                3. **🌍 Public** : Consulte candidats, campagnes et résultats
                                
                                ### 🚀 Démarrage Rapide
                                1. **Authentification** : `POST /api/auth/electeur/login` ou `/admin/login`
                                2. **Explorer** : `GET /api/public/accueil` pour commencer
                                3. **Voter** : `POST /api/votes/effectuer` (électeurs authentifiés)
                                4. **Administrer** : `GET /api/admin/dashboard` (admins)
                                
                                ###  Réponses API
                                Toutes les réponses suivent un format JSON standard avec gestion d'erreurs appropriée.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe Technique")
                                .email("piodjiele@gmail.com")
                                .url("https://github.com/PIO-VIA/Civix.git"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description(" Serveur de Développement"),
                        new Server()
                                .url("https://civix-tqg1.onrender.com")
                                .description(" Serveur de Production")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token d'authentification Bearer (électeur ou admin)")))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Authentification")
                        .description("🔐 Connexion, déconnexion et gestion des sessions"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Administration")
                        .description("👨‍💼 Gestion administrative (électeurs, candidats, campagnes)"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Vote")
                        .description("🗳️ Processus de vote et consultation des résultats"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Électeur")
                        .description("👥 Interface dédiée aux électeurs"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Candidats (Public)")
                        .description("🏆 Consultation publique des candidats"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Campagnes (Public)")
                        .description("📢 Consultation publique des campagnes"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Public")
                        .description("🌍 APIs publiques générales"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Tableaux de bord")
                        .description("📊 Dashboards personnalisés par rôle"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Rapports")
                        .description("📑 Génération et export de rapports"))
                .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name("Système")
                        .description("🔧 Monitoring, maintenance et administration système"));
    }
}
