package mb.spoofax.runtime.util;

import mb.pie.vfs.path.PPath;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Arguments implements Iterable<Object>, Serializable {
    private static final long serialVersionUID = 1l;
    private static final Pattern spaces = Pattern.compile("[\\s]");

    private final ArrayList<Object> arguments;


    public Arguments() {
        this.arguments = new ArrayList<>();
    }

    public Arguments(Arguments other) {
        this.arguments = new ArrayList<>(other.arguments);
    }


    public Arguments add(Object arg) {
        if(arg == null || (arg instanceof String && ((String) arg).isEmpty())) {
            return this;
        }
        arguments.add(arg);
        return this;
    }

    public Arguments add(Object arg0, Object arg1) {
        add(arg0);
        add(arg1);
        return this;
    }

    public Arguments add(Object... args) {
        for(Object arg : args) {
            add(arg);
        }
        return this;
    }

    public Arguments addLine(String line) {
        add((Object[]) spaces.split(line));
        return this;
    }

    public Arguments addPath(PPath path) {
        add(path);
        return this;
    }

    public Arguments addPath(String flag, PPath path) {
        add(flag);
        addPath(path);
        return this;
    }

    public Arguments addPaths(PPath... paths) {
        for(PPath path : paths) {
            addPath(path);
        }
        return this;
    }

    public Arguments addAll(Arguments args) {
        addAll(args.arguments);
        return this;
    }

    public Arguments addAll(Iterable<Object> args) {
        for(Object obj : args) {
            add(obj);
        }
        return this;
    }


    public Arguments clear() {
        arguments.clear();
        return this;
    }

    /**
     * Returns the arguments as an iterable of strings.
     *
     * @param baseDir The working directory relative to which the paths have to be; or <code>null</code> to keep paths
     *                absolute.
     * @return An iterable of strings.
     */
    public ArrayList<String> asStrings(@Nullable PPath baseDir) {
        final ArrayList<String> result = new ArrayList<>(this.size());
        for(Object arg : this) {
            result.add(asString(arg, baseDir));
        }
        return result;
    }

    private String asString(Object arg, @Nullable PPath baseDir) {
        if(arg instanceof PPath) {
            final PPath path = (PPath) arg;
            final String pathStr;
            if(baseDir != null) {
                pathStr = baseDir.relativizeStringFrom(path);
            } else {
                pathStr = path.toString();
            }
            return StringUtils.fixFileSeparatorChar(pathStr);
        } else {
            return arg.toString();
        }
    }


    public int size() {
        return this.arguments.size();
    }

    @Override
    public Iterator<Object> iterator() {
        return arguments.iterator();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arguments.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Arguments other = (Arguments) obj;
        return arguments.equals(other.arguments);
    }

    @Override
    public String toString() {
        boolean first = true;
        final StringBuilder sb = new StringBuilder();
        for(String arg : asStrings(null)) {
            if(!first) {
                sb.append(' ');
            }
            sb.append(StringUtils.quoteArgument(arg));
            first = false;
        }
        return sb.toString();
    }
}
