package org.opencds.cqf.cdshooks.request.prefetch;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.cdshooks.request.Field;

public class Prefetch extends Field<Object> {

    public static final String name = "prefetch";

    private FhirContext fhirContext;

    public Prefetch(Object value, FhirContext fhirContext) {
        setOptional(true);
        this.fhirContext = fhirContext;
        super.setValue(value);
    }
}
