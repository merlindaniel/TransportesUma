package com.uma.transportesuma.exception;

import com.uma.transportesuma.document.User;

public class UserError extends User {

    public final static String EMAIL_ERROR = "El email ya existe";
    public final static String USERNAME_ERROR = "El nombre de usuario ya existe";
    private String fieldError;
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        if(error.equals(EMAIL_ERROR))
            this.setFieldError("EMAIL");
        else if(error.equals(USERNAME_ERROR))
            this.setFieldError("USERNAME");
        this.error = error;
    }

    public String getFieldError() {
        return fieldError;
    }

    public void setFieldError(String fieldError) {
        this.fieldError = fieldError;
    }
}
