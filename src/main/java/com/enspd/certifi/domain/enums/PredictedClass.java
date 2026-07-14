package com.enspd.certifi.domain.enums;

/** Les 4 classes de sortie du modèle ML (cf. mémoire, Tableau 2.3). */
public enum PredictedClass {
    NORMAL,
    FALSIFICATION,
    SUBSTITUTION_CLE,
    REJEU
}
