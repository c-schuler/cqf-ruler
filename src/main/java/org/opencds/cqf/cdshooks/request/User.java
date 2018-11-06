package org.opencds.cqf.cdshooks.request;

import org.opencds.cqf.exceptions.InvalidUserException;

public class User extends Field<String> {

    public static final String name = "user";

    public User(String value) {
        setOptional(false);
        super.setValue(value);
    }

    @Override
    public void validate() {
        super.validate();
        if (!getValue().startsWith("Practitioner/")
                && !getValue().startsWith("Patient/")
                && !getValue().startsWith("RelatedPerson/"))
        {
            throw new InvalidUserException(getValue());
        }
    }
}
