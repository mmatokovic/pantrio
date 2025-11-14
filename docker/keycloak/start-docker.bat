docker run --name kc ^
    -v ./docker/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json ^
    -e KC_BOOTSTRAP_ADMIN_USERNAME=god ^
    -e KC_BOOTSTRAP_ADMIN_PASSWORD=god ^
    -e JAVA_OPTS_APPEND="-Dkeycloak.profile.feature.upload_scripts=enabled" ^
    -p 8180:8080 ^
    quay.io/keycloak/keycloak start-dev --import-realm
