package com.logistics.bootstrap.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ModuleArchitectureTest {

    private static final String IDENTITY = "com.logistics.identity..";
    private static final String DRIVER = "com.logistics.driver..";
    private static final String ORDER = "com.logistics.order..";
    private static final String TRACKING = "com.logistics.tracking..";
    private static final String ROUTING = "com.logistics.routing..";
    private static final String ANALYTICS = "com.logistics.analytics..";

    private static final String[] FEATURE_MODULES = {IDENTITY, DRIVER, ORDER, TRACKING, ROUTING, ANALYTICS};

    @Test
    void featureModulesMustNotDependOnOtherFeatureModulesInfrastructure() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.logistics");

        String[] modules = {
            "com.logistics.identity",
            "com.logistics.driver",
            "com.logistics.order",
            "com.logistics.tracking",
            "com.logistics.routing",
            "com.logistics.analytics"
        };

        for (String sourceModule : modules) {
            for (String targetModule : modules) {
                // A module may use its own infrastructure.
                if (sourceModule.equals(targetModule)) {
                    continue;
                }

                ArchRule rule = noClasses()
                        .that()
                        .resideInAnyPackage(sourceModule + "..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(targetModule + ".infrastructure..")
                        .allowEmptyShould(true);

                rule.check(classes);
            }
        }
    }

    @Test
    void sharedKernelMustNotDependOnFeatureModules() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.logistics");

        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("com.logistics.shared..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(FEATURE_MODULES);

        rule.check(classes);
    }
}
