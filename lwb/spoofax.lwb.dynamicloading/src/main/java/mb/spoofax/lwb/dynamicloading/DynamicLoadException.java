package mb.spoofax.lwb.dynamicloading;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.util.ADT;
import mb.spoofax.core.Coordinate;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

@ADT
public abstract class DynamicLoadException extends Exception implements HasOptionalMessages {
    public interface Cases<R> {
        R supplyDynamicLoadInfoFail(Exception cause);

        R requireInputFileFail(IOException ioException);

        R classPathToUrlFail(MalformedURLException malformedURLException);

        R participantInstantiateFail(ReflectiveOperationException reflectiveOperationException);

        R incompatibleLoggerComponent(String requiredClassName, String className);

        R incompatibleBaseResourceServiceComponent(String requiredClassName, String className);

        R incompatiblePlatformComponent(String requiredClassName, String className);

        R duplicateCoordinateFail(Coordinate duplicateCoordinate, DynamicComponentInfo info, DynamicComponentInfo otherInfo);

        R duplicateFileExtensionFail(String duplicateFileExtension, DynamicComponentInfo info, DynamicComponentInfo otherInfo);
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

    public static DynamicLoadException duplicateCoordinateFail(Coordinate duplicateCoordinate, DynamicComponentInfo info, DynamicComponentInfo otherInfo, @Nullable IOException closeException) {
        return withCause(DynamicLoadExceptions.duplicateCoordinateFail(duplicateCoordinate, info, otherInfo), closeException);
    }

    public static DynamicLoadException duplicateFileExtensionFail(String duplicateFileExtension, DynamicComponentInfo info, DynamicComponentInfo otherInfo, @Nullable IOException closeException) {
        return withCause(DynamicLoadExceptions.duplicateFileExtensionFail(duplicateFileExtension, info, otherInfo), closeException);
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
            .duplicateCoordinateFail((duplicateCoordinate, info, otherInfo) -> "Cannot register dynamically loaded component '" + info + "' with coordinate '" + duplicateCoordinate + "', a different component '" + otherInfo + "' is already registered with those coordinates" + closeFailMessage())
            .duplicateFileExtensionFail((duplicateFileExtension, info, otherInfo) -> "Cannot register dynamically loaded component '" + info.coordinate + "' with a language using file extension '" + duplicateFileExtension + "', another component '" + otherInfo.coordinate + "' has a language that is already registered with that file extension" + closeFailMessage())
            .apply(this);
    }

    private String closeFailMessage() {
        if(getCause() != null) {
            return ". Additionally, closing the dynamic component failed so resources may have leaked. See the cause of this exception for more info";
        }
        return "";
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }

    @Override public Optional<KeyedMessages> getOptionalMessages() {
        return DynamicLoadExceptions.getMessages(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
