package com.stundb.service;

import com.stundb.BaseTest;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import static org.mockito.Mockito.verify;

public class ElectionServiceTest extends BaseTest {

    @Spy
    private ElectionService testee;

    @Test
    void test_run() {
        testee.run();
        verify(testee).run(false);
    }
}
