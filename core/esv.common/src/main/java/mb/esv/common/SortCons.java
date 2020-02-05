package mb.esv.common;

import java.io.Serializable;

class SortCons implements Serializable {
    final String sort;
    final String cons;


    SortCons(String sort, String cons) {
        this.sort = sort;
        this.cons = cons;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SortCons sortCons = (SortCons) o;
        if(!sort.equals(sortCons.sort)) return false;
        return cons.equals(sortCons.cons);
    }

    @Override public int hashCode() {
        int result = sort.hashCode();
        result = 31 * result + cons.hashCode();
        return result;
    }

    @Override public String toString() {
        return sort + "." + cons;
    }
}
