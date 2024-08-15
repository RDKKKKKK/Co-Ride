package com.coride.service.general.impl;

import com.alibaba.fastjson.JSON;
import com.coride.constant.AccountTypeConstant;
import com.coride.constant.MessageConstant;
import com.coride.dto.AccountManagementDTO;
import com.coride.dto.UserLoginDTO;
import com.coride.dto.UserRegisterDTO;
import com.coride.entity.User;
import com.coride.exception.AccountNotFoundException;
import com.coride.exception.PasswordErrorException;
import com.coride.exception.RegisterNotAllowedException;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.mapper.UserMapper;
import com.coride.service.general.EmailService;
import com.coride.service.general.UserService;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);

        // verify email authenticity
        String userCode = userRegisterDTO.getVerificationCode();
        if (!emailService.checkVerificationCode(userRegisterDTO.getAccountNo(), userCode)){
            throw new RegisterNotAllowedException("Wrong email verification code!");
        }

        // verify email suffix
        String orgSuffix;

        Object cachedSuffix = stringRedisTemplate.opsForHash().get("cache:org:" + user.getIdOrganization().toString(), user.getIdOrganization().toString());
        if (cachedSuffix == null) {
            orgSuffix = userMapper.getSuffix(user);
            stringRedisTemplate.opsForHash().put("cache:org:" + user.getIdOrganization().toString(), user.getIdOrganization().toString(), orgSuffix);
        }
        else{
            orgSuffix = cachedSuffix.toString();
        }

        String userSuffix = user.getAccountNo().split("@")[1];

        if (!orgSuffix.equals(userSuffix))
            throw new RegisterNotAllowedException(MessageConstant.ORG_VERIFICATION_FAILED);

        //encrypt password
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));

        userMapper.insert(user);

        switch (userRegisterDTO.getAccountType()){
            case AccountTypeConstant.CARPOOLER:
            carpoolerMapper.insert(user);
                break;
            case AccountTypeConstant.DRIVER:
            driverMapper.insert(user);
                break;
        }
        log.info("User successfully registered: {}", user);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String accountNo = userLoginDTO.getAccountNo();
        String password = userLoginDTO.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        User user = null;

        String userJson = stringRedisTemplate.opsForValue().get("cache:user" + accountNo);

        if (StrUtil.isNotBlank(userJson)){
            user = JSON.parseObject(userJson, User.class);
            log.info("User successfully login: {}", user);
            return user;
        }

        switch (userLoginDTO.getAccountType()){
            case "Carpooler":
                user = userMapper.getCarpoolerByAccountNo(accountNo);
                break;

            case "Driver":
                user = userMapper.getDriverByAccountNo(accountNo);
                break;
        }

        if (user == null)
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);

        if (!password.equals(user.getPassword()))
            throw  new PasswordErrorException(MessageConstant.PASSWORD_ERROR);

        stringRedisTemplate.opsForValue().set("cache:user:"+accountNo, JSON.toJSONString(user));
        log.info("User successfully login: {}", user);

        return user;
    }

    @Override
    public AccountManagementDTO getAccount(Long id) {
        AccountManagementDTO account = userMapper.getAccount(id);
        return account;
    }

    @Override
    public void updateAccount(Long id, AccountManagementDTO accountManagementDTO) {
        accountManagementDTO.setPassword(DigestUtils.md5DigestAsHex(accountManagementDTO.getPassword().getBytes()));
        userMapper.updateAccount(id, accountManagementDTO);
    }
}
