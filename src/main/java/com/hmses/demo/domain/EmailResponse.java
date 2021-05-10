package com.hmses.demo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class EmailResponse implements Serializable {

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("response")
    private String response;

    public String getResponse() {
        return response;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
