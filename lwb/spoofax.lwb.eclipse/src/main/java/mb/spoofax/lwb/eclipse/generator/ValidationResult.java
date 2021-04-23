package mb.spoofax.lwb.eclipse.generator;

import java.util.ArrayList;

class ValidationResult {
    public final ArrayList<String> errors;
    public final boolean complete;

    ValidationResult(ArrayList<String> errors, boolean complete) {
        this.errors = errors;
        this.complete = complete;
    }
}
