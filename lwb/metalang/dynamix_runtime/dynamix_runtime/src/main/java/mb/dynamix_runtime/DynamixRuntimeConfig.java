package mb.dynamix_runtime;

import java.io.Serializable;

public class DynamixRuntimeConfig implements Serializable {
    public final String mainRuleName;

    public DynamixRuntimeConfig(String mainRuleName) {
        this.mainRuleName = mainRuleName;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        DynamixRuntimeConfig that = (DynamixRuntimeConfig)o;

        return mainRuleName.equals(that.mainRuleName);
    }

    @Override public int hashCode() {
        return mainRuleName.hashCode();
    }

    @Override public String toString() {
        return "DynamixRuntimeConfig{" +
            "mainRuleName='" + mainRuleName + '\'' +
            '}';
    }
}
