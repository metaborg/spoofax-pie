package mb.spoofax.lwb.compiler.definition;

import mb.common.option.Option;
import mb.common.util.StreamUtil;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerScope;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SpoofaxLwbCompilerScope
public class LanguageDefinitionManager {
    private final HashMap<Coordinate, ResourcePath> languageDefinitions = new HashMap<>();

    @Inject public LanguageDefinitionManager() {}

    public Option<ResourcePath> getLanguageDefinition(Coordinate coordinate) {
        return Option.ofNullable(languageDefinitions.get(coordinate));
    }

    public Stream<ResourcePath> getLanguageDefinitions(CoordinateRequirement coordinate) {
        final Stream.Builder<ResourcePath> streamBuilder = Stream.builder();
        for(Map.Entry<Coordinate, ResourcePath> entry : languageDefinitions.entrySet()) {
            if(coordinate.matches(entry.getKey())) {
                streamBuilder.accept(entry.getValue());
            }
        }
        return streamBuilder.build();
    }

    public Option<ResourcePath> getOneLanguageDefinition(CoordinateRequirement coordinateRequirement) {
        final Stream<ResourcePath> languageDefinitions = getLanguageDefinitions(coordinateRequirement);
        return StreamUtil.findOne(languageDefinitions);
    }


    void registerLanguageDefinition(Coordinate coordinate, ResourcePath rootDirectory) {
        languageDefinitions.put(coordinate, rootDirectory);
    }
}
