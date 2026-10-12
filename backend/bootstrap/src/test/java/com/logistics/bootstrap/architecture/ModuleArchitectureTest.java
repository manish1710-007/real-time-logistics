package com.logistics.bootstrap.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleArchitectureTest {

    private static final String IDENTITY = "com.logistics.identity..";
    private static final String DRIVER = "com.logistics.driver..";
    private static final String ORDER = "com.logistics.order..";
    private static final String TRACKING = "com.logistics.tracking..";
    private static final String ROUTING = "com.logistics.routing..";
    private static final String ANALYTICS = "com.logistics.analytics..";


    @Test
    void featureModulesMustNotDependOnOtherFeatureModulesInternals() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.logistics");

        ArchRule rule = noClasses().that().resideInAnyPackage(IDENTITY, DRIVER, ORDER, TRACKING, ROUTING, ANALYTICS)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        IDENTITY + "internal..",
                        DRIVER + "internal..",
                        ORDER + "internal..",
                        TRACKING + "internal..",
                        ROUTING + "internal..",
                        ANALYTICS + "internal..");
        rule.check(classes);
    }
}