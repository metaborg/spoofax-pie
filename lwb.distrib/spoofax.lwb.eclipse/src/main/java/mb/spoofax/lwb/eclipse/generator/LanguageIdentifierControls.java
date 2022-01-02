package mb.spoofax.lwb.eclipse.generator;

import mb.common.util.StringUtil;
import mb.spoofax.compiler.util.Conversion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;

public class LanguageIdentifierControls {
    protected final Group container;

    protected final Text id;
    protected final Text name;
    protected final Text javaClassIdPrefix;

    private boolean idModified = false;
    private boolean nameModified = false;
    private boolean javaClassIdPrefixModified = false;

    private boolean ignoreEvents = false;

    public LanguageIdentifierControls(Composite parent, ModifyListener parentListener) {
        container = new Group(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        container.setText("Language identification");

        new Label(container, SWT.NONE).setText("&Identifier:");
        id = new Text(container, SWT.BORDER | SWT.SINGLE);
        id.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        id.addModifyListener(e -> {
            if(ignoreEvents) return;
            idModified = true;
            parentListener.modifyText(e);
        });

        new Label(container, SWT.NONE).setText("&Name:");
        name = new Text(container, SWT.BORDER | SWT.SINGLE);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.addModifyListener(e -> {
            if(ignoreEvents) return;
            nameModified = true;
            parentListener.modifyText(e);
        });

        new Label(container, SWT.NONE).setText("&Java class ID prefix:");
        javaClassIdPrefix = new Text(container, SWT.BORDER | SWT.SINGLE);
        javaClassIdPrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        javaClassIdPrefix.addModifyListener(e -> {
            if(ignoreEvents) return;
            javaClassIdPrefixModified = true;
            parentListener.modifyText(e);
        });
    }

    public void updateFromProjectName(String projectName) {
        ignoreEvents = true;
        if(!idModified) {
            id.setText(Conversion.nameToJavaPackageId(projectName));
        }
        if(!nameModified) {
            name.setText(Conversion.nameToJavaId(projectName));
        }
        if(!javaClassIdPrefixModified) {
            javaClassIdPrefix.setText(StringUtil.capitalize(Conversion.nameToJavaId(projectName)));
        }
        ignoreEvents = false;
    }

    public ValidationResult validate() {
        final ArrayList<String> errors = new ArrayList<>();
        boolean complete = true;
        final String name = name();
        if(nameModified) {
            if(name.isEmpty()) {
                errors.add("Language name must be specified");
            }
            // TODO: validate name
        } else if(name.isEmpty()) {
            complete = false;
        }

        final String id = id();
        if(idModified) {
            if(id.isEmpty()) {
                errors.add("Identifier must be specified");
            }
            // TODO: validate id
        } else if(id.isEmpty()) {
            complete = false;
        }

        final String javaClassIdPrefix = javaClassIdPrefix();
        if(javaClassIdPrefixModified) {
            if(javaClassIdPrefix.isEmpty()) {
                errors.add("Java class ID prefix must be specified");
            }
            // TODO: validate javaClassIdPrefix
        } else if(javaClassIdPrefix.isEmpty()) {
            complete = false;
        }

        return new ValidationResult(errors, complete);
    }

    public String id() {
        return id.getText();
    }

    public String name() {
        return name.getText();
    }

    public String javaClassIdPrefix() {
        return javaClassIdPrefix.getText();
    }
}
