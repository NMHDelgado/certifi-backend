package com.enspd.certifi.ml;

import java.util.Optional;

/**
 * Abstraction du pont vers le service de classification ML. Décision
 * d'architecture figée (prompt maître, section 1) : le modèle est livré
 * comme une API Python (FastAPI) indépendante, consommée ici en HTTP.
 *
 * Retourne Optional.empty() si le service est indisponible — le code appelant
 * (DocumentVerificationService) doit alors renvoyer un verdict cryptographique
 * seul, avec `mlServiceUnavailable = true`, sans jamais faire échouer la requête.
 */
public interface MlClassifierClient {
    Optional<ClassificationResult> classify(FeatureVector featureVector);
    boolean isAvailable();
}
