package com.skillloop.session_service.exception;

import com.skillloop.session_service.entity.EtatSeance;

public class TransitionInvalideException extends RegleMetierException {
    public TransitionInvalideException(EtatSeance actuel, EtatSeance cible) {
        super("Transition impossible : " + actuel + " vers " + cible);
    }
}