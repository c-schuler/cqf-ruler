package org.opencds.cqf.cdshooks.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.opencds.cqf.exceptions.InvalidRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirAuth extends Field<Object> {

    public static final String name = "fhirAuthorization";

    private AccessToken accessToken;
    public AccessToken getAccessToken() {
        return accessToken;
    }

    private TokenType tokenType;
    public TokenType getTokenType() {
        return tokenType;
    }

    private ExpiresIn expiresIn;
    public ExpiresIn getExpiresIn() {
        return expiresIn;
    }

    private Scope scope;
    public Scope getScope() {
        return scope;
    }

    private Subject subject;
    public Subject getSubject() {
        return subject;
    }

    public FhirAuth(Object value) {
        setValue(value);
        setOptionality(FieldOptionality.OPTIONAL);
    }

    private List<Field> getFieldList() {
        List<Field> fields = new ArrayList<>();
        fields.add(accessToken == null ? new AccessToken(null) : accessToken);
        fields.add(tokenType == null ? new TokenType(null) : tokenType);
        fields.add(expiresIn == null ? new ExpiresIn(null) : expiresIn);
        fields.add(scope == null ? new Scope(null) : scope);
        fields.add(subject == null ? new Subject(null) : subject);
        return fields;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof JsonObject) {
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) value).entrySet()) {
                switch (entry.getKey()) {
                    case AccessToken.name:
                        accessToken = new AccessToken(Field.getFieldString(entry.getValue()));
                        break;
                    case TokenType.name:
                        tokenType = new TokenType(Field.getFieldString(entry.getValue()));
                        break;
                    case ExpiresIn.name:
                        expiresIn = new ExpiresIn(Field.getFieldInt(entry.getValue()));
                        break;
                    case Scope.name:
                        scope = new Scope(Field.getFieldString(entry.getValue()));
                        break;
                    case Subject.name:
                        subject = new Subject(Field.getFieldString(entry.getValue()));
                        break;
                    default:
                        throw new InvalidRequestException("Unexpected field in fhirAuthorization: " + entry.getKey());
                }
            }
            super.setValue(getFieldList());
        }
        else {
            super.setValue(value);
        }
    }

    @Override
    public void validate() {
        for (Field field : getFieldList()) {
            field.validate();
        }
        super.validate();
    }

    public class AccessToken extends Field<String> {

        public static final String name = "access_token";

        private AccessToken(String value) {
            super.setValue(value);
            setOptionality(FieldOptionality.REQUIRED);
        }
    }

    public class TokenType extends Field<String> {

        public static final String name = "token_type";

        private TokenType(String value) {
            super.setValue(value);
            setOptionality(FieldOptionality.REQUIRED);
        }
    }

    public class ExpiresIn extends Field<Integer> {

        public static final String name = "expires_in";

        private ExpiresIn(Integer value) {
            super.setValue(value);
            setOptionality(FieldOptionality.REQUIRED);
        }
    }

    public class Scope extends Field<String> {

        public static final String name = "scope";

        private Scope(String value) {
            super.setValue(value);
            setOptionality(FieldOptionality.REQUIRED);
        }
    }

    public class Subject extends Field<String> {

        public static final String name = "subject";

        private Subject(String value) {
            super.setValue(value);
            setOptionality(FieldOptionality.REQUIRED);
        }
    }
}