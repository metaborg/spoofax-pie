package mb.pipe.run.spoofax.esv;

import java.io.Serializable;

public class SortCons implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String sort;
    public final String cons;


    public SortCons(String sort, String cons) {
        this.sort = sort;
        this.cons = cons;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cons.hashCode();
        result = prime * result + sort.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SortCons other = (SortCons) obj;
        if(!sort.equals(other.sort))
            return false;
        if(!cons.equals(other.cons))
            return false;
        return true;
    }

    @Override public String toString() {
        return sort + "." + cons;
    }
}
