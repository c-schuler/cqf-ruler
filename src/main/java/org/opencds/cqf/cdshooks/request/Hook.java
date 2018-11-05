package org.opencds.cqf.cdshooks.request;

public class Hook extends Field<String> {

    public static final String name = "hook";

    public Hook(String value) {
        super.setValue(value);
        setOptionality(FieldOptionality.REQUIRED);
    }
}
