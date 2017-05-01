package mb.pipe.run.pluto.generated;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.pluto.util.ABuilder;
import mb.pipe.run.pluto.util.AInput;
import mb.pipe.run.pluto.util.Result;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;

public class editorPipeline extends ABuilder<editorPipeline.Input, editorPipeline.Output> {
    public static class Input extends AInput{
      private static final long serialVersionUID = 1L;
      
      public final String text;
      public final mb.pipe.run.core.model.IContext context;
      
      public Input(IContext _internal_context, @Nullable Origin origin, String text, mb.pipe.run.core.model.IContext context) {
          super(_internal_context, origin);
          this.text = text;
          this.context = context;
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return new mb.pipe.run.core.util.Tuple(text, context);
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((text == null) ? 0 : text.hashCode());
          result = prime * result + ((context == null) ? 0 : context.hashCode());
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Input other = (Input) obj;
          if(text == null) { if(other.text != null) return false; } else if(!text.equals(other.text)) return false;
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


    public static final BuilderFactory<Input, Output, editorPipeline> factory = factory(editorPipeline.class, Input.class);

    public static BuildRequest<Input, Output, editorPipeline, BuilderFactory<Input, Output, editorPipeline>> request(Input input) {
        return request(input, editorPipeline.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, editorPipeline.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, editorPipeline.class, Input.class);
    }
    
    public static mb.pipe.run.core.util.ITuple build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, editorPipeline.class, Input.class).output.getPipeVal();
    }


    public editorPipeline(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "editorPipeline";
    }

    @Override public File persistentPath(Input input) {
        return depFile("editorPipeline", input.text, input.context);
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        final Result<parse.Output> init7 = parse.requireBuild(this, new parse.Input(input.context, null, input.text, input.context));
        org.spoofax.interpreter.terms.IStrategoTerm ast = (org.spoofax.interpreter.terms.IStrategoTerm) init7.output.getPipeVal().get(0);
        Collection<mb.pipe.run.core.model.parse.IToken> tokenStream = (Collection<mb.pipe.run.core.model.parse.IToken>) init7.output.getPipeVal().get(1);
        Collection<mb.pipe.run.core.model.message.IMsg> messages = (Collection<mb.pipe.run.core.model.message.IMsg>) init7.output.getPipeVal().get(2);
        @Nullable mb.pipe.run.core.model.style.IStyling styling;
        
        if(tokenStream != null)
            {
              final Result<style.Output> init8 = style.requireBuild(this, new style.Input(input.context, null, tokenStream, input.context));
              styling = init8.output.getPipeVal();
            }
        else
            {
              
              styling = null;
            }
        
        return new Output(new mb.pipe.run.core.util.Tuple(input.text, ast, tokenStream, messages, styling));
        
    }
}
