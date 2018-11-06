package org.opencds.cqf.exceptions;

import com.google.gson.JsonElement;

public class InvalidFieldTypeException extends RuntimeException {

    private String message;

    public InvalidFieldTypeException(String message) {
        this.message = message;
    }

    public InvalidFieldTypeException(String expected, JsonElement found) {
        String type = "";
        if (found.isJsonPrimitive()) {
            if (found.getAsJsonPrimitive().isBoolean()) {
                type = "Boolean";
            }
            else if (found.getAsJsonPrimitive().isString()) {
                type = "String";
            }
            else if (found.getAsJsonPrimitive().isNumber()) {
                type = "Number";
            }
        }
        else if (found.isJsonArray()) {
            type = "Array";
        }
        else if (found.isJsonObject()) {
            type = "Object";
        }
        else {
            type = "unknown";
        }

        message = "Expected " + expected + ", Found: " + type;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
