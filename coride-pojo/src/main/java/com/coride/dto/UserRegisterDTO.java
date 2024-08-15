package com.coride.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private String accountNo;

    private String password;

    private String name;

    private String phone;

    private Integer idOrganization;

    private String accountType;

    private String verificationCode;
}
