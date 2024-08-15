package com.coride.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * All three types of users can use this DTO
 */


@Data
@ApiModel(description = "DTO for user login")
public class UserLoginDTO implements Serializable {
    private String accountNo;

    private String password;

    private String accountType;
    
}
