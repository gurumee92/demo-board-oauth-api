package com.gurumee.demoboardauthapi.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordEncodeTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void test() {
        String client = "test";
        String encode = passwordEncoder.encode(client);
        System.out.println(encode);
    }

}
