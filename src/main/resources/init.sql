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


-- ===============================================
-- VÉRIFICATIONS ET STATISTIQUES
-- ===============================================

-- Vérifier les données insérées

SELECT 'CANDIDATS', COUNT(*) FROM candidat
UNION ALL
SELECT 'CAMPAGNES', COUNT(*) FROM campagne
UNION ALL

-- Afficher le taux de participation
SELECT
    ROUND((SELECT COUNT(*) FROM electeur WHERE a_vote = true) * 100.0 / (SELECT COUNT(*) FROM electeur), 2) as taux_participation_pourcent;

-- ===============================================
-- NOTES IMPORTANTES
-- ===============================================

