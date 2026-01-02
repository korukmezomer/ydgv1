package com.example.backend;

import org.junit.jupiter.api.Test;

class BackendApplicationSmokeTest {

    @Test
    void main_shouldStartWithoutExceptions() {
        BackendApplication.main(new String[]{"--spring.profiles.active=test"});
    }
}


