package mb.spoofax.runtime.cfg;

import com.google.inject.Inject;
import mb.log.Logger;
import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.term.Terms;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigParser {
    private final Logger log;


    @Inject public ConfigParser(Logger logFactory) {
        this.log = logFactory.forContext(getClass());
    }


    public ImmutableWorkspaceConfigPaths parseWorkspaceConfigPaths(IStrategoTerm root, PPath dir) {
        final ArrayList<IStrategoTerm> sections = sections(root);
        final ArrayList<IStrategoTerm> workspaceSections = subSectionsOf(sections, "WorkspaceSec");
        final ArrayList<PPath> langSpecConfigFiles = pathValuesOf(workspaceSections, "LangSpec", dir);
        return ImmutableWorkspaceConfigPaths.of(langSpecConfigFiles);
    }

    public LangSpecConfig parseLangSpecConfig(IStrategoTerm root, PPath dir) {
        final ArrayList<IStrategoTerm> sections = sections(root);
        final ArrayList<IStrategoTerm> langSpecSections = subSectionsOf(sections, "LangSpecSec");

        // Identification
        final ArrayList<IStrategoTerm> identificationSections = subSectionsOf(langSpecSections, "IdentificationSec");
        final ArrayList<String> extensions = stringValuesOf(identificationSections, "FileExtensions");

        // Information
        final ArrayList<IStrategoTerm> informationSections = subSectionsOf(langSpecSections, "InformationSec");
        final @Nullable String name = stringValueOf(informationSections, "Name").orElse(null);

        // Syntax
        final ArrayList<IStrategoTerm> syntaxSubSections = subSectionsOf(langSpecSections, "SyntaxSec");
        // Syntax - Parsing
        final ArrayList<IStrategoTerm> syntaxParseOpts = subSectionsOf(syntaxSubSections, "SyntaxParseSubSec");
        final ArrayList<PPath> syntaxFiles = pathValuesOf(syntaxParseOpts, "SyntaxParseFiles", dir);
        final @Nullable PPath syntaxParseMainFile = pathValueOf(syntaxParseOpts, "SyntaxParseMainFile", dir).orElse(null);
        final @Nullable String syntaxParseStartSymbolId = stringValueOf(syntaxParseOpts, "SyntaxParseStartSymbolId").orElse(null);
        // Syntax - Signatures
        final ArrayList<IStrategoTerm> syntaxSignatureSections = subSectionsOf(syntaxSubSections, "SyntaxSignatureSubSec");
        final List<PPath> syntaxSignatureFiles = pathValuesOf(syntaxSignatureSections, "SignatureSyntaxFiles", dir);
        // Syntax - Styling
        final ArrayList<IStrategoTerm> syntaxStyleSections = subSectionsOf(syntaxSubSections, "SyntaxStyleSubSec");
        final @Nullable PPath syntaxStyleFile = pathValueOf(syntaxStyleSections, "SyntaxStyleFile", dir).orElse(null);

        // NaTs
        final ArrayList<IStrategoTerm> natsOpts = subSectionsOf(langSpecSections, "NaTsSec");
        final List<PPath> natsNaBL2Files = pathValuesOf(natsOpts, "NaTsNaBL2Files", dir);
        final @Nullable ImmutableStrategoConfig natsStrategoConfig = value(natsOpts, "NaTsStrategoConfig")
            .flatMap((t) -> parseStrategoConfig(t, dir))
            .orElse(null);
        final @Nullable String natsStrategoStrategyId = stringValueOf(natsOpts, "NaTsStrategoStrategyId").orElse(null);
        final boolean natsRootScopePerFile = value(natsOpts, "NaTsRootScopePerFile")
            .map((t) -> {
                final IStrategoTerm inner = t.getSubterm(0);
                if(Terms.hasCons(inner, 0, "True")) return true;
                if(Terms.hasCons(inner, 0, "False")) return false;
                return false;
            })
            .orElse(false);

        return ImmutableLangSpecConfig.of(dir, extensions, name, syntaxFiles, syntaxParseMainFile,
            syntaxParseStartSymbolId, syntaxSignatureFiles, syntaxStyleFile, natsNaBL2Files, natsStrategoConfig,
            natsStrategoStrategyId, natsRootScopePerFile);
    }

    public Optional<ImmutableStrategoConfig> parseStrategoConfig(IStrategoTerm root, PPath dir) {
        final ArrayList<IStrategoTerm> options = sections(root);
        final PPath mainFile = pathValueOf(options, "StrategoConfigMainFile", dir).orElse(null);
        if(mainFile == null) {
            log.error("Invalid Stratego config; main file is not set");
            return Optional.empty();
        }
        final ArrayList<PPath> includeDirs = pathValuesOf(options, "StrategoConfigIncludeDirs", dir);
        final ArrayList<PPath> includeFiles = pathValuesOf(options, "StrategoConfigIncludeFiles", dir);
        final ArrayList<String> includeLibs = stringValuesOf(options, "StrategoConfigIncludeLibs");
        final PPath baseDir = pathValueOf(options, "StrategoConfigBaseDir", dir).orElse(mainFile.parent());
        if(baseDir == null) {
            log.error("Invalid Stratego config of main file {}; base directory is not set and main file has no parent directory", mainFile);
            return Optional.empty();
        }
        final PPath cacheDir = pathValueOf(options, "StrategoConfigCacheDir", dir).orElseGet(() -> {
            final PPath parent = mainFile.parent();
            if(parent != null) {
                // Default to <parent of main file>/target/str-cache
                return parent.resolve("target/str-cache");
            }
            return null;
        });
        if(cacheDir == null) {
            log.error("Invalid Stratego config of main file {}; cache directory is not set and main file has no parent directory",
                mainFile);
            return Optional.empty();
        }
        final PPath outputFile = pathValueOf(options, "StrategoConfigOutputFile", dir).orElse(mainFile.extend(".ctree"));
        return Optional.of(ImmutableStrategoConfig.of(mainFile, includeDirs, includeFiles, includeLibs, baseDir, cacheDir, outputFile));
    }


    private ArrayList<IStrategoTerm> sections(IStrategoTerm root) {
        return Terms
            .stream(root.getSubterm(0))
            .collect(Collectors.toCollection(ArrayList::new));
    }


    private Optional<IStrategoTerm> subSectionOf(ArrayList<IStrategoTerm> sections, String constructorName) {
        return sections
            .stream()
            .filter((t) -> Terms.isAppl(t, constructorName))
            .findFirst();
    }

    private ArrayList<IStrategoTerm> subSectionsOf(ArrayList<IStrategoTerm> sections, String constructorName) {
        return sections
            .stream()
            .filter((t) -> Terms.isAppl(t, constructorName))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .collect(Collectors.toCollection(ArrayList::new));
    }


    private Optional<IStrategoTerm> value(ArrayList<IStrategoTerm> sections, String constructorName) {
        return sections
            .stream()
            .filter((t) -> Terms.isAppl(t, constructorName))
            .findFirst()
            .map((t) -> t.getSubterm(0));
    }

    private Stream<IStrategoTerm> values(ArrayList<IStrategoTerm> sections, String constructorName) {
        return sections
            .stream()
            .filter((t) -> Terms.isAppl(t, constructorName))
            .map((t) -> t.getSubterm(0));
    }


    private Optional<String> stringValueOf(ArrayList<IStrategoTerm> sections, String constructorName) {
        return values(sections, constructorName)
            .flatMap(ConfigParser::stringTerms)
            .map(this::interpolateString)
            .findFirst();
    }

    private ArrayList<String> stringValuesOf(ArrayList<IStrategoTerm> sections, String constructorName) {
        return values(sections, constructorName)
            .flatMap(ConfigParser::stringTerms)
            .map(this::interpolateString)
            .collect(Collectors.toCollection(ArrayList::new));
    }


    private Optional<PPath> pathValueOf(ArrayList<IStrategoTerm> sections, String constructorName, PPath dir) {
        return values(sections, constructorName)
            .flatMap(ConfigParser::stringTerms)
            .map(this::interpolateString)
            .map(dir::resolve)
            .findFirst();
    }

    private ArrayList<PPath> pathValuesOf(ArrayList<IStrategoTerm> sections, String constructorName, PPath dir) {
        return values(sections, constructorName)
            .flatMap(ConfigParser::stringTerms)
            .map(this::interpolateString)
            .map(dir::resolve)
            .collect(Collectors.toCollection(ArrayList::new));
    }


    private static Stream<IStrategoTerm> stringTerms(IStrategoTerm stringOrStringsTerm) {
        if(Terms.isAppl(stringOrStringsTerm, 1, "String")) {
            return Stream.of(stringOrStringsTerm);
        } else if(Terms.isAppl(stringOrStringsTerm, 1, "Strings")) {
            return Terms.stream(stringOrStringsTerm.getSubterm(0));
        } else {
            return Stream.empty();
        }
    }


    private String interpolateString(IStrategoTerm stringTerm) {
        return Terms
            .stream(stringTerm.getSubterm(0))
            .map((t) -> {
                if(Terms.isAppl(t, 1, "Chars")) {
                    return Terms.asString(t.getSubterm(0));
                } else if(Terms.isAppl(t, 1, "Ref")) {
                    return ""; // TODO: replace with string value
                } else {
                    return "";
                }
            })
            .collect(Collectors.joining());
    }
}
