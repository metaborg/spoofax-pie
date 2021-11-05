package mb.rv32im.task;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.rv32im.Rv32ImScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import venus.assembler.Assembler;
import venus.assembler.AssemblerError;
import venus.assembler.AssemblerOutput;
import venus.linker.LinkedProgram;
import venus.linker.Linker;
import venus.riscv.insts.*;
import venus.simulator.Simulator;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Rv32ImScope
public class ExecuteRiscV implements TaskDef<ExecuteRiscV.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourceKey file;

        public Args(ResourceKey file) {
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file);
        }

        @Override public String toString() {
            return "ExecuteRiscV$Args{" +
                "file=" + file +
                '}';
        }
    }

    private final Rv32ImParse parse;
    private final Rv32ImGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public ExecuteRiscV(
        Rv32ImParse parse,
        Rv32ImGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.parse = parse;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        // Parse
        final Result<IStrategoTerm, JsglrParseException> parseResult = context.require(parse.inputBuilder().withFile(input.file).buildAstSupplier());
        if(parseResult.isErr()) {
            return CommandFeedback.ofTryExtractMessagesFrom(parseResult.getErr());
        }
        final IStrategoTerm ast = parseResult.get();

        // Pretty-print
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        final IStrategoTerm prettyPrintedTerm;
        try {
            prettyPrintedTerm = strategoRuntime.invoke("pp-debug", ast);
        } catch(StrategoException e) {
            return CommandFeedback.ofTryExtractMessagesFrom(e);
        }
        final String prettyPrinted = TermUtils.asJavaString(prettyPrintedTerm)
            .orElseThrow(() -> new InvalidAstShapeException("pretty-printed RISC-V term", prettyPrintedTerm));

        // Assemble
        setupObjects();
        final AssemblerOutput assemblerOutput = Assembler.INSTANCE.assemble(prettyPrinted);
        if(assemblerOutput.getErrors().size() > 0) {
            final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
            for(AssemblerError error : assemblerOutput.getErrors()) {
                final @Nullable Region region;
                final @Nullable Integer line = error.getLine();
                if(line != null) {
                    region = Region.fromOffsets(0, 0, error.getLine());
                } else {
                    region = null;
                }
                messagesBuilder.addMessage(error.getMessage(), Severity.Error, input.file, region);
            }
            return CommandFeedback.of(messagesBuilder.build());
        }

        // Link
        final LinkedProgram program;
        try {
            program = Linker.INSTANCE.link(Arrays.asList(assemblerOutput.getProg()));
        } catch(Throwable e) {
            // HACK: throws AssemblerError, but Kotlin methods do not have checked exceptions, so we catch all.
            return CommandFeedback.ofTryExtractMessagesFrom(e);
        }

        // Simulate
        final PrintStream old = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out));
            final Simulator simulator = new Simulator(program);
            simulator.run();
        } finally {
            System.setOut(old);
        }
        return CommandFeedback.of(ShowFeedback.showText(out.toString(), "RISC-V simulation output for '" + input.file + "'"));
    }

    private void setupObjects() {
        AddKt.getAdd();
        AddiKt.getAddi();
        AndKt.getAnd();
        AndiKt.getAndi();
        AuipcKt.getAuipc();
        BeqKt.getBeq();
        BgeKt.getBge();
        BgeuKt.getBgeu();
        BltKt.getBlt();
        BltuKt.getBltu();
        BneKt.getBne();
        DivKt.getDiv();
        DivuKt.getDivu();
        EcallKt.getEcall();
        JalKt.getJal();
        JalrKt.getJalr();
        LbKt.getLb();
        LbuKt.getLbu();
        LhKt.getLh();
        LhuKt.getLhu();
        LuiKt.getLui();
        LwKt.getLw();
        MulKt.getMul();
        MulhKt.getMulh();
        MulhsuKt.getMulhsu();
        MulhuKt.getMulhu();
        OrKt.getOr();
        OriKt.getOri();
        RemKt.getRem();
        RemuKt.getRemu();
        SbKt.getSb();
        ShKt.getSh();
        SllKt.getSll();
        SlliKt.getSlli();
        SltKt.getSlt();
        SltiKt.getSlti();
        SltiuKt.getSltiu();
        SltuKt.getSltu();
        SraKt.getSra();
        SraiKt.getSrai();
        SrlKt.getSrl();
        SrliKt.getSrli();
        SubKt.getSub();
        SwKt.getSw();
        XorKt.getXor();
        XoriKt.getXori();
    }
}
