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

public class main extends ABuilder<main.Input, main.Output> {
    public static class Input extends AInput{
      private static final long serialVersionUID = 1L;
      
      
      
      public Input(File depDir, @Nullable Origin origin) {
          super(depDir, origin);
          
      }
      
      public mb.pipe.run.core.util.ITuple getPipeVal() {
          return new mb.pipe.run.core.util.Tuple();
      }
      
      @Override public int hashCode() {
          final int prime = 31;
          int result = 1;
          
          return result;
      }
      
      @Override public boolean equals(Object obj) {
          if(this == obj) return true;
          if(obj == null) return false;
          if(getClass() != obj.getClass()) return false;
          final Input other = (Input) obj;
          
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


    public static final BuilderFactory<Input, Output, main> factory = factory(main.class, Input.class);

    public static BuildRequest<Input, Output, main, BuilderFactory<Input, Output, main>> request(Input input) {
        return request(input, main.class, Input.class);
    }

    public static Origin origin(Input input) {
        return origin(input, main.class, Input.class);
    }

    public static Result<Output> requireBuild(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, main.class, Input.class);
    }
    
    public static mb.pipe.run.core.util.ITuple build(Builder<?, ?> requiree, Input input) throws IOException {
        return requireBuild(requiree, input, main.class, Input.class).output.getPipeVal();
    }


    public main(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "main";
    }

    @Override public File persistentPath(Input input) {
        return depFile("main");
    }

    @SuppressWarnings("unchecked") @Override protected Output build(Input input) throws Throwable {
        requireOrigins();

        
        mb.pipe.run.core.vfs.IResource file = mb.pipe.run.core.vfs.VFSResource.resolveStatic("example/test.min");
        final Result<filePipeline.Output> init4 = filePipeline.requireBuild(this, new filePipeline.Input(input.depDir, null, file));
        return new Output(init4.output.getPipeVal());
        
    }
}
