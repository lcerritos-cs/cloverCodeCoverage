package com.clover.codecoverage.clover.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestServiceTest
{
    private Logger logger = LoggerFactory.getLogger(TestServiceTest.class);
    private TestServiceImpl testService = new TestServiceImpl();

    @Test
    public void test()
    {
//        logger.info("test log");
//        logger.info("test log");
//        testService.testFunction();
    }

//    @Test
//    public void failureTest()
//    {
//        logger.info("test fail log");
//        testService.testFunction();
//        Assert.assertFalse(false);
//    }
}
