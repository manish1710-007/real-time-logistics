package com.logistics.shared.arch;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.logistics")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule modulesShouldNotDependOnEachOther =
            slices().matching("com.logistics.(*).")
                    .should().notDependOnEachOther()
                    .ignoreDependency( alwaysTrue(),
                        // Ignore the dependency if the target is the shared-kernel
                        resideInAnyPackage("com.logistics.shared...")
                    );
}