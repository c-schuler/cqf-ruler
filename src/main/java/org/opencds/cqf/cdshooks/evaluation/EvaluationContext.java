package org.opencds.cqf.cdshooks.evaluation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.rp.dstu3.LibraryResourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.cdshooks.hooks.Hook;
import org.opencds.cqf.config.STU3LibraryLoader;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderDstu2;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.LibraryLoader;
import org.opencds.cqf.exceptions.NotImplementedException;
import org.opencds.cqf.providers.FHIRBundleResourceProvider;
import org.opencds.cqf.providers.JpaDataProvider;

import java.io.IOException;
import java.util.List;

public class EvaluationContext {

    // Provided
    private Hook hook;
    private FhirVersionEnum fhirVersion;
    private FhirContext fhirContext;
    private JpaDataProvider systemProvider;

    // Requires resolution
    private BaseFhirDataProvider remoteProvider;
    private PlanDefinition planDefinition;
    private Library library;
    private LibraryLoader libraryLoader;
    private Context context;
    private List<Object> contextResources;
    private List<Object> prefetchResources;


    public EvaluationContext(Hook hook, FhirVersionEnum fhirVersion, JpaDataProvider systemProvider) {
        this.hook = hook;
        this.fhirVersion = fhirVersion;
        this.fhirContext = new FhirContext(fhirVersion);
        this.systemProvider = systemProvider;
    }

    public Hook getHook() {
        return hook;
    }

    public FhirVersionEnum getFhirVersion() {
        return fhirVersion;
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    public JpaDataProvider getSystemProvider() {
        return systemProvider;
    }

    public BaseFhirDataProvider getRemoteProvider() {
        if (remoteProvider == null) {
            if (hook.getRequest().getFhirServerUrl() != null) {
                switch (fhirVersion) {
                    case DSTU2:
                        remoteProvider = new FhirDataProviderDstu2().setEndpoint(hook.getRequest().getFhirServerUrl());
                        break;
                    case DSTU3:
                        remoteProvider = new FhirDataProviderStu3().setEndpoint(hook.getRequest().getFhirServerUrl());
                        break;
                    default:
                        throw new NotImplementedException("This CDS Hooks implementation is not configured for FHIR version: " + fhirVersion.getFhirVersionString());
                }
                remoteProvider.setTerminologyProvider(systemProvider.getTerminologyProvider());
            }
        }
        return remoteProvider;
    }

    public PlanDefinition getPlanDefinition() {
        if (planDefinition == null) {
            planDefinition = (PlanDefinition) systemProvider.resolveResourceProvider("PlanDefinition").getDao().read(new IdType(hook.getRequest().getServiceName()));
            if (planDefinition == null) {
                throw new RuntimeException("PlanDefinition could not be found: PlanDefinition/" + hook.getRequest().getServiceName());
            }
        }
        return planDefinition;
    }

    public LibraryLoader getLibraryLoader() {
        if (libraryLoader == null) {
            libraryLoader =
                    new STU3LibraryLoader(
                            (LibraryResourceProvider) systemProvider.resolveResourceProvider("Library"),
                            new LibraryManager(new ModelManager()),
                            new ModelManager()
                    );
        }
        return libraryLoader;
    }

    public Library getLibrary() {
        if (library == null) {
            if (getPlanDefinition().hasLibrary()) {
                // TODO - account for multiple libraries
                IIdType libraryId = getPlanDefinition().getLibraryFirstRep().getReferenceElement();
                if (libraryId.hasVersionIdPart()) {
                    library = getLibraryLoader().load(new VersionedIdentifier().withId(libraryId.getIdPart()).withVersion(libraryId.getVersionIdPart()));
                }
                else {
                    library = getLibraryLoader().load(new VersionedIdentifier().withId(libraryId.getIdPart()));
                }
            }
            else {
                throw new RuntimeException("Missing library reference for PlanDefinition/" + hook.getRequest().getServiceName());
            }
        }
        return library;
    }

    public Context getContext() {
        if (context == null) {
            context = new Context(getLibrary());
            context.registerDataProvider("http://hl7.org/fhir", getRemoteProvider() == null ? systemProvider : getRemoteProvider());
            context.registerLibraryLoader(getLibraryLoader());
            context.setExpressionCaching(true);
        }
        return context;
    }

    public List<Object> getContextResources() {
        if (contextResources == null) {
            contextResources = EvaluationHelper.resolveContextResources(hook.getContextResources(), fhirContext);
            if (hook.getRequest().isApplyCql()) {
                applyCqlToResources(contextResources);
            }
        }
        return contextResources;
    }

    public List<Object> getPrefetchResources() throws IOException {
        if (prefetchResources == null) {
            prefetchResources =
                    EvaluationHelper.resolvePrefetchResources(
                            hook,
                            fhirContext,
                            getRemoteProvider() == null ? systemProvider.getFhirClient() : getRemoteProvider().getFhirClient()
                    );
            if (hook.getRequest().isApplyCql()) {
                applyCqlToResources(prefetchResources);
            }
        }
        return prefetchResources;
    }

    private void applyCqlToResources(List<Object> resources) {
        FHIRBundleResourceProvider bundleResourceProvider = new FHIRBundleResourceProvider(systemProvider);
        for (Object res : resources) {
            try {
                bundleResourceProvider.applyCql((Resource) res);
            } catch (FHIRException e) {
                e.printStackTrace();
                throw new RuntimeException("Error applying cql expression extensions: " + e.getMessage());
            }
        }
    }
}
