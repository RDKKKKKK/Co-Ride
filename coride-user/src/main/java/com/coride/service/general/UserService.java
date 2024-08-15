package com.coride.service.general;

import com.coride.dto.AccountManagementDTO;
import com.coride.dto.UserLoginDTO;
import com.coride.dto.UserRegisterDTO;
import com.coride.entity.User;

public interface UserService {
    void register(UserRegisterDTO userRegisterDTO);

    User login(UserLoginDTO userLoginDTO);

    AccountManagementDTO getAccount(Long id);

    void updateAccount(Long id, AccountManagementDTO accountManagementDTO);
}
