package com.capgemini.demo.ruleEngine.rules;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.ruleEngine.RuleSet;
import com.capgemini.demo.ruleEngine.RuleSuggestion;
import com.capgemini.demo.ruleEngine.priority;
import org.springframework.format.annotation.DurationFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class Chargeback implements RuleSet {

    @Override
    public RuleSuggestion evaluate(CaseFacade caseFacade) {
        RuleSuggestion suggestion = new RuleSuggestion();

        if(LocalDateTime.now().until(caseFacade.getClassification().getDueDate(), ChronoUnit.DAYS) <= 2){
            suggestion.setPriority(priority.CRITICAL);
            suggestion.setRecommendedNextAction("ESCALATE");
            return suggestion;
        }
        //TODO: implement actual rule logic
        suggestion.setRecommendedNextAction("MANUAL_EVALUATION");
        suggestion.setPriority(priority.UNKNOWN);

        return suggestion;
    }

}
