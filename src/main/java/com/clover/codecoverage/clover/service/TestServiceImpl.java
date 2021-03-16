package com.clover.codecoverage.clover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements ITestService
{
    private Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @Override
    public void testFunction()
    {
        logger.info("TEST 1");
        logger.info("TEST 2");
        logger.info("TEST 3");
        logger.info("TEST 4");
    }

    public void testFunction2() {
        logger.info("Hello world!");
        logger.info("We're actually doing some records here :)");
        logger.info("Why don't you test me?");
    }
}
