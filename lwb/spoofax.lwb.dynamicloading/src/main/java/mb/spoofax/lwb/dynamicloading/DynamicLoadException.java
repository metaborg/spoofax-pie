package mb.spoofax.lwb.dynamicloading;

import mb.common.util.ADT;
import mb.spoofax.core.Coordinate;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;

@ADT
public abstract class DynamicLoadException extends Exception {
    public interface Cases<R> {
        R supplyDynamicLoadInfoFail(Exception cause);

        R requireInputFileFail(IOException ioException);

        R classPathToUrlFail(MalformedURLException malformedURLException);

        R participantInstantiateFail(ReflectiveOperationException reflectiveOperationException);

        R incompatibleLoggerComponent(String requiredClassName, String className);

        R incompatibleBaseResourceServiceComponent(String requiredClassName, String className);

        R incompatiblePlatformComponent(String requiredClassName, String className);

        R duplicateCoordinateFail(Coordinate duplicateCoordinate, DynamicComponentInfo componentInfo);

        R duplicateFileExtensionFail(String duplicateFileExtension, DynamicComponentInfo componentInfo);

        R closeExistingFail(IOException ioException);
    }

    public static DynamicLoadException supplyDynamicLoadInfoFail(Exception cause) {
        return withCause(DynamicLoadExceptions.supplyDynamicLoadInfoFail(cause), cause);
    }

    public static DynamicLoadException requireInputFileFail(IOException ioException) {
        return withCause(DynamicLoadExceptions.requireInputFileFail(ioException), ioException);
    }

    public static DynamicLoadException classPathToUrlFail(MalformedURLException malformedURLException) {
        return withCause(DynamicLoadExceptions.classPathToUrlFail(malformedURLException), malformedURLException);
    }

    public static DynamicLoadException participantInstantiateFail(ReflectiveOperationException reflectiveOperationException) {
        return withCause(DynamicLoadExceptions.participantInstantiateFail(reflectiveOperationException), reflectiveOperationException);
    }

    public static DynamicLoadException incompatibleLoggerComponent(String requiredClassName, String className) {
        return DynamicLoadExceptions.incompatibleLoggerComponent(requiredClassName, className);
    }

    public static DynamicLoadException incompatibleBaseResourceServiceComponent(String requiredClassName, String className) {
        return DynamicLoadExceptions.incompatibleBaseResourceServiceComponent(requiredClassName, className);
    }

    public static DynamicLoadException incompatiblePlatformComponent(String requiredClassName, String className) {
        return DynamicLoadExceptions.incompatiblePlatformComponent(requiredClassName, className);
    }

    public static DynamicLoadException duplicateCoordinateFail(Coordinate duplicateCoordinate, DynamicComponentInfo componentInfo) {
        return DynamicLoadExceptions.duplicateCoordinateFail(duplicateCoordinate, componentInfo);
    }

    public static DynamicLoadException duplicateFileExtensionFail(String duplicateFileExtension, DynamicComponentInfo componentInfo) {
        return DynamicLoadExceptions.duplicateFileExtensionFail(duplicateFileExtension, componentInfo);
    }

    public static DynamicLoadException closeExistingFail(IOException ioException) {
        return withCause(DynamicLoadExceptions.closeExistingFail(ioException), ioException);
    }


    private static DynamicLoadException withCause(DynamicLoadException e, @Nullable Exception cause) {
        if(cause != null) {
            e.initCause(cause);
        }
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static DynamicLoadExceptions.CasesMatchers.TotalMatcher_SupplyDynamicLoadInfoFail cases() {
        return DynamicLoadExceptions.cases();
    }

    public DynamicLoadExceptions.CaseOfMatchers.TotalMatcher_SupplyDynamicLoadInfoFail caseOf() {
        return DynamicLoadExceptions.caseOf(this);
    }


    @Override public @NonNull String getMessage() {
        return cases()
            .supplyDynamicLoadInfoFail((e) -> "Supplying the dynamic load information failed")
            .requireInputFileFail((e) -> "Requiring input file of the classpath failed")
            .classPathToUrlFail((e) -> "Converting a part of the classpath to a URL failed")
            .participantInstantiateFail((e) -> "Instantiating the Participant of the component through reflection failed")
            .incompatibleLoggerComponent((requiredClassName, className) -> "Cannot register dynamically loaded component, participant requires a LoggerComponent that implements '" + requiredClassName + "', but ours (" + className + ") does not")
            .incompatibleBaseResourceServiceComponent((requiredClassName, className) -> "Cannot register dynamically loaded component, participant requires a base ResourceServiceComponent that implements '" + requiredClassName + "', but ours (" + className + ") does not")
            .incompatiblePlatformComponent((requiredClassName, className) -> "Cannot register dynamically loaded component, participant requires a PlatformComponent that implements '" + requiredClassName + "', but ours (" + className + ") does not")
            .duplicateCoordinateFail((duplicateCoordinate, otherInfo) -> "Cannot create dynamic component from participant with coordinate '" + duplicateCoordinate + "', a different component '" + otherInfo + "' is already registered with those coordinates")
            .duplicateFileExtensionFail((duplicateFileExtension, otherInfo) -> "Cannot create dynamic component from participant with a language using file extension '" + duplicateFileExtension + "', another component '" + otherInfo.coordinate + "' has a language that is already registered with that file extension")
            .closeExistingFail((e) -> "Closing the existing dynamic component before reloading failed")
            .apply(this);
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
