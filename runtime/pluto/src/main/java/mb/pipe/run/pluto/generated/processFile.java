package mb.pipe.run.pluto.generated;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.util.Lists2;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;

public class processFile extends ABuilder<processFile.Input, processFile.Output> {
    public static class Input extends AInput{
      private static final long serialVersionUID = 1L;
      
      public final mb.pipe.run.core.vfs.IResource file;
      public final mb.pipe.run.core.model.IContext context;
      
      public Input(IContext _internal_context, @Nullable Origin origin, mb.pipe.run.core.vfs.IResource file, mb.pipe.run.core.model.IContext context) {
          super(_internal_context, origin);
          this.file = file;
          this.context = context;
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return new mb.pipe.run.core.util.Tuple(file, context);
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((file == null) ? 0 : file.hashCode());
          result = prime * result + ((context == null) ? 0 : context.hashCode());
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Input other = (Input) obj;
          if(file == null) { if(other.file != null) return false; } else if(!file.equals(other.file)) return false;
          if(context == null) { if(other.context != null) return false; } else if(!context.equals(other.context)) return false;
          return true;
      }
    }

    public static class Output implements build.pluto.output.Output{
      private static final long serialVersionUID = 1L;
      
      public final mb.pipe.run.core.util.ITuple out;
      
      public Output(mb.pipe.run.core.util.ITuple out) {
          
          this.out = out;
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return out;
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((out == null) ? 0 : out.hashCode());
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Output other = (Output) obj;
          if(out == null) { if(other.out != null) return false; } else if(!out.equals(other.out)) return false;
          return true;
      }
    }


    public static final BuilderFactory<Input, Output, processFile> factory = factory(processFile.class, Input.class);

    public static BuildRequest<Input, Output, processFile, BuilderFactory<Input, Output, processFile>> request(Input input) {
        return request(input, processFile.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, processFile.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, processFile.class, Input.class);
    }
    
    public static mb.pipe.run.core.util.ITuple build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, processFile.class, Input.class).output.getPipeVal();
    }


    public processFile(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "processFile";
    }

    @Override public File persistentPath(Input input) {
        return depFile("processFile", input.file, input.context);
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final Result<mb.pipe.run.pluto.vfs.Read.Output> init4 = mb.pipe.run.pluto.vfs.Read.requireBuild(this, new mb.pipe.run.pluto.vfs.Read.Input(input.context, null, input.file));
        String text = init4.output.getPipeVal();
        final Result<processString.Output> init5 = processString.requireBuild(this, new processString.Input(input.context, null, text, input.context));
        String _ = (String) init5.output.getPipeVal().get(0);
        org.spoofax.interpreter.terms.IStrategoTerm ast = (org.spoofax.interpreter.terms.IStrategoTerm) init5.output.getPipeVal().get(1);
        Collection<mb.pipe.run.core.model.parse.IToken> tokenStream = (Collection<mb.pipe.run.core.model.parse.IToken>) init5.output.getPipeVal().get(2);
        Collection<mb.pipe.run.core.model.message.IMsg> messages = (Collection<mb.pipe.run.core.model.message.IMsg>) init5.output.getPipeVal().get(3);
        mb.pipe.run.core.model.style.IStyling styling = (mb.pipe.run.core.model.style.IStyling) init5.output.getPipeVal().get(4);
        
        return new Output(new mb.pipe.run.core.util.Tuple(input.file, text, ast, tokenStream, messages, styling));
        
    }
}
