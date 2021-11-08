package mb.rv32im.task;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.rv32im.Rv32ImScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;
import venus.assembler.Assembler;
import venus.assembler.AssemblerError;
import venus.assembler.AssemblerOutput;
import venus.linker.LinkedProgram;
import venus.linker.Linker;
import venus.riscv.insts.AddKt;
import venus.riscv.insts.AddiKt;
import venus.riscv.insts.AndKt;
import venus.riscv.insts.AndiKt;
import venus.riscv.insts.AuipcKt;
import venus.riscv.insts.BeqKt;
import venus.riscv.insts.BgeKt;
import venus.riscv.insts.BgeuKt;
import venus.riscv.insts.BltKt;
import venus.riscv.insts.BltuKt;
import venus.riscv.insts.BneKt;
import venus.riscv.insts.DivKt;
import venus.riscv.insts.DivuKt;
import venus.riscv.insts.EcallKt;
import venus.riscv.insts.JalKt;
import venus.riscv.insts.JalrKt;
import venus.riscv.insts.LbKt;
import venus.riscv.insts.LbuKt;
import venus.riscv.insts.LhKt;
import venus.riscv.insts.LhuKt;
import venus.riscv.insts.LuiKt;
import venus.riscv.insts.LwKt;
import venus.riscv.insts.MulKt;
import venus.riscv.insts.MulhKt;
import venus.riscv.insts.MulhsuKt;
import venus.riscv.insts.MulhuKt;
import venus.riscv.insts.OrKt;
import venus.riscv.insts.OriKt;
import venus.riscv.insts.RemKt;
import venus.riscv.insts.RemuKt;
import venus.riscv.insts.SbKt;
import venus.riscv.insts.ShKt;
import venus.riscv.insts.SllKt;
import venus.riscv.insts.SlliKt;
import venus.riscv.insts.SltKt;
import venus.riscv.insts.SltiKt;
import venus.riscv.insts.SltiuKt;
import venus.riscv.insts.SltuKt;
import venus.riscv.insts.SraKt;
import venus.riscv.insts.SraiKt;
import venus.riscv.insts.SrlKt;
import venus.riscv.insts.SrliKt;
import venus.riscv.insts.SubKt;
import venus.riscv.insts.SwKt;
import venus.riscv.insts.XorKt;
import venus.riscv.insts.XoriKt;
import venus.simulator.Simulator;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Inject public ExecuteRiscV() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) {
        final ResourceKey file = input.file;
        final String text;
        try {
            text = context.require(input.file).readString();
        } catch(IOException e) {
            return CommandFeedback.ofTryExtractMessagesFrom(e, file);
        }

        // Assemble
        setupObjects();
        final AssemblerOutput assemblerOutput = Assembler.INSTANCE.assemble(text);
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
                messagesBuilder.addMessage(error.getMessage(), Severity.Error, file, region);
            }
            return CommandFeedback.of(messagesBuilder.build(file));
        }

        // Link
        final LinkedProgram program;
        try {
            program = Linker.INSTANCE.link(Arrays.asList(assemblerOutput.getProg()));
        } catch(Throwable e) {
            // HACK: throws AssemblerError, but Kotlin methods do not have checked exceptions, so we catch all.
            return CommandFeedback.ofTryExtractMessagesFrom(e, file);
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
