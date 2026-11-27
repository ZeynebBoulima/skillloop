-- V1 : initialisation du schéma du service Session
CREATE TABLE IF NOT EXISTS service_info (
                                            id BIGSERIAL PRIMARY KEY,
                                            name VARCHAR(100) NOT NULL
    );