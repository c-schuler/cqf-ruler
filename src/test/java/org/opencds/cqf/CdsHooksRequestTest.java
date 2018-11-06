package org.opencds.cqf;

import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cdshooks.request.Request;
import org.opencds.cqf.exceptions.*;

public class CdsHooksRequestTest {

    private JsonParser parser = new JsonParser();
    private String medicationsBundleNonDraft = "{\n" +
            "  \"resourceType\": \"Bundle\",\n" +
            "  \"entry\": [\n" +
            "    {" +
            "      \"resource\": {\n" +
            "        \"resourceType\": \"MedicationRequest\",\n" +
            "        \"id\": \"3123\",\n" +
            "        \"status\": \"active\",\n" +
            "        \"intent\": \"order\",\n" +
            "        \"medicationReference\": {\n" +
            "          \"reference\": \"Medication/example\"\n" +
            "        },\n" +
            "        \"subject\": {\n" +
            "          \"reference\": \"Patient/123\"\n" +
            "        }\n" +
            "      }" +
            "    }" +
            "  ]" +
            "}";

    private String medicationsBundleDraft = "{\n" +
            "  \"resourceType\": \"Bundle\",\n" +
            "  \"entry\": [\n" +
            "    {" +
            "      \"resource\": {\n" +
            "        \"resourceType\": \"MedicationRequest\",\n" +
            "        \"id\": \"3123\",\n" +
            "        \"status\": \"draft\",\n" +
            "        \"intent\": \"order\",\n" +
            "        \"medicationReference\": {\n" +
            "          \"reference\": \"Medication/example\"\n" +
            "        },\n" +
            "        \"subject\": {\n" +
            "          \"reference\": \"Patient/123\"\n" +
            "        }\n" +
            "      }" +
            "    }" +
            "  ]" +
            "}";

    @Test
    public void InvalidRequest() {

        String request;
        Request testRequest;

        // empty request
        try {
            request = "{}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // invalid hook
        try {
            request = "{\"hook\": \"invalid\"}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidHookException e) {
            // pass
        }

        // invalid hook type
        try {
            request = "{\"hook\": []}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // missing fields: hookInstance, user and context
        try {
            request = "{\"hook\": \"medication-prescribe\"}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // invalid hookInstance type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": {}}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // missing fields: user and context
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\"}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // invalid user type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": []}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // invalid user reference
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Prac/123\"}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidUserException e) {
            // pass
        }

        // missing fields: context
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\"}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // invalid context type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"context\": []}";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // empty context
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"context\": {} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // missing medications field in context
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\"} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidContextException e) {
            // pass
        }

        // context resource not draft status
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleNonDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // valid medication-prescribe request
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
        } catch (Exception e) {
            Assert.fail();
        }

        // invalid fhirServer type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"fhirServer\": {}, \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // invalid fhirServer URL
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"fhirServer\": \"invalidURL\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // valid medication-prescribe request
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"user\": \"Practitioner/123\", \"fhirServer\": \"http://measure.eval.kanvix.com/cqf-ruler/baseDstu3\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
        } catch (InvalidRequestException e) {
            Assert.fail();
        }

        // invalid fhirAuthorization type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"fhirAuthorization\": [], \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidFieldTypeException e) {
            // pass
        }

        // missing fhirServer when fhirAuthorization is present
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"fhirAuthorization\": {\"access_token\": \"token\", \"token_type\": \"Bearer\", \"expires_in\": 300, \"scope\": \"scope\", \"subject\": \"subject\"}, \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // invalid token_type
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"fhirAuthorization\": {\"access_token\": \"token\", \"token_type\": \"something\", \"expires_in\": 300, \"scope\": \"scope\", \"subject\": \"subject\"}, \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // missing fhirAuthorization fields
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"fhirAuthorization\": {}, \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
            Assert.fail();
        } catch (InvalidRequestException e) {
            // pass
        }

        // valid medication-prescribe request
        try {
            request = "{\"hook\": \"medication-prescribe\", \"hookInstance\": \"someID\", \"fhirServer\": \"http://measure.eval.kanvix.com/cqf-ruler/baseDstu3\", \"fhirAuthorization\": {\"access_token\": \"token\", \"token_type\": \"Bearer\", \"expires_in\": 300, \"scope\": \"scope\", \"subject\": \"subject\"}, \"user\": \"Practitioner/123\", \"context\": {\"patientId\": \"123\", \"medications\": " + medicationsBundleDraft + "} }";
            testRequest = new Request(parser.parse(request).getAsJsonObject(), null, FhirVersionEnum.DSTU3, null, null);
        } catch (InvalidRequestException e) {
            Assert.fail();
        }
    }
}
