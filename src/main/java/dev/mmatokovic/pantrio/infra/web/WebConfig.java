package dev.mmatokovic.pantrio.infra.web;

import dev.mmatokovic.pantrio.PantrioApplication;
import dev.mmatokovic.pantrio.app.job.JobStateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class WebConfig implements WebMvcConfigurer {

    private static final int VERSION_PATH_SEGMENT = 1;

    private final ApiProperties apiProperties;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(apiProperties.basePath() + "/{version}",
            controller -> controller.isAnnotationPresent(RestController.class)
                && controller.getPackageName().startsWith(PantrioApplication.class.getPackageName()));
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.usePathSegment(VERSION_PATH_SEGMENT, path -> path.value().startsWith(apiProperties.basePath() + "/"))
            .addSupportedVersions(apiProperties.supportedVersions().toArray(String[]::new))
            .setVersionRequired(false);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, JobStateEnum.class, state -> JobStateEnum.valueOf(state.trim().toUpperCase(Locale.ROOT)));
    }
}
