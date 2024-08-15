package com.coride.controller.general;

import com.coride.constant.JwtClaimsConstant;
import com.coride.constant.MessageConstant;
import com.coride.context.BaseContext;
import com.coride.dto.AccountManagementDTO;
import com.coride.dto.UserLoginDTO;
import com.coride.dto.UserRegisterDTO;
import com.coride.entity.User;
import com.coride.properties.JwtProperties;
import com.coride.result.Result;
import com.coride.service.general.EmailService;
import com.coride.service.general.UserService;
import com.coride.utils.JwtUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private EmailService emailService;

    @PostMapping ("/email")
    public Result getVerificationCode(@RequestParam String email){
        emailService.sendVerificationCode(email);
        log.info("Email code sent to "+email);
        return Result.success("Verification code has been sent!");
    }

    @PostMapping("/register")
    @ApiOperation("User register account")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO){
        userService.register(userRegisterDTO);
        return Result.success(MessageConstant.REGISTERED_SUCCESSFULLY);
    }

    @PostMapping("/login")
    @ApiOperation("User login")
    public Result<String> login(@RequestBody UserLoginDTO userLoginDTO){
        User user = userService.login(userLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getIdUser());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return Result.success(token,"You have successfully login!");
    }

    @GetMapping("/account/view")
    public Result<AccountManagementDTO> viewAccount(){
        Long id = BaseContext.getCurrentId();
        AccountManagementDTO accountManagementDTO = userService.getAccount(id);
        return Result.success(accountManagementDTO);
    }

    @PostMapping("/account/update")
    public Result updateAccount(@RequestBody AccountManagementDTO accountManagementDTO){
        Long id = BaseContext.getCurrentId();
        userService.updateAccount(id, accountManagementDTO);
        return Result.success();
    }
}
