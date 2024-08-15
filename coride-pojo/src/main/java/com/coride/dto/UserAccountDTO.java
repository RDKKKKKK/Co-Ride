package com.coride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountDTO implements Serializable {
    private String accountNo;

    private String password;

    private String name;

    private Integer idOrganization;

    private String phone;
}
