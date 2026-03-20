package com.capgemini.test;


import com.capgemini.demo.casefacade.CaseFacade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CardRequestPaymentManagementApplicationTests {

    CaseFacade testCase = new CaseFacade();
    @BeforeAll
    public static void createTestCase(){
    }

	@Test
	void contextLoads() {
	}

    @Test
    void getCase(){
        //new Assert(testCase.getAssignment().getCreatedBy().equals(""));
    }

}

