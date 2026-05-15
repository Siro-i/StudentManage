package com.school.system.service;

import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Map<String, String> generateCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(120, 40, 4);
        captcha.setCharType(2);
        String captchaCode = captcha.text();
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        String key = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, captchaCode, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaImage", captcha.toBase64());

        return result;
    }

    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null || captchaId.isEmpty() || captchaCode.isEmpty()) {
            return false;
        }
        String key = CAPTCHA_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            return false;
        }
        redisTemplate.delete(key);
        return storedCode.equalsIgnoreCase(captchaCode);
    }
}