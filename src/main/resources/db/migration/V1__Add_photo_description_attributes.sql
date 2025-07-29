-- Migration pour ajouter les attributs photo et description aux entités concernées

-- Ajouter les attributs manquants à la table candidat
ALTER TABLE candidat 
ADD COLUMN IF NOT EXISTS description VARCHAR(5000),
ADD COLUMN IF NOT EXISTS photo_path VARCHAR(255);

-- Ajouter l'attribut photo manquant à la table elections
ALTER TABLE elections 
ADD COLUMN IF NOT EXISTS photo_path VARCHAR(255);

-- Note: La table campagne possède déjà les colonnes description et photo_path
-- Note: La table elections possède déjà la colonne description