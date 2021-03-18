package com.clover.codecoverage.clover.controller;

import com.clover.codecoverage.clover.service.TestServiceImpl;
import org.junit.Test;
import org.mockito.Mock;

public class TestControllerTest
{
    @Mock
    private TestServiceImpl testService;

    @Test
    public void test() {
        TestController testController = new TestController(testService);
        testController.call();
    }

    @Test
    public void test2() {
//        TestController testController = new TestController(testService);
//        testController.newTest();
    }
}
