package mb.pipe.run.pluto.generated;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;

public class filePipeline extends ABuilder<filePipeline.Input, filePipeline.Output> {
    public static class Input extends AInput{
      private static final long serialVersionUID = 1L;
      
      public final mb.pipe.run.core.vfs.IResource file;
      
      public Input(File depDir, @Nullable Origin origin, mb.pipe.run.core.vfs.IResource file) {
          super(depDir, origin);
          this.file = file;
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return new mb.pipe.run.core.util.Tuple(file);
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((file == null) ? 0 : file.hashCode());
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Input other = (Input) obj;
          if(file == null) { if(other.file != null) return false; } else if(!file.equals(other.file)) return false;
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


    public static final BuilderFactory<Input, Output, filePipeline> factory = factory(filePipeline.class, Input.class);

    public static BuildRequest<Input, Output, filePipeline, BuilderFactory<Input, Output, filePipeline>> request(Input input) {
        return request(input, filePipeline.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, filePipeline.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, filePipeline.class, Input.class);
    }
    
    public static mb.pipe.run.core.util.ITuple build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, filePipeline.class, Input.class).output.getPipeVal();
    }


    public filePipeline(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "filePipeline";
    }

    @Override public File persistentPath(Input input) {
        return depFile("filePipeline", input.file);
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final Result<mb.pipe.run.pluto.vfs.Read.Output> init4 = mb.pipe.run.pluto.vfs.Read.requireBuild(this, new mb.pipe.run.pluto.vfs.Read.Input(input.depDir, null, input.file));
        String text = init4.output.getPipeVal();
        final Result<parse.Output> init5 = parse.requireBuild(this, new parse.Input(input.depDir, null, text));
        org.spoofax.interpreter.terms.IStrategoTerm ast = (org.spoofax.interpreter.terms.IStrategoTerm) init5.output.getPipeVal().get(0);
        Collection<mb.pipe.run.core.model.parse.IToken> tokenStream = (Collection<mb.pipe.run.core.model.parse.IToken>) init5.output.getPipeVal().get(1);
        Collection<mb.pipe.run.core.model.message.IMsg> messages = (Collection<mb.pipe.run.core.model.message.IMsg>) init5.output.getPipeVal().get(2);
        @Nullable mb.pipe.run.core.model.style.IStyling styling;
        
        if(tokenStream != null)
            {
              final Result<style.Output> init6 = style.requireBuild(this, new style.Input(input.depDir, null, tokenStream));
              styling = init6.output.getPipeVal();
            }
        else
            {
              
              styling = null;
            }
        
        return new Output(new mb.pipe.run.core.util.Tuple(input.file, text, ast, tokenStream, messages, styling));
        
    }
}
