-- V2 : tables du service Séance
DROP TABLE IF EXISTS service_info;

CREATE TABLE creneaux (
                          id      BIGSERIAL PRIMARY KEY,
                          debut   TIMESTAMP   NOT NULL,
                          fin     TIMESTAMP   NOT NULL,
                          etat    VARCHAR(20) NOT NULL,
                          CONSTRAINT chk_creneau_horaire CHECK (fin > debut)
);

CREATE TABLE suivis_reservation (
                                    id                   BIGSERIAL PRIMARY KEY,
                                    identifiant_suivi    VARCHAR(64) NOT NULL UNIQUE,
                                    etape_actuelle       VARCHAR(50) NOT NULL,
                                    etat                 VARCHAR(20) NOT NULL,
                                    date_mise_a_jour     TIMESTAMP   NOT NULL
);

CREATE TABLE seances (
                         id                    BIGSERIAL PRIMARY KEY,
                         demandeur_id          BIGINT        NOT NULL,
                         mentor_id             BIGINT        NOT NULL,
                         titre                 VARCHAR(150)  NOT NULL,
                         description           VARCHAR(1000),
                         capacite              INTEGER       NOT NULL DEFAULT 2,
                         lien_visio            VARCHAR(255),
                         etat                  VARCHAR(20)   NOT NULL,
                         date_creation         TIMESTAMP     NOT NULL,
                         creneau_id            BIGINT        NOT NULL UNIQUE REFERENCES creneaux(id),
                         suivi_reservation_id  BIGINT        UNIQUE REFERENCES suivis_reservation(id),
                         CONSTRAINT chk_seance_etat CHECK (etat IN
                                                           ('DEMANDEE','ACCEPTEE','CONFIRMEE','EN_COURS',
                                                            'TERMINEE','ANNULEE','ABSENCE','LITIGE')),
                         CONSTRAINT chk_capacite CHECK (capacite >= 2),
                         CONSTRAINT chk_utilisateurs_differents CHECK (demandeur_id <> mentor_id)
);

CREATE TABLE participants (
                              id              BIGSERIAL PRIMARY KEY,
                              seance_id       BIGINT      NOT NULL REFERENCES seances(id) ON DELETE CASCADE,
                              utilisateur_id  BIGINT      NOT NULL,
                              role            VARCHAR(20) NOT NULL,
                              present         BOOLEAN     NOT NULL DEFAULT FALSE,
                              UNIQUE (seance_id, utilisateur_id)
);

CREATE TABLE places_attente (
                                id              BIGSERIAL PRIMARY KEY,
                                seance_id       BIGINT    NOT NULL REFERENCES seances(id) ON DELETE CASCADE,
                                utilisateur_id  BIGINT    NOT NULL,
                                date_ajout      TIMESTAMP NOT NULL,
                                rang            INTEGER   NOT NULL,
                                UNIQUE (seance_id, utilisateur_id)
);

CREATE TABLE rappels (
                         id                 BIGSERIAL PRIMARY KEY,
                         seance_id          BIGINT      NOT NULL REFERENCES seances(id) ON DELETE CASCADE,
                         date_envoi_prevue  TIMESTAMP   NOT NULL,
                         etat               VARCHAR(20) NOT NULL
);

CREATE INDEX idx_seances_demandeur ON seances(demandeur_id);
CREATE INDEX idx_seances_mentor    ON seances(mentor_id);
CREATE INDEX idx_seances_etat      ON seances(etat);
CREATE INDEX idx_rappels_a_envoyer ON rappels(date_envoi_prevue);