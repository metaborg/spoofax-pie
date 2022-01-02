package mb.spoofax.lwb.eclipse.generator;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.compiler.util.Conversion;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class LanguageProjectWizardPage extends WizardNewProjectCreationPage {
    private final Logger logger;

    private @MonotonicNonNull LanguageIdentifierControls languageIdentifier;

    private boolean fileExtensionsModified = false;
    private @MonotonicNonNull Text fileExtensions;

    private @MonotonicNonNull Button multiFileAnalysis;

    private boolean ignoreEvents = false;


    public LanguageProjectWizardPage(LoggerFactory loggerFactory) {
        super("page1");
        this.logger = loggerFactory.create(getClass());
        setTitle("Create Spoofax language project");
        setDescription("This wizard creates a Spoofax language project");
    }


    public String id() {
        return languageIdentifier.id();
    }

    public String name() {
        return languageIdentifier.name();
    }

    public String javaClassIdPrefix() {
        return languageIdentifier.javaClassIdPrefix();
    }

    public Collection<String> fileExtensions() {
        return Arrays.asList(splitFileExtensions(fileExtensions.getText()));
    }

    public boolean multiFileAnalysis() {
        return multiFileAnalysis.getSelection();
    }


    @Override public void createControl(Composite parent) {
        // Create a container for this page.
        final Composite container = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        container.setLayout(layout);

        // Create new project creation control, with as parent our container.
        super.createControl(container);
        // HACK: fix wrong layout data of the parent control
        getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        // Create language identifier controls, with as parent our container.
        languageIdentifier = new LanguageIdentifierControls(container, e -> {
            if(ignoreEvents) return;
            boolean valid = validatePage();
            setPageComplete(valid);
        });

        // Add extensions, with as parent the language identifier controls.
        new Label(languageIdentifier.container, SWT.NONE).setText("&Extensions:");
        fileExtensions = new Text(languageIdentifier.container, SWT.BORDER | SWT.SINGLE);
        fileExtensions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileExtensions.addModifyListener(e -> {
            if(ignoreEvents) return;
            fileExtensionsModified = true;
        });

        // Add language options in a new container
        final Group optionsContainer = new Group(container, SWT.NONE);
        final GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 2;
        optionsContainer.setLayout(optionsLayout);
        optionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        optionsContainer.setText("Language options");

        new Label(optionsContainer, SWT.NONE).setText("&Multi-file analysis:");
        multiFileAnalysis = new Button(optionsContainer, SWT.CHECK);
        multiFileAnalysis.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Modifying the project name fills in language name, language id, and extensions.
        try {
            // HACK: get project name input field via reflection because it is package private.
            final Field projectNameField = WizardNewProjectCreationPage.class.getDeclaredField("projectNameField");
            projectNameField.setAccessible(true);
            final Text projectNameInput = (Text)projectNameField.get(this);
            projectNameInput.addModifyListener(e -> {
                if(ignoreEvents) return;
                try {
                    ignoreEvents = true;
                    final String projectName = projectNameInput.getText();
                    languageIdentifier.updateFromProjectName(projectName);
                    if(!fileExtensionsModified) {
                        fileExtensions.setText(Conversion.nameToFileExtension(projectName));
                    }
                } finally {
                    ignoreEvents = false;
                }
            });
        } catch(IllegalAccessException | NoSuchFieldException e) {
            logger.error("Could not get project name input field via reflection, cannot automatically set names via the project name", e);
        }

        // Modifying the language name fills in extensions.
        languageIdentifier.name.addModifyListener(e -> {
            if(ignoreEvents) return;
            try {
                ignoreEvents = true;
                if(!fileExtensionsModified) {
                    fileExtensions.setText(Conversion.nameToFileExtension(languageIdentifier.name()));
                }
            } finally {
                ignoreEvents = false;
            }
        });

        // Set the current control to our container.
        setControl(container);
    }

    @Override protected boolean validatePage() {
        // Validation can be executed before control has been made, check for null to confirm.
        if(languageIdentifier == null) {
            return super.validatePage();
        }
        final ValidationResult languageIdentifierValidation = languageIdentifier.validate();
        final ArrayList<String> errors = languageIdentifierValidation.errors;
        boolean complete = languageIdentifierValidation.complete;

        final String fileExtensionsText = this.fileExtensions.getText();
        final Collection<String> fileExtensions = fileExtensions();
        if(fileExtensionsText.isEmpty() || fileExtensions.isEmpty()) {
            complete = false;
            if(fileExtensionsModified) {
                errors.add("At least one extension must be specified");
            }
        }

        if(complete) {
            return super.validatePage();
        }

        if(!errors.isEmpty()) {
            final String error = errors.get(0);
            setErrorMessage(error);
        } else {
            setErrorMessage(null);
        }

        return false;
    }

    public static String[] splitFileExtensions(final String extensions) {
        return extensions.split(",");
    }
}
