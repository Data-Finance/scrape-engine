package it.datafinace.scrape_engine.config;

import it.datafinace.scrape_engine.domain.DomainService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackages = "it.datafinance.scrape_engine.domain",
    includeFilters =
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = DomainService.class))
public class DomainServiceConfig {}
