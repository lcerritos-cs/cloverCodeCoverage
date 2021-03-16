package com.clover.codecoverage.clover.controller;

import com.clover.codecoverage.clover.service.ITestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController

{
    private final Logger logger = LoggerFactory.getLogger(TestController.class);
    ITestService testService;

    @Autowired
    public TestController(ITestService testService) {
        this.testService = testService;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<Void> call() {
        logger.info("Calling to the service");
        logger.info("INFO LOG");
        logger.warn("WARN LOG");
        return ResponseEntity.ok(null);
    }

    @RequestMapping(value = "/test/new", method = RequestMethod.GET)
    public ResponseEntity<Void> newTest() {
        logger.info("Calling to the service 2");
        logger.info("INFO LOG");
        logger.info("INFO LOG 2");
        return ResponseEntity.ok(null);

    }
}
