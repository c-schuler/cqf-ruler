package org.opencds.cqf.cdshooks.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        return field.getAsString();
    }

    public static Integer getFieldInt(JsonElement field) {
        if (field == null || field.isJsonNull()) {
            return null;
        }
        return field.getAsInt();
    }

    public static JsonObject getFieldObject(JsonElement field) {
        if (field == null || field.isJsonNull()) {
            return null;
        }
        return field.getAsJsonObject();
    }

    public void validate() {
        if (isOptional() && value == null) {
            throw new InvalidRequestException(
                    "Request is missing the following field: "
                            + this.getClass().getSimpleName().substring(0, 1).toLowerCase()
                            + this.getClass().getSimpleName().substring(1)
            );
        }
    }
}
