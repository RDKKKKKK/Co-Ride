package com.coride.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    private Long idUser;

    private String accountNo;

    private String password;

    private String name;

    private Integer idOrganization;

    private String phone;
}
