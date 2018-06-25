@file:Suppress("warnings")

package mb.spoofax.pie.generated

import java.io.Serializable
import java.nio.file.Paths
import com.google.inject.Binder
import com.google.inject.Inject
import com.google.inject.multibindings.MapBinder
import mb.pie.api.*
import mb.pie.lang.runtime.path.*
import mb.pie.lang.runtime.util.*
import mb.pie.taskdefs.guice.TaskDefsModule
import mb.pie.vfs.path.*

class processWorkspace @Inject constructor(
  private val listContents: ListContents,
  private val _processProject: processProject
) : TaskDef<PPath, mb.spoofax.pie.processing.WorkspaceResult> {
  companion object {
    const val id = "processWorkspace"
  }

  override val id = Companion.id
  override fun key(input: PPath): Key = input
  override fun ExecContext.exec(input: PPath): mb.spoofax.pie.processing.WorkspaceResult = run {
    val projectResults = require(listContents, ListContents.Input(input, PPaths.regexPathMatcher("^[^.]((?!src-gen).)*\$"))).map { project -> require(_processProject, processProject.Input(project, input)) }.toCollection(ArrayList<mb.spoofax.pie.processing.ProjectResult>())
    mb.spoofax.pie.processing.createWorkspaceResult(input, projectResults)
  }
}

class processProject @Inject constructor(
  private val _mb_spoofax_pie_processing_LegacyExtensions: mb.spoofax.pie.processing.LegacyExtensions,
  private val _legacyProcessDocument: legacyProcessDocument,
  private val walkContents: WalkContents,
  private val _mb_spoofax_pie_processing_LangSpecExtensions: mb.spoofax.pie.processing.LangSpecExtensions,
  private val _processDocument: processDocument
) : TaskDef<processProject.Input, mb.spoofax.pie.processing.ProjectResult> {
  companion object {
    const val id = "processProject"
  }

  data class Input(val project: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun key(input: processProject.Input): Key = input.project
  override fun ExecContext.exec(input: processProject.Input): mb.spoofax.pie.processing.ProjectResult = run {
    val langSpecResults = require(walkContents, WalkContents.Input(input.project, PPaths.extensionsPathWalker(require(_mb_spoofax_pie_processing_LangSpecExtensions, input.root)))).map { document ->
      run {
        if(!mb.spoofax.pie.processing.shouldProcessDocument(document)) run {
          mb.spoofax.pie.processing.emptyDocumentResult(document)
        } else run {
          require(_processDocument, processDocument.Input(document, input.project, input.root))
        }
      }
    }.toCollection(ArrayList<mb.spoofax.pie.processing.DocumentResult>());
    val legacyResults = require(walkContents, WalkContents.Input(input.project, PPaths.extensionsPathWalker(require(_mb_spoofax_pie_processing_LegacyExtensions, None.instance)))).map { document ->
      run {
        if(!mb.spoofax.pie.processing.shouldProcessDocument(document)) run {
          mb.spoofax.pie.processing.emptyDocumentResult(document)
        } else run {
          require(_legacyProcessDocument, legacyProcessDocument.Input(document, input.project, input.root))
        }
      }
    }.toCollection(ArrayList<mb.spoofax.pie.processing.DocumentResult>())
    mb.spoofax.pie.processing.createProjectResult(input.project, langSpecResults, legacyResults)
  }
}

class processEditor @Inject constructor(
  private val _legacyProcessTextBuffer: legacyProcessTextBuffer,
  private val _mb_spoofax_pie_processing_IsLegacyDocument: mb.spoofax.pie.processing.IsLegacyDocument,
  private val _processTextBuffer: processTextBuffer,
  private val _mb_spoofax_pie_processing_IsLangSpecDocument: mb.spoofax.pie.processing.IsLangSpecDocument,
  private val exists: Exists
) : TaskDef<processEditor.Input, mb.spoofax.pie.processing.DocumentResult> {
  companion object {
    const val id = "processEditor"
  }

  data class Input(val document: PPath, val project: PPath, val root: PPath, val text: String) : Tuple4<PPath, PPath, PPath, String> {
    constructor(tuple: Tuple4<PPath, PPath, PPath, String>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun key(input: processEditor.Input): Key = input.document
  override fun ExecContext.exec(input: processEditor.Input): mb.spoofax.pie.processing.DocumentResult = run {

    if(!require(exists, input.document) || !mb.spoofax.pie.processing.shouldProcessDocument(input.document)) run {
      mb.spoofax.pie.processing.emptyDocumentResult(input.document)
    } else run {
      if(require(_mb_spoofax_pie_processing_IsLangSpecDocument, mb.spoofax.pie.processing.IsLangSpecDocument.Input(input.document, input.root))) run {
        require(_processTextBuffer, processTextBuffer.Input(input.document, input.project, input.root, input.text))
      } else run {
        if(require(_mb_spoofax_pie_processing_IsLegacyDocument, input.document)) run {
          require(_legacyProcessTextBuffer, legacyProcessTextBuffer.Input(input.document, input.project, input.root, input.text))
        } else run {
          mb.spoofax.pie.processing.emptyDocumentResult(input.document)
        }
      }
    }
  }
}

class processDocument @Inject constructor(
  private val _processTextBuffer: processTextBuffer,
  private val read: Read
) : TaskDef<processDocument.Input, mb.spoofax.pie.processing.DocumentResult> {
  companion object {
    const val id = "processDocument"
  }

  data class Input(val document: PPath, val project: PPath, val root: PPath) : Tuple3<PPath, PPath, PPath> {
    constructor(tuple: Tuple3<PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun key(input: processDocument.Input): Key = input.document
  override fun ExecContext.exec(input: processDocument.Input): mb.spoofax.pie.processing.DocumentResult = run {
    val text = require(read, input.document)!!
    require(_processTextBuffer, processTextBuffer.Input(input.document, input.project, input.root, text))
  }
}

class processTextBuffer @Inject constructor(
  private val _solveDocument: solveDocument,
  private val _style: style,
  private val _parse: parse,
  private val _mb_spoofax_pie_processing_LangIdOfDocument: mb.spoofax.pie.processing.LangIdOfDocument
) : TaskDef<processTextBuffer.Input, mb.spoofax.pie.processing.DocumentResult> {
  companion object {
    const val id = "processTextBuffer"
  }

  data class Input(val document: PPath, val project: PPath, val root: PPath, val text: String) : Tuple4<PPath, PPath, PPath, String> {
    constructor(tuple: Tuple4<PPath, PPath, PPath, String>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun key(input: processTextBuffer.Input): Key = input.document
  override fun ExecContext.exec(input: processTextBuffer.Input): mb.spoofax.pie.processing.DocumentResult = run {
    val langId = require(_mb_spoofax_pie_processing_LangIdOfDocument, mb.spoofax.pie.processing.LangIdOfDocument.Input(input.document, input.root));
    val (ast, tokens, messages) = require(_parse, parse.Input(input.document, langId, input.root, input.text));
    val styling: mb.spoofax.api.style.Styling? = if(tokens == null) null else require(_style, style.Input(langId, input.root, tokens!!));
    val constraintsSolution: mb.spoofax.runtime.constraint.CSolution? = if(ast == null) null else require(_solveDocument, solveDocument.Input(input.document, langId, input.project, input.root, ast!!))
    mb.spoofax.pie.processing.createDocumentResult(input.document, messages, tokens, ast, styling, constraintsSolution)
  }
}

class parse @Inject constructor(
  private val _mb_spoofax_pie_jsglr_JSGLRParse: mb.spoofax.pie.jsglr.JSGLRParse,
  private val _mb_spoofax_pie_sdf3_SDF3ToJSGLRParseTable: mb.spoofax.pie.sdf3.SDF3ToJSGLRParseTable
) : TaskDef<parse.Input, parse.Output> {
  companion object {
    const val id = "parse"
  }

  data class Input(val document: PPath, val langId: mb.spoofax.runtime.cfg.LangId, val root: PPath, val text: String) : Tuple4<PPath, mb.spoofax.runtime.cfg.LangId, PPath, String> {
    constructor(tuple: Tuple4<PPath, mb.spoofax.runtime.cfg.LangId, PPath, String>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun key(input: parse.Input): Key = input.document
  override fun ExecContext.exec(input: parse.Input): parse.Output = run {
    val parseTable = require(_mb_spoofax_pie_sdf3_SDF3ToJSGLRParseTable, mb.spoofax.pie.sdf3.SDF3ToJSGLRParseTable.Input(input.langId, input.root));
    if(parseTable == null) run {
      val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null;
      val emptyTokens: ArrayList<mb.spoofax.api.parse.Token>? = null;
      val emptyMessages: ArrayList<mb.spoofax.api.message.Msg> = list();
      return output(tuple(emptyAst, emptyTokens, emptyMessages))
    }
    output(require(_mb_spoofax_pie_jsglr_JSGLRParse, mb.spoofax.pie.jsglr.JSGLRParse.Input(input.document, input.langId, input.root, input.text, parseTable!!)))
  }
}

class style @Inject constructor(
  private val _mb_spoofax_pie_style_SpoofaxStyle: mb.spoofax.pie.style.SpoofaxStyle,
  private val _mb_spoofax_pie_esv_ESVToStylingRules: mb.spoofax.pie.esv.ESVToStylingRules
) : TaskDef<style.Input, mb.spoofax.api.style.Styling?> {
  companion object {
    const val id = "style"
  }

  data class Input(val langId: mb.spoofax.runtime.cfg.LangId, val root: PPath, val tokens: ArrayList<mb.spoofax.api.parse.Token>) : Tuple3<mb.spoofax.runtime.cfg.LangId, PPath, ArrayList<mb.spoofax.api.parse.Token>> {
    constructor(tuple: Tuple3<mb.spoofax.runtime.cfg.LangId, PPath, ArrayList<mb.spoofax.api.parse.Token>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun key(input: style.Input): Key = input
  override fun ExecContext.exec(input: style.Input): mb.spoofax.api.style.Styling? = run {
    val syntaxStyler = require(_mb_spoofax_pie_esv_ESVToStylingRules, mb.spoofax.pie.esv.ESVToStylingRules.Input(input.langId, input.root));
    if(syntaxStyler == null) return null
    require(_mb_spoofax_pie_style_SpoofaxStyle, mb.spoofax.pie.style.SpoofaxStyle.Input(input.tokens, syntaxStyler!!)) as mb.spoofax.api.style.Styling?
  }
}

class solveDocument @Inject constructor(
  private val _mb_spoofax_pie_constraint_CSolveFinal: mb.spoofax.pie.constraint.CSolveFinal,
  private val _mb_spoofax_pie_constraint_CSolveDocument: mb.spoofax.pie.constraint.CSolveDocument,
  private val _mb_spoofax_pie_constraint_CGenDocument: mb.spoofax.pie.constraint.CGenDocument,
  private val _mb_spoofax_pie_constraint_CSolveGlobal: mb.spoofax.pie.constraint.CSolveGlobal,
  private val _mb_spoofax_pie_constraint_CGenGlobal: mb.spoofax.pie.constraint.CGenGlobal
) : TaskDef<solveDocument.Input, mb.spoofax.runtime.constraint.CSolution?> {
  companion object {
    const val id = "solveDocument"
  }

  data class Input(val document: PPath, val langId: mb.spoofax.runtime.cfg.LangId, val project: PPath, val root: PPath, val ast: org.spoofax.interpreter.terms.IStrategoTerm) : Tuple5<PPath, mb.spoofax.runtime.cfg.LangId, PPath, PPath, org.spoofax.interpreter.terms.IStrategoTerm> {
    constructor(tuple: Tuple5<PPath, mb.spoofax.runtime.cfg.LangId, PPath, PPath, org.spoofax.interpreter.terms.IStrategoTerm>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  override val id = Companion.id
  override fun key(input: solveDocument.Input): Key = input.document
  override fun ExecContext.exec(input: solveDocument.Input): mb.spoofax.runtime.constraint.CSolution? = run {
    val globalConstraints = require(_mb_spoofax_pie_constraint_CGenGlobal, mb.spoofax.pie.constraint.CGenGlobal.Input(input.langId, input.root));
    if(globalConstraints == null) return null;
    val globalSolution = require(_mb_spoofax_pie_constraint_CSolveGlobal, globalConstraints!!);
    if(globalSolution == null) return null;
    val documentConstraints = require(_mb_spoofax_pie_constraint_CGenDocument, mb.spoofax.pie.constraint.CGenDocument.Input(input.document, input.langId, input.root, input.ast, globalConstraints!!));
    if(documentConstraints == null) return null;
    val documentSolution = require(_mb_spoofax_pie_constraint_CSolveDocument, mb.spoofax.pie.constraint.CSolveDocument.Input(documentConstraints!!, globalConstraints!!, globalSolution!!));
    if(documentSolution == null) return null;
    val solution = require(_mb_spoofax_pie_constraint_CSolveFinal, mb.spoofax.pie.constraint.CSolveFinal.Input(input.project, list(documentSolution!!), globalSolution!!))
    solution
  }
}

class legacyProcessDocument @Inject constructor(
  private val _legacyProcessTextBuffer: legacyProcessTextBuffer,
  private val read: Read
) : TaskDef<legacyProcessDocument.Input, mb.spoofax.pie.processing.DocumentResult> {
  companion object {
    const val id = "legacyProcessDocument"
  }

  data class Input(val document: PPath, val project: PPath, val root: PPath) : Tuple3<PPath, PPath, PPath> {
    constructor(tuple: Tuple3<PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun key(input: legacyProcessDocument.Input): Key = input.document
  override fun ExecContext.exec(input: legacyProcessDocument.Input): mb.spoofax.pie.processing.DocumentResult = run {
    val text = require(read, input.document)!!
    require(_legacyProcessTextBuffer, legacyProcessTextBuffer.Input(input.document, input.project, input.root, text))
  }
}

class legacyProcessTextBuffer @Inject constructor(
  private val _mb_spoofax_pie_legacy_LegacyStyle: mb.spoofax.pie.legacy.LegacyStyle,
  private val _mb_spoofax_pie_legacy_LegacyParse: mb.spoofax.pie.legacy.LegacyParse
) : TaskDef<legacyProcessTextBuffer.Input, mb.spoofax.pie.processing.DocumentResult> {
  companion object {
    const val id = "legacyProcessTextBuffer"
  }

  data class Input(val document: PPath, val project: PPath, val root: PPath, val text: String) : Tuple4<PPath, PPath, PPath, String> {
    constructor(tuple: Tuple4<PPath, PPath, PPath, String>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun key(input: legacyProcessTextBuffer.Input): Key = input.document
  override fun ExecContext.exec(input: legacyProcessTextBuffer.Input): mb.spoofax.pie.processing.DocumentResult = run {
    val (ast, tokens, messages) = require(_mb_spoofax_pie_legacy_LegacyParse, mb.spoofax.pie.legacy.LegacyParse.Input(input.document, input.text));
    val styling: mb.spoofax.api.style.Styling? = if(ast == null || tokens == null) null else require(_mb_spoofax_pie_legacy_LegacyStyle, mb.spoofax.pie.legacy.LegacyStyle.Input(input.document, tokens!!, ast!!)) as mb.spoofax.api.style.Styling?
    mb.spoofax.pie.processing.createDocumentResult(input.document, messages, tokens, ast, styling, null)
  }
}


class TaskDefsModule_spoofax : TaskDefsModule() {
  override fun Binder.bindTaskDefs(taskDefsBinder: MapBinder<String, TaskDef<*, *>>) {
    bindTaskDef<legacyProcessTextBuffer>(taskDefsBinder, "legacyProcessTextBuffer")
    bindTaskDef<legacyProcessDocument>(taskDefsBinder, "legacyProcessDocument")
    bindTaskDef<solveDocument>(taskDefsBinder, "solveDocument")
    bindTaskDef<style>(taskDefsBinder, "style")
    bindTaskDef<parse>(taskDefsBinder, "parse")
    bindTaskDef<processTextBuffer>(taskDefsBinder, "processTextBuffer")
    bindTaskDef<processDocument>(taskDefsBinder, "processDocument")
    bindTaskDef<processEditor>(taskDefsBinder, "processEditor")
    bindTaskDef<processProject>(taskDefsBinder, "processProject")
    bindTaskDef<processWorkspace>(taskDefsBinder, "processWorkspace")
  }
}
