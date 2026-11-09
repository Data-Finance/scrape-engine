package it.datafinace.scrape_engine.config;

import it.datafinace.scrape_engine.application.ApplicationService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackages = "it.datafinance.scrape_engine.application",
    includeFilters =
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ApplicationService.class))
public class ApplicationServiceConfig {}
