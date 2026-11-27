package com.skillloop.session_service.entity;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum EtatSeance {
    DEMANDEE, ACCEPTEE, CONFIRMEE, EN_COURS,
    TERMINEE, ANNULEE, ABSENCE, LITIGE;

    private static final Map<EtatSeance, Set<EtatSeance>> TRANSITIONS =
            new EnumMap<>(EtatSeance.class);

    static {
        TRANSITIONS.put(DEMANDEE,  EnumSet.of(ACCEPTEE, ANNULEE));
        TRANSITIONS.put(ACCEPTEE,  EnumSet.of(CONFIRMEE, ANNULEE));
        TRANSITIONS.put(CONFIRMEE, EnumSet.of(EN_COURS, ANNULEE, ABSENCE, LITIGE));
        TRANSITIONS.put(EN_COURS,  EnumSet.of(TERMINEE, LITIGE));
        TRANSITIONS.put(TERMINEE,  EnumSet.of(LITIGE));
        TRANSITIONS.put(ABSENCE,   EnumSet.of(LITIGE));
        TRANSITIONS.put(LITIGE,    EnumSet.of(TERMINEE, ANNULEE));
        TRANSITIONS.put(ANNULEE,   EnumSet.noneOf(EtatSeance.class));
    }

    public boolean peutPasserA(EtatSeance cible) {
        return TRANSITIONS.get(this).contains(cible);
    }
}