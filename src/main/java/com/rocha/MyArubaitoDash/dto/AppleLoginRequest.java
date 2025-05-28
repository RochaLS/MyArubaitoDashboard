package com.rocha.MyArubaitoDash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppleLoginRequest {
    private String idToken;
    private String clientId;
}