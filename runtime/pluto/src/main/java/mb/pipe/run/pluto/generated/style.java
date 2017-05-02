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

public class style extends ABuilder<style.Input, style.Output> {
    public static class Input extends AInput{
      private static final long serialVersionUID = 1L;
      
      public final Collection<mb.pipe.run.core.model.parse.IToken> tokenStream;
      public final mb.pipe.run.core.model.IContext context;
      
      public Input(IContext _internal_context, @Nullable Origin origin, Collection<mb.pipe.run.core.model.parse.IToken> tokenStream, mb.pipe.run.core.model.IContext context) {
          super(_internal_context, origin);
          this.tokenStream = tokenStream;
          this.context = context;
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return new mb.pipe.run.core.util.Tuple(tokenStream, context);
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((tokenStream == null) ? 0 : tokenStream.hashCode());
          result = prime * result + ((context == null) ? 0 : context.hashCode());
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Input other = (Input) obj;
          if(tokenStream == null) { if(other.tokenStream != null) return false; } else if(!tokenStream.equals(other.tokenStream)) return false;
          if(context == null) { if(other.context != null) return false; } else if(!context.equals(other.context)) return false;
          return true;
      }
    }

    public static class Output implements build.pluto.output.Output{
      private static final long serialVersionUID = 1L;
      
      public final mb.pipe.run.core.model.style.IStyling out;
      
      public Output(mb.pipe.run.core.model.style.IStyling out) {
          
          this.out = out;
      }
      
      public mb.pipe.run.core.model.style.IStyling getPipeVal() {
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


    public static final BuilderFactory<Input, Output, style> factory = factory(style.class, Input.class);

    public static BuildRequest<Input, Output, style, BuilderFactory<Input, Output, style>> request(Input input) {
        return request(input, style.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, style.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, style.class, Input.class);
    }
    
    public static mb.pipe.run.core.model.style.IStyling build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, style.class, Input.class).output.getPipeVal();
    }


    public style(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "style";
    }

    @Override public File persistentPath(Input input) {
        return depFile("style", input.tokenStream, input.context);
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        
        mb.pipe.run.core.vfs.IResource currentDir = input.context.currentDir();
        
        mb.pipe.run.core.vfs.IResource langLoc = mb.pipe.run.core.vfs.VFSResource.resolveStatic("/Users/gohla/.m2/repository/org/metaborg/org.metaborg.meta.lang.esv/2.3.0-SNAPSHOT/org.metaborg.meta.lang.esv-2.3.0-SNAPSHOT.spoofax-language");
        
        mb.pipe.run.core.vfs.IResource specDir = currentDir;
        
        mb.pipe.run.core.vfs.IResource mainFile = currentDir.resolve("editor/Main.esv");
        
        Collection<mb.pipe.run.core.vfs.IResource> includedFiles = Lists.newArrayList();
        final Result<mb.pipe.run.pluto.esv.GenerateStylerRules.Output> init2 = mb.pipe.run.pluto.esv.GenerateStylerRules.requireBuild(this, new mb.pipe.run.pluto.esv.GenerateStylerRules.Input(input.context, null, langLoc, specDir, mainFile, includedFiles));
        @Nullable mb.pipe.run.spoofax.esv.StylingRules syntaxStyler = init2.output.getPipeVal();
        
        if(syntaxStyler == null)
            
            throw new RuntimeException("Unable to build syntax styler".toString());
        final Result<mb.pipe.run.pluto.esv.Style.Output> init3 = mb.pipe.run.pluto.esv.Style.requireBuild(this, new mb.pipe.run.pluto.esv.Style.Input(input.context, null, input.tokenStream, syntaxStyler));
        return new Output(init3.output.getPipeVal());
        
    }
}
