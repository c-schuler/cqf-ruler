package org.opencds.cqf.qdm.providers;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.opencds.cqf.cql.runtime.*;
import org.opencds.cqf.providers.JpaDataProvider;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class QdmDataProvider extends JpaDataProvider {

    private QdmAdapter adapter;

    public QdmDataProvider(Collection<IResourceProvider> providers) {
        super(providers);
        setPackageName("org.opencds.cqf.qdm.model");
        adapter = new QdmAdapter(this);
    }

    @Override
    public Iterable<Object> retrieve(String context, Object contextValue, String dataType, String templateId,
                                     String codePath, Iterable<Code> codes, String valueSet, String datePath,
                                     String dateLowPath, String dateHighPath, Interval dateRange)
    {
        if (codePath != null) {
            codePath = QdmAdapter.mapCodePath(dataType, codePath);
        }
        Iterable<Object> fhirResources = super.retrieve(context, contextValue, adapter.mapToFhirType(dataType), templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange);
        List<Object> qdmResources = new ArrayList<>();

        for (Object fhirResource : fhirResources) {
            qdmResources.add(adapter.createQdmResource(dataType, fhirResource));
        }

        return qdmResources;
    }

    // NOTE: this is used to represent a Null Interval - only used for the intervals that are resolved using extensions (opioid test data)
    public Interval nullInterval = new Interval(
            new DateTime("0001-01-01T00:00:00.000", ZoneOffset.UTC), true,
            new DateTime("0001-01-01T00:00:00.000", ZoneOffset.UTC), true
    );

    @Override
    protected Object toJavaPrimitive(Object result, Object source) {
        if (result instanceof Period) {
            if (QdmAdapter.isPeriodNull((Period) result)) {
                return nullInterval;
            }
            return new Interval(
                    ((Period) result).hasStart() ? DateTime.fromJavaDate(((Period) result).getStart()) : null, true,
                    ((Period) result).hasEnd() ? DateTime.fromJavaDate(((Period) result).getEnd()) : null, true
            );
        }

        if (result instanceof Range) {
            Range range = (Range) result;
            return new Interval(
                    new org.opencds.cqf.cql.runtime.Quantity().withValue(range.getLow().getValue()).withUnit(range.getLow().getUnit()), true,
                    new org.opencds.cqf.cql.runtime.Quantity().withValue(range.getHigh().getValue()).withUnit(range.getHigh().getUnit()), true
            );
        }

        if (result instanceof Quantity) {
            return new org.opencds.cqf.cql.runtime.Quantity()
                    .withValue(((Quantity) result).getValue())
                    .withUnit(((Quantity) result).getUnit());
        }

        if (result instanceof DateTimeType) {
            return DateTime.fromJavaDate(((DateTimeType) result).getValue());
        }

        if (result instanceof IPrimitiveType) {
            return ((IPrimitiveType) result).getValue();
        }

        return super.toJavaPrimitive(result, source);
    }

}
