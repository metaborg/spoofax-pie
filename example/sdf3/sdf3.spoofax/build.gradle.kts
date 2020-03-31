import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.CommandDefRepr.builder
import mb.spoofax.compiler.command.ParamRepr
import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.menu.CommandActionRepr
import mb.spoofax.compiler.menu.MenuItemRepr
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
  id("org.metaborg.gradle.config.junit-testing")
}

adapterProjectCompiler {
  settings.set(AdapterProjectCompilerSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),
    constraintAnalyzer = ConstraintAnalyzerCompiler.AdapterProjectInput.builder(),
    compiler = run {
      val packageId = "mb.sdf3.spoofax"
      val taskPackageId = "$packageId.task"
      val commandPackageId = "$packageId.command"

      val builder = AdapterProjectCompiler.Input.builder()

      // Utility task definitions
      val desugarTemplates = TypeInfo.of(taskPackageId, "Sdf3DesugarTemplates")
      builder.addTaskDefs(desugarTemplates)

      // Generation task definitions
      val toCompletionColorer = TypeInfo.of(taskPackageId, "Sdf3ToCompletionColorer")
      val toCompletionRuntime = TypeInfo.of(taskPackageId, "Sdf3ToCompletionRuntime")
      val toCompletion = TypeInfo.of(taskPackageId, "Sdf3ToCompletion")
      val toSignature = TypeInfo.of(taskPackageId, "Sdf3ToSignature")
      val toDynsemSignature = TypeInfo.of(taskPackageId, "Sdf3ToDynsemSignature")
      val toPrettyPrinter = TypeInfo.of(taskPackageId, "Sdf3ToPrettyPrinter")
      val toPermissive = TypeInfo.of(taskPackageId, "Sdf3ToPermissive")
      val toNormalForm = TypeInfo.of(taskPackageId, "Sdf3ToNormalForm")
      val specToParseTable = TypeInfo.of(taskPackageId, "Sdf3SpecToParseTable")
      val specToParenthesizer = TypeInfo.of(taskPackageId, "Sdf3SpecToParenthesizer")
      builder.addTaskDefs(toCompletionColorer, toCompletionRuntime, toCompletion, toSignature, toDynsemSignature,
        toPrettyPrinter, toPermissive, toNormalForm, specToParseTable, specToParenthesizer)

      // Show (debugging) task definitions
      val debugTaskPackageId = "$taskPackageId.debug"
      val showNormalForm = TypeInfo.of(debugTaskPackageId, "Sdf3ShowNormalForm")
      builder.addTaskDefs(showNormalForm)

      // Show (debugging) commands
      val showNormalFormCommand = builder()
        .type(commandPackageId, "Sdf3ShowNormalFormCommand")
        .taskDefType(showNormalForm)
        .argType(showNormalForm.appendToId(".Args"))
        .displayName("Show normal-form")
        .description("Shows the normal-form of the file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addAllParams(listOf(
          ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File)),
          ParamRepr.of("concrete", TypeInfo.ofBoolean(), true)
        ))
        .build()
      builder.addCommandDefs(showNormalFormCommand)

      // Menu bindings
      val mainAndEditorMenu = listOf(
        MenuItemRepr.menu("Debug",
          MenuItemRepr.menu("Transform",
            MenuItemRepr.menu("Abstract",
              CommandActionRepr.builder().manualOnce(showNormalFormCommand, mapOf(Pair("concrete", "false"))).fileRequired().buildItem(),
              CommandActionRepr.builder().manualContinuous(showNormalFormCommand, mapOf(Pair("concrete", "false"))).fileRequired().buildItem()
            ),
            MenuItemRepr.menu("Concrete",
              CommandActionRepr.builder().manualOnce(showNormalFormCommand, mapOf(Pair("concrete", "true"))).fileRequired().buildItem(),
              CommandActionRepr.builder().manualContinuous(showNormalFormCommand, mapOf(Pair("concrete", "true"))).fileRequired().buildItem()
            )
          )
        )
      )
      builder.addAllMainMenuItems(mainAndEditorMenu)
      builder.addAllEditorContextMenuItems(mainAndEditorMenu)
      builder.addResourceContextMenuItems(
        MenuItemRepr.menu("Debug",
          MenuItemRepr.menu("Transform",
            MenuItemRepr.menu("Abstract",
              CommandActionRepr.builder().manualOnce(showNormalFormCommand, mapOf(Pair("concrete", "false"))).fileRequired().buildItem()
            ),
            MenuItemRepr.menu("Concrete",
              CommandActionRepr.builder().manualOnce(showNormalFormCommand, mapOf(Pair("concrete", "true"))).fileRequired().buildItem()
            )
          )
        )
      )

      builder
    }
  ))
}

dependencies {
  api("org.metaborg:sdf2parenthesize")

  testAnnotationProcessor(platform("$group:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.dagger")
  testCompileOnly("org.checkerframework:checker-qual-android")
  testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

tasks.test {
  // HACK: skip if not in devenv composite build, as that is not using the latest version of SDF3.
  if (gradle.parent == null || gradle.parent!!.rootProject.name != "devenv") {
    onlyIf { false }
  }

  // Show standard out and err in tests during development.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
