package mb.spoofax.runtime.sdf3;

import mb.fs.java.JavaFSPath;

import java.io.Serializable;
import java.util.ArrayList;

public class Signatures implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<JavaFSPath> signatureFiles;
    private final JavaFSPath includeDir;


    public Signatures(ArrayList<JavaFSPath> signatureFiles, JavaFSPath includeDir) {
        this.signatureFiles = signatureFiles;
        this.includeDir = includeDir;
    }


    public ArrayList<JavaFSPath> signatureFiles() {
        return signatureFiles;
    }

    public JavaFSPath includeDir() {
        return includeDir;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + signatureFiles.hashCode();
        result = prime * result + includeDir.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Signatures other = (Signatures) obj;
        if(!signatureFiles.equals(other.signatureFiles))
            return false;
        return includeDir.equals(other.includeDir);
    }

    @Override public String toString() {
        return "Signatures(signatureFiles=" + signatureFiles + ", includeDir=" + includeDir + ")";
    }
}
