package dev.mmatokovic.pantrio.archunit;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import dev.mmatokovic.pantrio.PantrioApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@AnalyzeClasses(packagesOf = PantrioApplication.class)
class AnnotationRule {

    @ArchTest
    public static final ArchRule repositoryAnnotationRule = ArchRuleDefinition.classes()
        .that().haveSimpleNameEndingWith("Repository").and().areAssignableTo(JpaRepository.class)
        .should().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule restControllerAnnotationRule = ArchRuleDefinition.classes()
        .that().haveSimpleNameEndingWith("Controller").and().doNotHaveModifier(JavaModifier.ABSTRACT)
        .should().beAnnotatedWith(RestController.class);

    @ArchTest
    public static final ArchRule controllerAnnotationRule = ArchRuleDefinition.noClasses()
        .should().beAnnotatedWith(Controller.class);

    @ArchTest
    public static final ArchRule junit4TestAnnotationRule = ArchRuleDefinition.noMethods()
        .should().beAnnotatedWith(org.junit.Test.class);

    @ArchTest
    public static final ArchRule jakartaTransactionalAnnotationRule = ArchRuleDefinition.noMethods()
        .should().beAnnotatedWith(jakarta.transaction.Transactional.class); // use Spring framework annotation

}
