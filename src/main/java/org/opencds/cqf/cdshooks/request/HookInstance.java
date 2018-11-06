package org.opencds.cqf.cdshooks.request;

public class HookInstance extends Field<String> {

    public static final String name = "hookInstance";

    public HookInstance(String value) {
        setOptional(false);
        super.setValue(value);
    }
}
