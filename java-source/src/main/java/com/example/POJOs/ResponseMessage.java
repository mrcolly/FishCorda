package com.example.POJOs;

public class ResponseMessage {

        private String outcome;
        private String message;

    public ResponseMessage(String outcome, String message) {
        this.outcome = outcome;
        this.message = message;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getMessage() {
        return message;
    }
}
