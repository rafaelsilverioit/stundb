package com.stundb.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ElectionServiceTest {

    @Spy private ElectionService testee;

    @Test
    void test_run() {
        testee.run();
        verify(testee).run(false);
    }
}
