package mb.pipe.run.pluto.util;

import build.pluto.dependency.Origin;

public class Result<T> {
    public final T output;
    public final Origin origin;


    public Result(T output, Origin origin) {
        this.output = output;
        this.origin = origin;
    }
}
