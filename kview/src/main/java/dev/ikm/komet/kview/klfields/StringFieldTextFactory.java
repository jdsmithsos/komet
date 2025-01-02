package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.FieldFactory;
import dev.ikm.komet.layout.component.version.field.KlField;
import jdk.jfr.Description;
import jdk.jfr.Name;

/**
 * The StringFieldTextFactory class is a factory for creating string fields
 * that utilize a TextField for presentation and editing. The field produced
 * by this factory is editable as it leverages the TextField's built-in editing
 * capabilities.
 *
 * This class implements the {@link FieldFactory} interface, specializing
 * in creating fields of type String. The fields are generated by associating
 * an {@link ObservableField} and an {@link ObservableView}, enabling dynamic
 * binding and updating of values between the UI and the underlying data model.
 *
 *
 * Main responsibilities:
 * - Create and return a new instance of {@link KlField} that represents
 *   a string field.
 * - Provide metadata about the type of field produced, namely {@link StringFieldText}.
 */
public class StringFieldTextFactory implements FieldFactory<String> {
    public StringFieldTextFactory() {
    }

    /**
     * Creates and returns a new instance of {@link KlField} that represents a string field
     * by associating the specified {@link ObservableField} and {@link ObservableView}.
     *
     * @param observableField The observable field of type String to be associated with the field.
     * @param observableView  The observable view that provides context and coordination for the field.
     * @return A {@link KlField} instance representing the created string field.
     */
    @Override
    public KlField<String> create(ObservableField<String> observableField, ObservableView observableView) {
        return StringFieldText.create(observableField, observableView);
    }

    @Override
    public Class<StringFieldText> getFieldInterface() {
        return StringFieldText.class;
    }

    @Override
    public String getName() {
        return "Editable String Field Factory";
    }

    @Override
    public String getDescription() {
        return "A String field that uses a TextField to present the field. " +
                "The field IS editable because TextField supports editing.";
    }
}
