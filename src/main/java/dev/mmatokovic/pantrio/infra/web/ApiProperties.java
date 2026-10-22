package dev.mmatokovic.pantrio.infra.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "pantrio.api")
record ApiProperties(
    String basePath,
    List<String> supportedVersions
) { }
