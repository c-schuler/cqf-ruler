package org.opencds.cqf.qdm.providers;

import ca.uhn.fhir.jpa.provider.dstu3.JpaResourceProviderDstu3;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.runtime.DateTime;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.cql.runtime.Quantity;
import org.opencds.cqf.exceptions.NotImplementedException;
import org.opencds.cqf.qdm.QdmBaseType;
import org.opencds.cqf.qdm.resources.PositiveEncounterPerformed;
import org.opencds.cqf.qdm.types.FacilityLocation;

import java.util.ArrayList;
import java.util.List;

public class QdmAdapter {

    private QdmDataProvider dataProvider;

    public QdmAdapter(QdmDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String mapToFhirType(String dataType) {
        switch (dataType) {
            case "NegativeDiagnosis": return "Condition";
            case "PositiveDiagnosis": return "Condition";
            case "NegativeEncounterPerformed": return "Encounter";
            case "PositiveEncounterPerformed": return "Encounter";
            default: throw new NotImplementedException("Mapping not implemented for QDM type: " + dataType);
        }
    }

    public QdmBaseType createQdmResource(String qdmResourceType, Object fhirResource) {
        switch(qdmResourceType) {
            case "PositiveEncounterPerformed":
                return createPositiveEncounterPerformed(fhirResource);
            default: throw new NotImplementedException("Resource creation is not implemented for QDM type: " + qdmResourceType);
        }
    }

    public Code createCode(Coding fhirCode) {
        if (fhirCode == null) {
            return null;
        }
        return new Code()
                .withSystem(fhirCode.getSystem())
                .withCode(fhirCode.getCode())
                .withDisplay(fhirCode.getDisplay())
                .withVersion(fhirCode.getVersion());
    }

    public Interval createInterval(Period period) {
        if (period == null) {
            return null;
        }
        if (!period.hasStart() && !period.hasEnd()) {
            return null;
        }
        return new Interval(
                period.hasStart() ? DateTime.fromJavaDate(period.getStart()) : null, true,
                period.hasEnd() ? DateTime.fromJavaDate(period.getEnd()) : null, true
        );
    }

    public PositiveEncounterPerformed createPositiveEncounterPerformed(Object fhirResource) {
        if (!(fhirResource instanceof Encounter)) {
            throw new IllegalArgumentException("Expecting FHIR Encounter resource, found " + fhirResource.getClass().getSimpleName());
        }

        Encounter encounter = (Encounter) fhirResource;

        PositiveEncounterPerformed positiveEncounterPerformed = new PositiveEncounterPerformed();

        // TODO - populate once Provenance support is added
        positiveEncounterPerformed.setAuthorDatetime(null);

        if (encounter.hasHospitalization()
                && encounter.getHospitalization().hasAdmitSource()
                && encounter.getHospitalization().getAdmitSource().hasCoding())
        {
            positiveEncounterPerformed.setAdmissionSource(createCode(encounter.getHospitalization().getAdmitSource().getCodingFirstRep()));
        }

        if (encounter.hasPeriod()) {
            positiveEncounterPerformed.setRelevantPeriod(createInterval(encounter.getPeriod()));
        }

        if (encounter.hasHospitalization()
                && encounter.getHospitalization().hasDischargeDisposition()
                && encounter.getHospitalization().getDischargeDisposition().hasCoding())
        {
            positiveEncounterPerformed.setDischargeDisposition(createCode(encounter.getHospitalization().getDischargeDisposition().getCodingFirstRep()));
        }

        if (encounter.hasDiagnosis() && !encounter.getDiagnosis().isEmpty()) {
            List<Code> diagnosisCodes = new ArrayList<>();
            for (Encounter.DiagnosisComponent component : encounter.getDiagnosis()) {
                boolean principleDaignosis = component.hasRank() && component.getRank() == 1;
                JpaResourceProviderDstu3<? extends IAnyResource> resourceProvider = dataProvider.resolveResourceProvider(component.getCondition().getReference().split("/")[0]);
                IAnyResource resource = resourceProvider.getDao().read(new IdType(component.getCondition().getReference()));
                if (resource instanceof Condition) {
                    Condition condition = (Condition) resource;
                    if (condition.hasCode()) {
                        for (Coding coding : condition.getCode().getCoding()) {
                            diagnosisCodes.add(createCode(coding));
                            if (principleDaignosis) {
                                positiveEncounterPerformed.setPrincipalDiagnosis(createCode(coding));
                            }
                        }
                    }
                }
                else {
                    Procedure procedure = (Procedure) resource;
                    if (procedure.hasCode()) {
                        for (Coding coding : procedure.getCode().getCoding()) {
                            diagnosisCodes.add(createCode(coding));
                            if (principleDaignosis) {
                                positiveEncounterPerformed.setPrincipalDiagnosis(createCode(coding));
                            }
                        }
                    }
                }
            }
            positiveEncounterPerformed.setDiagnosis(diagnosisCodes);
        }

        if (encounter.hasLocation()) {
            List<FacilityLocation> facilityLocations = new ArrayList<>();
            for (Encounter.EncounterLocationComponent location : encounter.getLocation()) {
                FacilityLocation facilityLocation = new FacilityLocation();
                facilityLocation.setLocationPeriod(createInterval(location.getPeriod()));
                JpaResourceProviderDstu3<? extends IAnyResource> resourceProvider = dataProvider.resolveResourceProvider("Location");
                IAnyResource resource = resourceProvider.getDao().read(new IdType(location.getLocation().getReference()));
                Location fhirLocation = (Location) resource;
                if (fhirLocation.hasType() && fhirLocation.getType().hasCoding()) {
                    facilityLocation.setCode(createCode(fhirLocation.getType().getCodingFirstRep()));
                }
                facilityLocations.add(facilityLocation);
            }
            positiveEncounterPerformed.setFacilityLocations(facilityLocations);
        }

        if (positiveEncounterPerformed.getPrincipalDiagnosis() == null
                && positiveEncounterPerformed.getDiagnosis() != null
                && !positiveEncounterPerformed.getDiagnosis().isEmpty())
        {
            positiveEncounterPerformed.setPrincipalDiagnosis(positiveEncounterPerformed.getDiagnosis().get(0));
        }

        if (encounter.hasExtension()) {
            for (Extension extension : encounter.getExtension()) {
                if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/encounter-reasonCancelled")) {
                    if (extension.hasValue()) {
                        positiveEncounterPerformed.setNegationRationale(createCode(((CodeableConcept) extension.getValue()).getCodingFirstRep()));
                    }
                    break;
                }
            }
        }

        if (encounter.hasLength()) {
            Duration duration = encounter.getLength();
            positiveEncounterPerformed.setLengthOfStay(new Quantity().withValue(duration.getValue()).withUnit(duration.getUnit()));
        }

        return positiveEncounterPerformed;
    }
}
