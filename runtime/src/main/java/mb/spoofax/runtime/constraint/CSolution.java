package mb.spoofax.runtime.constraint;

import mb.spoofax.api.message.Msg;
import mb.spoofax.api.message.PathMsg;

import java.io.Serializable;
import java.util.ArrayList;

public class CSolution implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<PathMsg> fileMessages;
    private ArrayList<Msg> projectMessages;


    public CSolution(ArrayList<PathMsg> fileMessages, ArrayList<Msg> projectMessages) {
        this.fileMessages = fileMessages;
        this.projectMessages = projectMessages;
    }


    public ArrayList<PathMsg> getFileMessages() {
        return fileMessages;
    }

    public ArrayList<Msg> getProjectMessages() {
        return projectMessages;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CSolution that = (CSolution) o;
        if(!fileMessages.equals(that.fileMessages)) return false;
        return projectMessages.equals(that.projectMessages);
    }

    @Override public int hashCode() {
        int result = fileMessages.hashCode();
        result = 31 * result + projectMessages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ConstraintSolverSolution(" +
            "fileMessages=" + fileMessages +
            ", projectMessages=" + projectMessages +
            ')';
    }
}
