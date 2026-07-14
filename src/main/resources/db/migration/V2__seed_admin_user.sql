-- Compte administrateur de démonstration pour les tests locaux.
-- Identifiants : admin@enspd.example / ChangeMe123!
-- ATTENTION : à supprimer ou changer impérativement avant tout déploiement réel.
INSERT INTO app_user (email, password_hash, role)
VALUES ('admin@enspd.example', '$2b$10$n/8AF6WH4Wo7TGRyIpV/rudpFiIFFsK2K3z4MfNHSDUHaQuh1fJHG', 'ADMIN');
