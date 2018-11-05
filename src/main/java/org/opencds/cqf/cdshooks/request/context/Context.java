package org.opencds.cqf.cdshooks.request.context;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.opencds.cqf.cdshooks.request.Field;
import org.opencds.cqf.exceptions.InvalidRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Context extends Field<Object> {

    public static final String name = "context";

    private FhirContext fhirContext;

    private PatientId patientId;
    public PatientId getPatientId() {
        return patientId;
    }

    private EncounterId encounterId;
    public EncounterId getEncounterId() {
        return encounterId;
    }

    private Medications medications;
    public Medications getMedications() {
        return medications;
    }

    private Orders orders;
    public Orders getOrders() {
        return orders;
    }

    public Context(Object value, FhirContext fhirContext) {
        this.fhirContext = fhirContext;
        setValue(value);
        setOptional(false);
    }

    private List<Field> getFieldList() {
        List<Field> fields = new ArrayList<>();
        fields.add(patientId == null ? new PatientId(null) : patientId);
        fields.add(encounterId == null ? new EncounterId(null) : encounterId);
        fields.add(medications == null ? new Medications(null) : medications);
        fields.add(orders == null ? new Orders(null) : orders);
        return fields;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof JsonObject) {
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) value).entrySet()) {
                switch (entry.getKey()) {
                    case PatientId.name:
                        patientId = new PatientId(Field.getFieldString(entry.getValue()));
                        break;
                    case EncounterId.name:
                        encounterId = new EncounterId(Field.getFieldString(entry.getValue()));
                        break;
                    case Medications.name:
                        medications = new Medications(Field.getFieldObject(entry.getValue()));
                        break;
                    case Orders.name:
                        orders = new Orders(Field.getFieldObject(entry.getValue()));
                        break;
                    default:
                        throw new InvalidRequestException("Unexpected field in context: " + entry.getKey());
                }
            }
            super.setValue(getFieldList());
        }
        else {
            super.setValue(value);
        }
    }

    @Override
    public void validate() {
        for (Field field : getFieldList()) {
            field.validate();
        }
        super.validate();
    }

    public class PatientId extends Field<String> {

        public static final String name = "patientId";

        private PatientId(String value) {
            super.setValue(value);
            setOptional(false);
        }
    }

    public class EncounterId extends Field<String> {

        public static final String name = "encounterId";

        private EncounterId(String value) {
            super.setValue(value);
            setOptional(true);
        }
    }

    public class Medications extends Field<Object> {

        public static final String name = "medications";

        private Medications(Object value) {
            setValue(value);
            setOptional(true);
        }

        @Override
        public void setValue(Object value) {
            super.setValue(
                    value instanceof JsonObject
                            ? new Resources((JsonObject) value, fhirContext)
                            : value
            );
        }

        @Override
        public void validate() {
            if (getValue() != null) {
                ((Resources) getValue()).validate();
            }
            super.validate();
        }
    }

    public class Orders extends Field<Object> {

        public static final String name = "orders";

        private Orders(Object value) {
            setValue(value);
            setOptional(true);
        }

        @Override
        public void setValue(Object value) {
            super.setValue(
                    value instanceof JsonObject
                            ? new Resources((JsonObject) value, fhirContext)
                            : value
            );
        }

        @Override
        public void validate() {
            if (getValue() != null) {
                ((Resources) getValue()).validate();
            }
            super.validate();
        }
    }
}
