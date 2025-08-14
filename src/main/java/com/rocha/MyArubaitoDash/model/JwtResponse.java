package com.rocha.MyArubaitoDash.model;

import java.io.Serializable;

public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;

    private final String token;

    private final boolean isNewUser;

    public JwtResponse(String token, boolean isNewUser) {
        this.token = token;
        this.isNewUser = isNewUser;
    }

    public String getToken() {
        return token;
    }


    public boolean getIsNewUser() {
        return isNewUser;
    }
}
