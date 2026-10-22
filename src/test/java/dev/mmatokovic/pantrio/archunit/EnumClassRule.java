package dev.mmatokovic.pantrio.archunit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import dev.mmatokovic.pantrio.PantrioApplication;

@SuppressWarnings("unused")
@AnalyzeClasses(packagesOf = PantrioApplication.class)
class EnumClassRule {

    @ArchTest
    public static final ArchRule privateFinalFieldsRule = ArchRuleDefinition.fields()
        .that().areDeclaredInClassesThat().areEnums().and().areNotStatic()
        .should().bePrivate().andShould().beFinal();

}
