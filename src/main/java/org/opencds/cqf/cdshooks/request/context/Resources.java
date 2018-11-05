package org.opencds.cqf.cdshooks.request.context;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.NutritionOrder;
import ca.uhn.fhir.model.dstu2.resource.ProcedureRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.opencds.cqf.cdshooks.request.Field;
import org.opencds.cqf.exceptions.InvalidRequestException;

import java.util.ArrayList;
import java.util.List;

public class Resources extends Field<List<Object>> {

    public Resources(JsonObject value, FhirContext fhirContext) {
        resolveBundle(
                fhirContext.newJsonParser().parseResource(
                        new GsonBuilder().create().toJson(value)
                )
        );
    }

    private void resolveBundle(Object contextResources) {
        List<Object> resources = new ArrayList<>();
        if (contextResources instanceof Bundle) {
            if (((Bundle) contextResources).getEntry().isEmpty()) {
                throw new InvalidRequestException("Context resource bundle is empty");
            }
            for (Bundle.Entry entry : ((Bundle) contextResources).getEntry()) {
                if (entry.getResource() == null) {
                    throw new InvalidRequestException("Context bundle entry missing resource");
                }
                resources.add(entry.getResource());
            }
        }
        else if (contextResources instanceof org.hl7.fhir.dstu3.model.Bundle) {
            if (!((org.hl7.fhir.dstu3.model.Bundle) contextResources).hasEntry()) {
                throw new InvalidRequestException("Context resource bundle is empty");
            }
            for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : ((org.hl7.fhir.dstu3.model.Bundle) contextResources).getEntry()) {
                if (!entry.hasResource()) {
                    throw new InvalidRequestException("Context bundle entry missing resource");
                }
                resources.add(entry.getResource());
            }
        }
        setValue(resources);
    }

    @Override
    public void validate() {
        for (Object resource : getValue()) {
            if (resource instanceof MedicationOrder) {
                if (!((MedicationOrder) resource).getStatus().equals("draft")) {
                    throw new InvalidRequestException("Context bundle MedicationOrder is required to have draft status");
                }
            }
            else if (resource instanceof DiagnosticOrder) {
                if (!((DiagnosticOrder) resource).getStatus().equals("draft")) {
                    throw new InvalidRequestException("Context bundle DiagnosticOrder is required to have draft status");
                }
            }
            else if (resource instanceof ProcedureRequest) {
                if (!((ProcedureRequest) resource).getStatus().equals("draft")) {
                    throw new InvalidRequestException("Context bundle ProcedureRequest is required to have draft status");
                }
            }
            else if (resource instanceof NutritionOrder) {
                if (!((NutritionOrder) resource).getStatus().equals("draft")) {
                    throw new InvalidRequestException("Context bundle NutritionOrder is required to have draft status");
                }
            }
            else if (resource instanceof MedicationRequest) {
                if (((MedicationRequest) resource).getStatus() != MedicationRequest.MedicationRequestStatus.DRAFT) {
                    throw new InvalidRequestException("Context bundle MedicationRequest is required to have draft status");
                }
            }
            else if (resource instanceof ReferralRequest) {
                if (((ReferralRequest) resource).getStatus() != ReferralRequest.ReferralRequestStatus.DRAFT) {
                    throw new InvalidRequestException("Context bundle DiagnosticOrder is required to have draft status");
                }
            }
            else if (resource instanceof org.hl7.fhir.dstu3.model.ProcedureRequest) {
                if (((org.hl7.fhir.dstu3.model.ProcedureRequest) resource).getStatus() != org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus.DRAFT) {
                    throw new InvalidRequestException("Context bundle ReferralRequest is required to have draft status");
                }
            }
            else if (resource instanceof org.hl7.fhir.dstu3.model.NutritionOrder) {
                if (((org.hl7.fhir.dstu3.model.NutritionOrder) resource).getStatus() != org.hl7.fhir.dstu3.model.NutritionOrder.NutritionOrderStatus.DRAFT) {
                    throw new InvalidRequestException("Context bundle NutritionOrder is required to have draft status");
                }
            }
            else if (resource instanceof org.hl7.fhir.dstu3.model.VisionPrescription) {
                if (((org.hl7.fhir.dstu3.model.VisionPrescription) resource).getStatus() != org.hl7.fhir.dstu3.model.VisionPrescription.VisionStatus.DRAFT) {
                    throw new InvalidRequestException("Context bundle VisionPrescription is required to have draft status");
                }
            }
            else {
                throw new InvalidRequestException("Found unexpected resource: " + resource.getClass().getName());
            }
        }
    }
}
