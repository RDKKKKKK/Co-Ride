package com.coride.dto;

import lombok.Data;

@Data
public class ConfirmDTO {
    private String matchId;

    private String accountType;

    private String messageType;

    private boolean confirmed;

}
