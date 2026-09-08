package com.logistics.shared.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(package = "com.logistics")
public class ArchitectureTest {

    // This ensures modules do not depend on each pther diretly!
    @ArchTest
    static final ArchRule moduleShouldNotDependOnEachOther =
            slices().matching("com.logistics.(*)..")
                    .should().notDependOnEachOther()
                    .ignoreDependency(
                        //Allow ignoring dependencies within the same module
                        (origin, target) -> origin.getOwner().getName().startWith(target.getowner().getName())
                    );
    
}
