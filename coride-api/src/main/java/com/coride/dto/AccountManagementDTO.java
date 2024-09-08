package com.coride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountManagementDTO implements Serializable {
    private String name;
    private String organizationName;
    private String accountNo;
    private String password;
}
