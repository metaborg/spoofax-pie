package mb.pipe.run.spoofax.sdf;

public interface IParseOutput {
    void accept(IParseOutputVisitor visitor);
}
