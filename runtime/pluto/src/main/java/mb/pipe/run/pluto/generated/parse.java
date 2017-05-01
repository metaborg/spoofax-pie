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

public class parse extends ABuilder<parse.Input, parse.Output> {
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


    public static final BuilderFactory<Input, Output, parse> factory = factory(parse.class, Input.class);

    public static BuildRequest<Input, Output, parse, BuilderFactory<Input, Output, parse>> request(Input input) {
        return request(input, parse.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, parse.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, parse.class, Input.class);
    }
    
    public static mb.pipe.run.core.util.ITuple build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, parse.class, Input.class).output.getPipeVal();
    }


    public parse(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "parse";
    }

    @Override public File persistentPath(Input input) {
        return depFile("parse", input.text, input.context);
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        
        mb.pipe.run.core.vfs.IResource currentDir = input.context.currentDir();
        
        mb.pipe.run.core.vfs.IResource langLoc = mb.pipe.run.core.vfs.VFSResource.resolveStatic("/Users/gohla/.m2/repository/org/metaborg/org.metaborg.meta.lang.template/2.2.0/org.metaborg.meta.lang.template-2.2.0.spoofax-language");
        
        mb.pipe.run.core.vfs.IResource specDir = currentDir;
        
        mb.pipe.run.core.vfs.IResource mainFile = currentDir.resolve("syntax/minimal.sdf3");
        
        Collection<mb.pipe.run.core.vfs.IResource> includedFiles = Lists.newArrayList();
        final Result<mb.pipe.run.pluto.sdf.GenerateTable.Output> init0 = mb.pipe.run.pluto.sdf.GenerateTable.requireBuild(this, new mb.pipe.run.pluto.sdf.GenerateTable.Input(input.context, null, langLoc, specDir, mainFile, includedFiles));
        @Nullable mb.pipe.run.spoofax.sdf.Table parseTable = init0.output.getPipeVal();
        
        if(parseTable == null)
            
            throw new RuntimeException("Unable to build parse table".toString());
        final Result<mb.pipe.run.pluto.sdf.Parse.Output> init1 = mb.pipe.run.pluto.sdf.Parse.requireBuild(this, new mb.pipe.run.pluto.sdf.Parse.Input(input.context, null, input.text, "Start", parseTable));
        return new Output(init1.output.getPipeVal());
        
    }
}
