package org.opencds.cqf.exceptions;

public class InvalidUserException extends RuntimeException {
    private String message;

    public InvalidUserException(String found) {
        message = "Expected user reference in the form Practitioner/{id}, Patient/{id}, or RelatedPerson/{id}, Found: " + found;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
