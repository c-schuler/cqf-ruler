package org.opencds.cqf.cdshooks.request;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderDstu2;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.providers.JpaDataProvider;

import java.net.URL;

public class FhirServer extends Field<URL> {

    public static final String name = "fhirServer";

    private JpaDataProvider localProvider;
    public JpaDataProvider getLocalProvider() {
        return localProvider;
    }
    public void setLocalProvider(JpaDataProvider localProvider) {
        this.localProvider = localProvider;
    }

    private BaseFhirDataProvider remoteProvider;
    public BaseFhirDataProvider getRemoteProvider() {
        return remoteProvider;
    }
    public void setRemoteProvider(BaseFhirDataProvider remoteProvider) {
        this.remoteProvider = remoteProvider;
    }
    public void setRemoteProviderAuth(FhirAuth auth) {
        if (hasRemoteProvider()) {
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(auth.getAccessToken().getValue());
            remoteProvider.getFhirClient().registerInterceptor(authInterceptor);
        }
    }
    public boolean hasRemoteProvider() {
        return remoteProvider != null;
    }

    public FhirServer(URL value, JpaDataProvider localProvider, FhirVersionEnum version) {
        setOptional(true);
        super.setValue(value);
        this.localProvider = localProvider;
        if (value != null) {
            remoteProvider =
                    version == FhirVersionEnum.DSTU2
                            ? new FhirDataProviderDstu2().setEndpoint(value.toString())
                            : new FhirDataProviderStu3().setEndpoint(value.toString());
        }
    }
}
