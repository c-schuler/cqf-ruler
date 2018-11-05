package org.opencds.cqf.cdshooks.request;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.JsonObject;
import org.opencds.cqf.cdshooks.request.context.Context;
import org.opencds.cqf.cdshooks.request.prefetch.Prefetch;
import org.opencds.cqf.providers.JpaDataProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private FhirVersionEnum version;
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
                default: throw new RuntimeException("Unknown hook: " + code);
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

    public Request(JsonObject request, JpaDataProvider provider, FhirVersionEnum version) {
        this.version = version;
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
            throw new RuntimeException(e.getMessage());
        }
        fhirAuth = new FhirAuth(Field.getFieldObject(request.get(FhirAuth.name)));
        if (fhirAuth.getValue() != null) {
            fhirServer.setRemoteProviderAuth(fhirAuth);
        }
        user = new User(Field.getFieldString(request.get(User.name)));
        context = new Context(Field.getFieldObject(request.get(Context.name)), fhirContext);
        switch (type) {
            case MEDICATION_PRESCRIBE:
                context.getMedications().setOptionality(FieldOptionality.REQUIRED);
                break;
            case ORDER_REVIEW:
                context.getOrders().setOptionality(FieldOptionality.REQUIRED);
        }
        prefetch = new Prefetch(Field.getFieldObject(request.get(Prefetch.name)), fhirContext);
    }

    private List<Field> getFieldList() {
        List<Field> fields = new ArrayList<>();
        fields.add(hook);
        fields.add(hookInstance);
        fields.add(fhirServer);
        fields.add(fhirAuth);
        fields.add(user);
        fields.add(context);
        fields.add(prefetch);
        return fields;
    }

    public void validate() {
        for (Field field : getFieldList()) {
            field.validate();
        }
    }
}
