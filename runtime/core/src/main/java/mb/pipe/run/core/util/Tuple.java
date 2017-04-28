package mb.pipe.run.core.util;

public class Tuple implements ITuple {
    private static final long serialVersionUID = 1L;

    private final Object[] objects;


    public Tuple(Object... objects) {
        this.objects = objects;
    }


    @Override public Object get(int index) {
        return objects[index];
    }
}
