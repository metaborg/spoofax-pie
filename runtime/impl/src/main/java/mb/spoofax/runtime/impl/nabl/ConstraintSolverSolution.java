package mb.spoofax.runtime.impl.nabl;

import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.message.PathMsg;

import java.io.Serializable;
import java.util.ArrayList;

public class ConstraintSolverSolution implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<PathMsg> fileMessages;
    private ArrayList<Msg> projectMessages;
//    private ArrayList<PathMsg> fileUnsolvedMessages;
//    private ArrayList<Msg> projectUnsolvedMessages;


    public ConstraintSolverSolution(ArrayList<PathMsg> fileMessages, ArrayList<Msg> projectMessages
//        , ArrayList<PathMsg> fileUnsolvedMessages, ArrayList<Msg> projectUnsolvedMessages
    ) {
        this.fileMessages = fileMessages;
        this.projectMessages = projectMessages;
//        this.fileUnsolvedMessages = fileUnsolvedMessages;
//        this.projectUnsolvedMessages = projectUnsolvedMessages;
    }


    public ArrayList<PathMsg> getFileMessages() {
        return fileMessages;
    }

    public ArrayList<Msg> getProjectMessages() {
        return projectMessages;
    }
//
//    public ArrayList<PathMsg> getFileUnsolvedMessages() {
//        return fileUnsolvedMessages;
//    }
//
//    public ArrayList<Msg> getProjectUnsolvedMessages() {
//        return projectUnsolvedMessages;
//    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ConstraintSolverSolution that = (ConstraintSolverSolution) o;
        if(!fileMessages.equals(that.fileMessages)) return false;
        if(!projectMessages.equals(that.projectMessages)) return false;
//        if(!fileUnsolvedMessages.equals(that.fileUnsolvedMessages)) return false;
//        if(!projectUnsolvedMessages.equals(that.projectUnsolvedMessages)) return false;
        return true;
    }

    @Override public int hashCode() {
        int result = fileMessages.hashCode();
        result = 31 * result + projectMessages.hashCode();
//        result = 31 * result + fileUnsolvedMessages.hashCode();
//        result = 31 * result + projectUnsolvedMessages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ConstraintSolverSolution(" +
            "fileMessages=" + fileMessages +
            ", projectMessages=" + projectMessages +
//            ", fileUnsolvedMessages=" + fileUnsolvedMessages +
//            ", projectUnsolvedMessages=" + projectUnsolvedMessages +
            ')';
    }
}
