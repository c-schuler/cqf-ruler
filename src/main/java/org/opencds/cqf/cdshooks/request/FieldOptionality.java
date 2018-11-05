package org.opencds.cqf.cdshooks.request;

public enum FieldOptionality {
    REQUIRED,
    OPTIONAL;

    @Override
    public String toString() {
        return this == REQUIRED ? "REQUIRED" : "OPTIONAL";
    }
}
