package com.gurumee.demoapi.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AppPropertiesTest {
    @Autowired
    private AppProperties appProperties;

    @Test
    @DisplayName("create test")
    public void test() {
        assertEquals("test", appProperties.getClientId());
        assertEquals("test", appProperties.getClientSecret());
    }

}