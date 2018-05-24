package mb.spoofax.runtime.sdf;

import mb.pie.vfs.path.PPath;

import java.io.Serializable;
import java.util.ArrayList;

public class Signatures implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<PPath> signatureFiles;
    private final PPath includeDir;


    public Signatures(ArrayList<PPath> signatureFiles, PPath includeDir) {
        this.signatureFiles = signatureFiles;
        this.includeDir = includeDir;
    }


    public ArrayList<PPath> signatureFiles() {
        return signatureFiles;
    }

    public PPath includeDir() {
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
