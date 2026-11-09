package it.datafinance.scrape_engine.config;

import it.datafinance.scrape_engine.application.ApplicationService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackageClasses = ApplicationService.class,
    includeFilters =
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ApplicationService.class))
public class ApplicationServiceConfig {}
