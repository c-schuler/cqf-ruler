package org.opencds.cqf.cdshooks.request;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.JsonObject;
import org.opencds.cqf.cdshooks.request.context.Context;
import org.opencds.cqf.cdshooks.request.prefetch.Prefetch;
import org.opencds.cqf.exceptions.InvalidContextException;
import org.opencds.cqf.exceptions.InvalidHookException;
import org.opencds.cqf.exceptions.InvalidRequestException;
import org.opencds.cqf.providers.JpaDataProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private FhirContext fhirContext;

    enum Type {
        PATIENT_VIEW,
        MEDICATION_PRESCRIBE,
        ORDER_REVIEW;

        public static Type fromCode(String code) {
            switch (code) {
                case "patient-view": return PATIENT_VIEW;
                case "medication-prescribe": return MEDICATION_PRESCRIBE;
                case "order-review": return ORDER_REVIEW;
                default: throw new InvalidHookException("Unknown hook: " + code);
            }
        }
    }

    private Type type;
    public Type getType() {
        return type;
    }

    private Hook hook;
    public Hook getHook() {
        return hook;
    }

    private HookInstance hookInstance;
    public HookInstance getHookInstance() {
        return hookInstance;
    }

    private FhirServer fhirServer;
    public FhirServer getFhirServer() {
        return fhirServer;
    }

    private FhirAuth fhirAuth;
    public FhirAuth getFhirAuth() {
        return fhirAuth;
    }

    private User user;
    public User getUser() {
        return user;
    }

    private Context context;
    public Context getContext() {
        return context;
    }

    private Prefetch prefetch;
    public Prefetch getPrefetch() {
        return prefetch;
    }

    public Request(JsonObject request, JpaDataProvider provider, FhirVersionEnum version, String service, JsonObject serviceJson) {
        fhirContext = new FhirContext(version);
        hook = new Hook(Field.getFieldString(request.get(Hook.name)));
        type = Type.fromCode(hook.getValue());
        hookInstance = new HookInstance(Field.getFieldString(request.get(HookInstance.name)));
        try {
            String url = Field.getFieldString(request.get(FhirServer.name));
            if (url != null) {
                fhirServer = new FhirServer(new URL(url), provider, version);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new InvalidRequestException(e.getMessage());
        }
        fhirAuth = new FhirAuth(Field.getFieldObject(request.get(FhirAuth.name)));
        if (fhirAuth.getValue() != null) {
            if (fhirServer == null) {
                throw new InvalidRequestException("fhirServer field must be populated when fhirAuthorization is present");
            }
            fhirServer.setRemoteProviderAuth(fhirAuth);
        }
        user = new User(Field.getFieldString(request.get(User.name)));
        context = new Context(Field.getFieldObject(request.get(Context.name)), fhirContext);
        switch (type) {
            case MEDICATION_PRESCRIBE:
                if (context.getMedications() == null) {
                    throw new InvalidContextException("medication-prescribe hook missing medications field in context");
                }
                context.getMedications().setOptional(false);
                break;
            case ORDER_REVIEW:
                if (context.getOrders() == null) {
                    throw new InvalidContextException("order-review hook missing orders field in context");
                }
                context.getOrders().setOptional(false);
        }
        prefetch = new Prefetch(Field.getFieldObject(request.get(Prefetch.name)), fhirContext);
    }

    public void validate() {
        hook.validate();
        hookInstance.validate();
        fhirServer.validate();
        fhirAuth.validate();
        user.validate();
        context.validate();
        prefetch.validate();
    }
}
