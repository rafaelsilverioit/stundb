package com.stundb;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class BaseTest {
    @BeforeEach
    void init_mocks() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }
}
