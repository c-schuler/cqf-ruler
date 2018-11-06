package org.opencds.cqf.cdshooks.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.opencds.cqf.exceptions.InvalidFieldTypeException;
import org.opencds.cqf.exceptions.InvalidRequestException;

public class Field<T> {
    private T value;
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
        validate();
    }

    private boolean optional;
    public boolean isOptional() {
        return optional;
    }
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public static String getFieldString(JsonElement field) {
        if (field == null || field.isJsonNull()) {
            return null;
        }
        try {
            return field.getAsString();
        } catch (IllegalStateException | UnsupportedOperationException e) {
            throw new InvalidFieldTypeException("String", field);
        }
    }

    public static Integer getFieldInt(JsonElement field) {
        if (field == null || field.isJsonNull()) {
            return null;
        }
        try {
            return field.getAsInt();
        } catch (IllegalStateException | UnsupportedOperationException e) {
            throw new InvalidFieldTypeException("Int", field);
        }
    }

    public static JsonObject getFieldObject(JsonElement field) {
        if (field == null || field.isJsonNull()) {
            return null;
        }
        try {
            return field.getAsJsonObject();
        } catch (IllegalStateException | UnsupportedOperationException e) {
            throw new InvalidFieldTypeException("Object", field);
        }
    }

    public void validate() {
        if (!isOptional() && value == null) {
            throw new InvalidRequestException(
                    "Request is missing the following field: "
                            + this.getClass().getSimpleName().substring(0, 1).toLowerCase()
                            + this.getClass().getSimpleName().substring(1)
            );
        }
    }
}
