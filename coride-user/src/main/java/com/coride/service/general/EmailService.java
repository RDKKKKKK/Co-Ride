package com.coride.service.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 用于存储验证码和邮箱的映射
    private Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void sendVerificationCode(String toEmail) {
        String verificationCode = generateVerificationCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rdk1606@163.com");
        message.setTo(toEmail);
        message.setSubject("Verification Code for Team Carpool App");
        message.setText("Your verification code is: " + verificationCode);
        mailSender.send(message);

        // 存储验证码用于之后的匹配
        verificationCodes.put(toEmail, verificationCode);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    public boolean checkVerificationCode(String email, String code) {
        String correctCode = verificationCodes.get(email);
        return code.equals(correctCode);
    }
}
