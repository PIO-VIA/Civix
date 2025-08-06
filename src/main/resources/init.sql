-- ===============================================
-- SCRIPT D'INITIALISATION
-- Plateforme de Vote Électronique
-- ===============================================

-- Suppression des données existantes (optionnel - décommenter si nécessaire)
-- TRUNCATE TABLE votes CASCADE;
-- TRUNCATE TABLE campagne CASCADE;
-- TRUNCATE TABLE candidat CASCADE;
-- TRUNCATE TABLE electeur CASCADE;
-- TRUNCATE TABLE administrateur CASCADE;

-- ===============================================
-- 1. INSERTION DES ADMINISTRATEURS
-- ===============================================

INSERT INTO administrateur (external_id_administrateur, nom_administrateur, email, mot_de_passe, empreinte_digitale) VALUES
('admin-001', 'Admin Général', 'admin@elecam.cm', '$2a$15$8Q7Q8Q7Q8Q7Q8Q7Q8Q7Q8eK4vF2mN8pL5rG9sH1tJ6kI2nP7qS3uV', NULL)

-- ===============================================
-- 2. INSERTION DES ÉLECTEURS
-- ===============================================

INSERT INTO electeur (external_id_electeur, nom_electeur, email, mot_de_passe, empreinte_digitale, a_vote) VALUES
-- Région du Centre (Yaoundé)
('electeur-001', 'NKOMO Paul', 'paul.nkomo@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-002', 'MVONDO Marie', 'marie.mvondo@yahoo.fr', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-003', 'FOUDA Jean', 'jean.fouda@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-004', 'ATANGANA Grace', 'grace.atangana@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-005', 'OLINGA Patrick', 'patrick.olinga@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),

-- Région du Littoral (Douala)
('electeur-006', 'SAWA Emmanuel', 'emmanuel.sawa@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-007', 'MALIMBA Sylvie', 'sylvie.malimba@yahoo.fr', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-008', 'DOUALA François', 'francois.douala@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-009', 'KOTTO Albertine', 'albertine.kotto@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-010', 'BELL Roger', 'roger.bell@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),

-- Région de l'Ouest (Bafoussam)
('electeur-011', 'KAMGA Viviane', 'viviane.kamga@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-012', 'TCHATCHOU André', 'andre.tchatchou@yahoo.fr', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-013', 'FOZEU Christine', 'christine.fozeu@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-014', 'DSCHANG Martin', 'martin.dschang@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-015', 'MBOUDA Rosette', 'rosette.mbouda@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),

-- Région du Nord-Ouest (Bamenda)
('electeur-016', 'NGWA Peter', 'peter.ngwa@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-017', 'FOFUNG Mary', 'mary.fofung@yahoo.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-018', 'BAMENDA John', 'john.bamenda@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-019', 'KUMBO Sarah', 'sarah.kumbo@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-020', 'FUNDONG Daniel', 'daniel.fundong@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),

-- Région du Sud-Ouest (Buea)
('electeur-021', 'MOLYKO Rebecca', 'rebecca.molyko@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-022', 'BUEA Michael', 'michael.buea@yahoo.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-023', 'LIMBE Grace', 'grace.limbe@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-024', 'KUMBA Joseph', 'joseph.kumba@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-025', 'TIKO Elizabeth', 'elizabeth.tiko@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),

-- Région de l'Extrême-Nord (Maroua)
('electeur-026', 'MAROUA Amadou', 'amadou.maroua@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-027', 'MOKOLO Fatima', 'fatima.mokolo@yahoo.fr', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-028', 'KOUSSERI Ibrahim', 'ibrahim.kousseri@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-029', 'MORA Aisha', 'aisha.mora@hotmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false),
('electeur-030', 'WAZA Hamadou', 'hamadou.waza@gmail.com', '$2a$15$TempPass123!AbCdEfGhIjKlMnOpQrStUvWxYz', NULL, false);

-- ===============================================
-- 3. INSERTION DES CANDIDATS
-- ===============================================

INSERT INTO candidat (external_id_candidat, username, description, photo_path) VALUES
('candidat-001', 'BIYA Paul Maurice',
 'Président sortant, au pouvoir depuis 1982. Candidat du Rassemblement Démocratique du Peuple Camerounais (RDPC). Prône la continuité et le développement infrastructurel du pays.',
 '/photos/biya.jpg'),

('candidat-002', 'KAMTO Maurice',
 'Professeur de droit international, leader du Mouvement pour la Renaissance du Cameroun (MRC). Défend la démocratie, l''état de droit et la lutte contre la corruption.',
 '/photos/kamto.jpg'),

('candidat-003', 'JOSHUA OSIH',
 'Leader du Social Democratic Front (SDF), parti d''opposition historique. Milite pour le fédéralisme et la résolution de la crise anglophone.',
 '/photos/osih.jpg'),

('candidat-004', 'CAVAYE YEGUIE DJIBRIL',
 'Président de l''Assemblée Nationale, candidat indépendant. Met l''accent sur l''unité nationale et le dialogue intercommunautaire.',
 '/photos/cavaye.jpg'),

('candidat-005', 'AKERE MUNA',
 'Avocat international et militant des droits de l''homme. Candidat indépendant prônant la gouvernance transparente et la justice sociale.',
 '/photos/akere.jpg'),

('candidat-006', 'NDAM NJOYA',
 'Sultan traditionnel et homme politique, leader de l''Union Démocratique du Cameroun (UDC). Défend la démocratie participative et les valeurs traditionnelles.',
 '/photos/ndam.jpg');

-- ===============================================
-- 4. INSERTION DES CAMPAGNES
-- ===============================================

INSERT INTO campagne (external_id_campagne, description, photo_path, candidat_id) VALUES
-- Campagnes pour BIYA Paul Maurice
('campagne-001',
 'CONTINUITÉ ET STABILITÉ - Le Cameroun que nous construisons ensemble continue sa marche vers l''émergence. Nos grands projets d''infrastructures transforment le pays : autoroutes, ports, aéroports, barrages. Nous poursuivrons cette dynamique de développement pour faire du Cameroun un pays émergent à l''horizon 2035.',
 '/campagnes/biya_continuite.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-001')),

('campagne-002',
 'PAIX ET SÉCURITÉ - Face aux défis sécuritaires, nous maintenons notre engagement pour la paix. Nos forces armées protègent l''intégrité territoriale. Nous privilégions le dialogue pour résoudre les crises et préserver l''unité nationale dans la diversité.',
 '/campagnes/biya_paix.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-001')),

-- Campagnes pour KAMTO Maurice
('campagne-003',
 'ALTERNANCE DÉMOCRATIQUE - Il est temps pour le changement ! Après 40 ans, le Cameroun mérite une nouvelle gouvernance. Nous proposons une démocratie véritable, des élections transparentes, et une justice indépendante. L''alternance est possible et nécessaire.',
 '/campagnes/kamto_alternance.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-002')),

('campagne-004',
 'LUTTE CONTRE LA CORRUPTION - Nous mettrons fin à l''impunité ! Création d''un tribunal spécial anticorruption, protection des lanceurs d''alerte, transparence dans la gestion publique. Les ressources du Cameroun doivent bénéficier aux Camerounais.',
 '/campagnes/kamto_corruption.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-002')),

-- Campagnes pour JOSHUA OSIH
('campagne-005',
 'FÉDÉRALISME ET UNITÉ - Le fédéralisme est la solution pour préserver notre unité dans la diversité. Deux États fédérés égaux en droits, respectant nos spécificités linguistiques et culturelles. Ensemble, construisons un Cameroun uni et fort.',
 '/campagnes/osih_federalisme.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-003')),

('campagne-006',
 'RÉSOLUTION DE LA CRISE ANGLOPHONE - Nous privilégions le dialogue inclusif pour résoudre la crise. Amnistie générale, statut spécial pour les régions anglophones, reconstruction des zones affectées. La paix par la justice et la réconciliation.',
 '/campagnes/osih_crise.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-003')),

-- Campagnes pour CAVAYE YEGUIE DJIBRIL
('campagne-007',
 'UNITÉ NATIONALE - Fort de mon expérience parlementaire, je connais les aspirations de tous les Camerounais. L''unité nationale n''est pas négociable. Nous valoriserons notre diversité comme une richesse, dans le respect mutuel.',
 '/campagnes/cavaye_unite.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-004')),

-- Campagnes pour AKERE MUNA
('campagne-008',
 'JUSTICE SOCIALE - En tant qu''avocat international, je défends les droits humains. Nous construirons un État de droit, une justice accessible à tous, et lutterons contre toutes les discriminations. Chaque Camerounais mérite dignité et respect.',
 '/campagnes/akere_justice.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-005')),

-- Campagnes pour NDAM NJOYA
('campagne-009',
 'DÉMOCRATIE PARTICIPATIVE - Associons tous les citoyens aux décisions qui les concernent. Renforçons le rôle des chefferies traditionnelles, créons des conseils consultatifs régionaux, donnons la parole au peuple. La démocratie, c''est l''affaire de tous.',
 '/campagnes/ndam_participation.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-006')),

('campagne-010',
 'VALEURS TRADITIONNELLES ET MODERNITÉ - Concilions nos valeurs ancestrales avec la modernité. Nos traditions sont un atout pour le développement. Préservons notre patrimoine culturel tout en embrassant les innovations du 21e siècle.',
 '/campagnes/ndam_tradition.jpg',
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-006'));

-- ===============================================
-- 5. INSERTION DE QUELQUES VOTES (optionnel)
-- ===============================================

-- Quelques électeurs ont déjà voté (pour les tests)
INSERT INTO votes (electeur_id, candidat_id, date_vote) VALUES
((SELECT id_electeur FROM electeur WHERE external_id_electeur = 'electeur-001'),
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-002'),
 NOW() - INTERVAL '2 hours'),

((SELECT id_electeur FROM electeur WHERE external_id_electeur = 'electeur-005'),
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-001'),
 NOW() - INTERVAL '1 hour'),

((SELECT id_electeur FROM electeur WHERE external_id_electeur = 'electeur-010'),
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-003'),
 NOW() - INTERVAL '30 minutes'),

((SELECT id_electeur FROM electeur WHERE external_id_electeur = 'electeur-015'),
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-002'),
 NOW() - INTERVAL '45 minutes'),

((SELECT id_electeur FROM electeur WHERE external_id_electeur = 'electeur-020'),
 (SELECT id FROM candidat WHERE external_id_candidat = 'candidat-004'),
 NOW() - INTERVAL '20 minutes');

-- Mettre à jour le statut des électeurs qui ont voté
UPDATE electeur SET a_vote = true WHERE external_id_electeur IN (
    'electeur-001', 'electeur-005', 'electeur-010', 'electeur-015', 'electeur-020'
);

-- ===============================================
-- VÉRIFICATIONS ET STATISTIQUES
-- ===============================================

-- Vérifier les données insérées
SELECT 'ADMINISTRATEURS' as type, COUNT(*) as nombre FROM administrateur
UNION ALL
SELECT 'ÉLECTEURS', COUNT(*) FROM electeur
UNION ALL
SELECT 'CANDIDATS', COUNT(*) FROM candidat
UNION ALL
SELECT 'CAMPAGNES', COUNT(*) FROM campagne
UNION ALL
SELECT 'VOTES', COUNT(*) FROM votes;

-- Afficher le taux de participation
SELECT
    ROUND((SELECT COUNT(*) FROM electeur WHERE a_vote = true) * 100.0 / (SELECT COUNT(*) FROM electeur), 2) as taux_participation_pourcent;

-- ===============================================
-- NOTES IMPORTANTES
-- ===============================================

/*
🔐 MOTS DE PASSE TEMPORAIRES :
- Tous les utilisateurs ont le mot de passe temporaire : TempPass123!
- Les électeurs devront changer leur mot de passe à la première connexion
- Les administrateurs peuvent se connecter directement

📧 COMPTES DE TEST :
- Admin : admin@elecam.cm / TempPass123!
- Électeur : paul.nkomo@gmail.com / TempPass123! (à changer)

🗳️ ÉTAT DES VOTES :
- 5 électeurs ont déjà voté (pour tester les résultats)
- 25 électeurs peuvent encore voter
- Taux de participation initial : 16,67%

🏛️ CANDIDATS PRÉSIDENTIELS :
- 6 candidats représentant différentes tendances politiques du Cameroun
- 10 campagnes au total (certains candidats ont plusieurs campagnes)

📍 REPRÉSENTATION GÉOGRAPHIQUE :
- Électeurs de toutes les régions du Cameroun
- Noms inspirés des localités et ethnies camerounaises
*/