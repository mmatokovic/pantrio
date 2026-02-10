package dev.mmatokovic.pantrio.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import dev.mmatokovic.pantrio.PantrioApplication;
import jakarta.persistence.Entity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;

@SuppressWarnings("unused")
@AnalyzeClasses(packagesOf = PantrioApplication.class)
class ArchitectureRule {

    @ArchTest
    public static final ArchRule domainLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..application..", "..infrastructure..", "..api..");

    @ArchTest
    public static final ArchRule applicationLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..api..");

    @ArchTest
    public static final ArchRule infrastructureLayerRule = ArchRuleDefinition.noClasses()
        .that().resideInAPackage("..infrastructure..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..api..");

    @ArchTest
    public static final ArchRule packageStructureRule = ArchRuleDefinition.classes()
        .that(DescribedPredicate.not(resideInAnyPackage("hr.crosig.integration.einvoicebilling", "hr.crosig.integration.einvoicebilling.archunit")))
        .should().resideInAnyPackage("..application..", "..infrastructure..", "..api..", "..domain..");

    @ArchTest
    public static final ArchRule implementationPackageRule = ArchRuleDefinition.noClasses()
        .should().resideInAPackage("..impl");

    @ArchTest
    public static final ArchRule utilityPackageRule = ArchRuleDefinition.noClasses()
        .should().resideInAPackage("..utils");

    @ArchTest
    public static final ArchRule controllerPackageRule = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("..api..");

    @ArchTest
    public static final ArchRule entityPackageRule = ArchRuleDefinition.classes()
        .that().areAnnotatedWith(Entity.class)
        .should().resideInAPackage("..domain..");

}
