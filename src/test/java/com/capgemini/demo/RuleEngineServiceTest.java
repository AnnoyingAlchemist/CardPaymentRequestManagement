package com.capgemini.demo;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.service.CaseService;
import com.capgemini.demo.service.ReportingService;
import com.capgemini.demo.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RuleEngineServiceTest {
    @Autowired
    CaseService caseService;
    @Autowired
    RuleEngineService ruleEngineService;
}
