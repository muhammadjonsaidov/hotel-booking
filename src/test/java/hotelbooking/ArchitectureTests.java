package hotelbooking;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hotelbooking", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTests {
    @ArchTest
    public static final ArchRule reservationsUsesOnlyPublicContracts =
            noClasses().that().resideInAPackage("hotelbooking.reservations..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "hotelbooking.rooms.internal..", "hotelbooking.payments.internal..",
                            "hotelbooking.notifications.internal..");

    @ArchTest
    public static final ArchRule roomsDependsOnNoOtherModule =
            noClasses().that().resideInAPackage("hotelbooking.rooms..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "hotelbooking.reservations..", "hotelbooking.payments..",
                            "hotelbooking.notifications..");

    @ArchTest
    public static final ArchRule paymentsAndNotificationsDoNotDependOnReservations =
            noClasses().that().resideInAnyPackage("hotelbooking.payments..", "hotelbooking.notifications..")
                    .should().dependOnClassesThat().resideInAPackage("hotelbooking.reservations..");
}
