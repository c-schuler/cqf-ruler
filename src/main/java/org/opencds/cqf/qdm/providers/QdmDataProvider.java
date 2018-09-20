package org.opencds.cqf.qdm.providers;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.providers.JpaDataProvider;
import org.opencds.cqf.qdm.QdmBaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QdmDataProvider extends JpaDataProvider {

    private QdmAdapter adapter;

    public QdmDataProvider(Collection<IResourceProvider> providers) {
        super(providers);
        setPackageName("org.opencds.cqf.qdm.resources");
        adapter = new QdmAdapter(this);
    }

    @Override
    public Iterable<Object> retrieve(String context, Object contextValue, String dataType, String templateId,
                                     String codePath, Iterable<Code> codes, String valueSet, String datePath,
                                     String dateLowPath, String dateHighPath, Interval dateRange)
    {
        Iterable<Object> fhirResources = super.retrieve(context, contextValue, adapter.mapToFhirType(dataType), templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange);
        List<Object> qdmResources = new ArrayList<>();

        for (Object fhirResource : fhirResources) {
            qdmResources.add(adapter.createQdmResource(dataType, fhirResource));
        }

        return qdmResources;
    }


}
