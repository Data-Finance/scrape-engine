package it.datafinance.scrape_engine;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "it.datafinance.scrape_engine",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainIsPure =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..adapter..")
          .allowEmptyShould(true);

  // Application services may only use Spring's transaction annotations (@Transactional)
  @ArchTest
  static final ArchRule applicationOnlyUsesSpringTransactions =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..adapter..")
                  .and(not(resideInAPackage("org.springframework.transaction.annotation.."))))
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule layers =
      onionArchitecture()
          .domainModels("..domain.model..")
          .domainServices("..domain.service..")
          .applicationServices("..application..")
          .adapter("web", "..adapter.in.web..")
          .adapter("persistence", "..adapter.out.persistence..")
          .ignoreDependency(resideInAPackage("..config.."), alwaysTrue())
          .withOptionalLayers(true);
}
