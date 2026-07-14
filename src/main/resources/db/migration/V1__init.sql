CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE document (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name              VARCHAR(500) NOT NULL,
    sha256_hash            VARCHAR(64)  NOT NULL,
    public_key_fingerprint VARCHAR(128) NOT NULL,
    signature_file_name    VARCHAR(500) NOT NULL,
    signature_bytes        BYTEA        NOT NULL,
    file_size_kb            NUMERIC(12,2) NOT NULL,
    signed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    signed_by              UUID REFERENCES app_user(id)
);

CREATE INDEX idx_document_hash ON document (sha256_hash);

CREATE TABLE verification_record (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id           UUID REFERENCES document(id),
    file_name             VARCHAR(500) NOT NULL,
    action                VARCHAR(10)  NOT NULL,               -- SIGN | VERIFY
    crypto_verdict        VARCHAR(10),                          -- VALID | INVALID
    hash_match            BOOLEAN,
    key_match             BOOLEAN,
    signature_valid       BOOLEAN,
    replay_suspected      BOOLEAN,
    predicted_class       VARCHAR(30),                          -- NORMAL | FALSIFICATION | SUBSTITUTION_CLE | REJEU
    probability_normal    NUMERIC(6,5),
    probability_falsification NUMERIC(6,5),
    probability_substitution  NUMERIC(6,5),
    probability_rejeu     NUMERIC(6,5),
    file_size_kb           NUMERIC(12,2),
    size_delta_kb          NUMERIC(12,2),
    time_since_signing_hours NUMERIC(12,2),
    ml_service_unavailable BOOLEAN NOT NULL DEFAULT false,
    ml_model_version       VARCHAR(50),
    performed_by           UUID REFERENCES app_user(id),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_verification_document ON verification_record (document_id);
CREATE INDEX idx_verification_created_at ON verification_record (created_at DESC);

CREATE TABLE alert (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id    UUID REFERENCES document(id),
    type           VARCHAR(30) NOT NULL,   -- FALSIFICATION | SUBSTITUTION_CLE | REJEU | ML_SERVICE_DOWN
    message        TEXT NOT NULL,
    acknowledged   BOOLEAN NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_alert_acknowledged ON alert (acknowledged);
