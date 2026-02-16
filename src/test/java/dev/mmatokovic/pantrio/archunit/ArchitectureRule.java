package dev.mmatokovic.pantrio.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import dev.mmatokovic.pantrio.PantrioApplication;
import jakarta.persistence.Entity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;

@SuppressWarnings("unused")
@AnalyzeClasses(packagesOf = PantrioApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRule {

    @ArchTest
    public static final ArchRule domainLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("dev.mmatokovic.pantrio.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("dev.mmatokovic.pantrio.app..", "dev.mmatokovic.pantrio.infra..", "dev.mmatokovic.pantrio.api..");

    @ArchTest
    public static final ArchRule applicationLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("dev.mmatokovic.pantrio.app..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("dev.mmatokovic.pantrio.api..");

    @ArchTest
    public static final ArchRule infrastructureLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("dev.mmatokovic.pantrio.infra..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("dev.mmatokovic.pantrio.api..");

    @ArchTest
    public static final ArchRule packageStructureRule = ArchRuleDefinition.classes()
        .that(DescribedPredicate.not(resideInAnyPackage("dev.mmatokovic.pantrio", "dev.mmatokovic.pantrio.archunit")))
        .should().resideInAnyPackage("dev.mmatokovic.pantrio.app..", "dev.mmatokovic.pantrio.infra..", "dev.mmatokovic.pantrio.api..", "dev.mmatokovic.pantrio.domain..");

    @ArchTest
    public static final ArchRule implementationPackageRule = ArchRuleDefinition.noClasses()
        .should().resideInAPackage("..impl");

    @ArchTest
    public static final ArchRule utilityPackageRule = ArchRuleDefinition.noClasses()
        .should().resideInAPackage("..utils");

    @ArchTest
    public static final ArchRule controllerPackageRule = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("dev.mmatokovic.pantrio.api..");

    @ArchTest
    public static final ArchRule entityPackageRule = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(Entity.class)
        .should().resideInAPackage("dev.mmatokovic.pantrio.domain..");

}
