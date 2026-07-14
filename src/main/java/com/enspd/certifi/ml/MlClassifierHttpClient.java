package com.enspd.certifi.ml;

import com.enspd.certifi.domain.enums.PredictedClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class MlClassifierHttpClient implements MlClassifierClient {

    private final WebClient webClient;
    private final Duration timeout;

    public MlClassifierHttpClient(
        @Value("${certifi.ml-service.base-url}") String baseUrl,
        @Value("${certifi.ml-service.timeout-ms}") long timeoutMs
    ) {
        this.timeout = Duration.ofMillis(timeoutMs);
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(this.timeout);

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Override
    public Optional<ClassificationResult> classify(FeatureVector featureVector) {
        try {
            ClassifyResponse response = webClient.post()
                .uri("/api/v1/classify")
                .bodyValue(toRequestBody(featureVector))
                .retrieve()
                .bodyToMono(ClassifyResponse.class)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(150)))
                .timeout(timeout)
                .block();

            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(toDomainResult(response));

        } catch (Exception e) {
            // Ne JAMAIS propager cette erreur au client : le verdict cryptographique
            // doit rester disponible même si le service ML est en panne.
            log.warn("Service ML indisponible ou en erreur : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HealthResponse health = webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .timeout(Duration.ofMillis(500))
                .block();
            return health != null && "up".equals(health.status());
        } catch (Exception e) {
            return false;
        }
    }

    private ClassifyRequestBody toRequestBody(FeatureVector fv) {
        return new ClassifyRequestBody(
            fv.fileSizeKb(),
            fv.sizeDeltaKb(),
            fv.hashMatch() ? 1 : 0,
            fv.keyMatch() ? 1 : 0,
            fv.signatureValid() ? 1 : 0,
            fv.replaySuspected() ? 1 : 0,
            fv.timeSinceSigningHours()
        );
    }

    private ClassificationResult toDomainResult(ClassifyResponse response) {
        Map<PredictedClass, Double> probabilities = new EnumMap<>(PredictedClass.class);
        probabilities.put(PredictedClass.NORMAL, response.probabilities().normal());
        probabilities.put(PredictedClass.FALSIFICATION, response.probabilities().falsification());
        probabilities.put(PredictedClass.SUBSTITUTION_CLE, response.probabilities().substitutionCle());
        probabilities.put(PredictedClass.REJEU, response.probabilities().rejeu());

        return new ClassificationResult(
            PredictedClass.valueOf(response.predictedClass()),
            probabilities,
            response.modelVersion()
        );
    }

    // --- DTO internes de transport HTTP (isolés du DTO exposé au frontend) ---

    private record ClassifyRequestBody(
        double fileSizeKb,
        double sizeDeltaKb,
        int hashMatch,
        int keyMatch,
        int signatureValid,
        int replaySuspected,
        double timeSinceSigningHours
    ) {
    }

    private record ProbabilitiesBody(
        double NORMAL,
        double FALSIFICATION,
        double SUBSTITUTION_CLE,
        double REJEU
    ) {
        double normal() { return NORMAL; }
        double falsification() { return FALSIFICATION; }
        double substitutionCle() { return SUBSTITUTION_CLE; }
        double rejeu() { return REJEU; }
    }

    private record ClassifyResponse(
        String predictedClass,
        ProbabilitiesBody probabilities,
        String modelVersion
    ) {
    }

    private record HealthResponse(String status, String modelVersion) {
    }
}
