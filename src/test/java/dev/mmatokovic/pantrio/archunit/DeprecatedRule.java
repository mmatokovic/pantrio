package dev.mmatokovic.pantrio.archunit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import dev.mmatokovic.pantrio.PantrioApplication;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;

@SuppressWarnings("unused")
@AnalyzeClasses(packagesOf = PantrioApplication.class)
class DeprecatedRule {

    @ArchTest
    public static final ArchRule deprecatedClassRule = ArchRuleDefinition.noClasses()
        .should().dependOnClassesThat().areAnnotatedWith(Deprecated.class);

    @ArchTest
    public static final ArchRule deprecatedMethodRule = ArchRuleDefinition.noClasses()
        .should().callMethodWhere(target(annotatedWith(Deprecated.class)));

    @ArchTest
    public static final ArchRule deprecatedFieldRule = ArchRuleDefinition.noClasses()
        .should().accessFieldWhere(target(annotatedWith(Deprecated.class)));

}
