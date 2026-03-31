package org.hikikomori.community.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String ROOT = "org.hikikomori.community";

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @Test
    @DisplayName("Controller는 Facade와 DTO만 의존한다")
    void controllerDependencies() {
        noClasses().that().resideInAPackage(ROOT + ".controller..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + ".service..")
                .as("Controller는 Service에 직접 의존하면 안 됩니다")
                .check(classes);

        noClasses().that().resideInAPackage(ROOT + ".controller..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + ".repository..")
                .as("Controller는 Repository에 직접 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Service는 Repository를 모른다")
    void serviceShouldNotDependOnRepository() {
        noClasses().that().resideInAPackage(ROOT + ".service..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + ".repository..")
                .as("Service는 Repository에 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Controller는 Entity에 직접 의존하지 않는다")
    void controllerShouldNotDependOnDomain() {
        noClasses().that().resideInAPackage(ROOT + ".controller..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + ".domain..")
                .as("Controller는 Entity에 직접 의존하면 안 됩니다")
                .check(classes);
    }

    @Test
    @DisplayName("Facade와 Service는 JpaRepository에 직접 접근하지 않는다")
    void facadeAndServiceShouldNotUseJpaRepository() {
        noClasses().that().resideInAnyPackage(ROOT + ".facade..", ROOT + ".service..")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("JpaRepository")
                .as("Facade와 Service는 JpaRepository에 직접 의존하면 안 됩니다")
                .check(classes);
    }
}
