package com.coride.service.general.impl;

import com.coride.constant.AccountTypeConstant;
import com.coride.constant.MessageConstant;
import com.coride.dto.UserRegisterDTO;
import com.coride.entity.User;
import com.coride.exception.RegisterNotAllowedException;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.mapper.UserMapper;
import com.coride.service.general.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CarpoolerMapper carpoolerMapper;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks // 自动将 Mock 的依赖注入到被测对象中
    private UserServiceImpl userService;

    @Test
    void testRegister_success() {
        // 模拟 StringRedisTemplate 的行为
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("cache:org:1", "1")).thenReturn("company.com");

        // 模拟 EmailService 的行为
        when(emailService.checkVerificationCode("test@company.com", "1234")).thenReturn(true);

        // 模拟 Mapper 的行为
        doNothing().when(userMapper).insert(any(User.class));
        doNothing().when(carpoolerMapper).insert(any(User.class));

        // 准备测试数据
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setAccountNo("test@company.com");
        userRegisterDTO.setPassword("password123");
        userRegisterDTO.setVerificationCode("1234");
        userRegisterDTO.setIdOrganization(1);
        userRegisterDTO.setAccountType(AccountTypeConstant.CARPOOLER);

        // 执行测试
        Assertions.assertDoesNotThrow(() -> userService.register(userRegisterDTO));

        // 验证依赖方法的调用
        verify(emailService, times(1)).checkVerificationCode("test@company.com", "1234");
        verify(hashOperations, times(1)).get("cache:org:1", "1");
        verify(userMapper, times(1)).insert(any(User.class));
        verify(carpoolerMapper, times(1)).insert(any(User.class));
        verify(driverMapper, never()).insert(any(User.class)); // 确保未调用 DriverMapper
    }
/*
    @Test
    void register_EmailVerificationFailed() {
        // Arrange: 设置测试数据
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setAccountNo("test@example.com");
        userRegisterDTO.setVerificationCode("wrong_code");

        // Mock: 模拟验证码验证失败
        when(emailService.checkVerificationCode(anyString(), anyString())).thenReturn(false);

        // Act & Assert: 验证抛出异常
        RegisterNotAllowedException exception = assertThrows(RegisterNotAllowedException.class, () -> userService.register(userRegisterDTO));
        assertEquals("Wrong email verification code!", exception.getMessage());
    }

    @Test
    void register_EmailSuffixMismatch() {
        // Arrange: 设置测试数据
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setAccountNo("test@wrong.com");
        userRegisterDTO.setVerificationCode("123456");
        userRegisterDTO.setIdOrganization(0);

        // Mock: 模拟缓存未命中，数据库返回正确后缀
        when(emailService.checkVerificationCode(anyString(), anyString())).thenReturn(true);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null); // 缓存未命中
        when(userMapper.getSuffix(any())).thenReturn("example.com"); // 数据库返回的邮箱后缀

        // Act & Assert: 验证抛出异常
        RegisterNotAllowedException exception = assertThrows(RegisterNotAllowedException.class, () -> userService.register(userRegisterDTO));
        assertEquals(MessageConstant.ORG_VERIFICATION_FAILED, exception.getMessage());
    }

    @Test
    void register_CacheMissAndInsertToCache() {
        // Arrange: 设置测试数据
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setAccountNo("test@example.com");
        userRegisterDTO.setVerificationCode("123456");
        userRegisterDTO.setAccountType(AccountTypeConstant.DRIVER);
        userRegisterDTO.setPassword("password123");
        userRegisterDTO.setIdOrganization(0);

        // Mock: 模拟缓存未命中但数据库命中
        when(emailService.checkVerificationCode(anyString(), anyString())).thenReturn(true);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null); // 缓存未命中
        when(userMapper.getSuffix(any())).thenReturn("example.com"); // 数据库返回邮箱后缀

        // Act
        assertDoesNotThrow(() -> userService.register(userRegisterDTO));

        // Assert
        verify(hashOperations, times(1)).put(anyString(), anyString(), eq("example.com")); // 验证是否更新了缓存
        verify(driverMapper, times(1)).insert(any(User.class)); // 验证是否插入了司机信息
    }*/
}