package com.stundb.service;

import static org.mockito.Mockito.verify;

import com.stundb.BaseTest;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;

public class ElectionServiceTest extends BaseTest {

    @Spy private ElectionService testee;

    @Test
    void test_run() {
        testee.run();
        verify(testee).run(false);
    }
}
