package com.nutriscanner.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AskRequest {

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("question")
    private String question;

    public String getBarcode() {
        return barcode;
    }

    public String getQuestion() {
        return question;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}