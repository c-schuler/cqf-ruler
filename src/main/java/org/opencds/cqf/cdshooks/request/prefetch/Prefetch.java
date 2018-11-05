package org.opencds.cqf.cdshooks.request.prefetch;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.opencds.cqf.cdshooks.request.Field;
import org.opencds.cqf.cdshooks.request.FieldOptionality;

public class Prefetch extends Field<Object> {

    public static final String name = "prefetch";

    private FhirContext fhirContext;

    public Prefetch(Object value, FhirContext fhirContext) {
        this.fhirContext = fhirContext;
        super.setValue(value);
        setOptionality(FieldOptionality.OPTIONAL);
    }
}
