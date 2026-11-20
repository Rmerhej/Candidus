-- ============================================================
-- V1 : schéma initial de Candidus
-- Règle d'or : on ne modifie JAMAIS un fichier de migration déjà appliqué
-- en production. Pour faire évoluer le schéma : V2__..., V3__..., etc.
-- ============================================================

-- Comptes utilisateurs (le mot de passe est haché avec BCrypt, jamais stocké en clair)
CREATE TABLE utilisateurs (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(190)  NOT NULL,
    mot_de_passe  VARCHAR(100)  NOT NULL,
    prenom        VARCHAR(60)   NOT NULL,
    nom           VARCHAR(60)   NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_utilisateurs_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Profil de recherche : un par utilisateur. C'est lui qui rend l'application "adaptable".
CREATE TABLE profils (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    utilisateur_id  BIGINT        NOT NULL,
    metier_vise     VARCHAR(150),
    mots_cles       VARCHAR(200),
    departements    VARCHAR(60),
    cv_resume       VARCHAR(3000),
    points_forts    VARCHAR(1000),
    telephone       VARCHAR(30),
    lien_profil     VARCHAR(255),
    updated_at      DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_profils_utilisateur UNIQUE (utilisateur_id),
    CONSTRAINT fk_profils_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Candidatures : chaque ligne appartient à un seul utilisateur
CREATE TABLE candidatures (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    utilisateur_id    BIGINT        NOT NULL,
    entreprise        VARCHAR(150)  NOT NULL,
    poste             VARCHAR(150),
    offre_url         VARCHAR(500),
    site_web          VARCHAR(255),
    ville             VARCHAR(100),
    code_postal       VARCHAR(10),
    notes             VARCHAR(2000),
    statut            VARCHAR(20)   NOT NULL,
    date_envoi        DATE,
    date_relance      DATE,
    score             INT,
    qualification     VARCHAR(30),

    -- Données publiques de l'API Recherche d'entreprises (vides si saisie à la main)
    siren             VARCHAR(9),
    code_naf          VARCHAR(10),
    tranche_effectif  VARCHAR(2),

    -- Analyse et lettre préparées par l'IA (relues par l'utilisateur avant tout envoi)
    ai_adequation     INT,
    ai_analyse        VARCHAR(2000),
    ai_poste_suggere  VARCHAR(300),
    lettre_objet      VARCHAR(200),
    lettre_corps      VARCHAR(4000),
    analysed_at       DATETIME(6),

    created_at        DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    -- Une même entreprise ne peut être importée qu'une fois par utilisateur
    CONSTRAINT uk_candidatures_utilisateur_siren UNIQUE (utilisateur_id, siren),
    CONSTRAINT fk_candidatures_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs (id) ON DELETE CASCADE,
    INDEX idx_candidatures_relance (utilisateur_id, statut, date_relance)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
