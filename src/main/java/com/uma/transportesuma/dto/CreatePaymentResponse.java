package com.uma.transportesuma.dto;

public class CreatePaymentResponse {

    private String clientSecret;
    private String id;

    public CreatePaymentResponse(String clientSecret, String id) {
        this.clientSecret = clientSecret;
        this.id = id;
    }

}
