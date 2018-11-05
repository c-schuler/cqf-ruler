package org.opencds.cqf.cdshooks.request;

public class User extends Field<String> {

    public static final String name = "user";

    public User(String value) {
        super.setValue(value);
        setOptionality(FieldOptionality.REQUIRED);
    }
}
