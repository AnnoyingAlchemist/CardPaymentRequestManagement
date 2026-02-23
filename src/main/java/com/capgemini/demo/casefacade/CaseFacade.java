package com.capgemini.demo.casefacade;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
public class CaseFacade {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long id;

    @Column(name = "rule_eval_failed")
    private Boolean ruleEvalFailed;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status", column = @Column(name = "status")),
            @AttributeOverride(name = "priority", column = @Column(name = "priority")),
            @AttributeOverride(name = "recommendedNextAction", column = @Column(name = "recommended_next_action")),
            @AttributeOverride(name = "dueDate", column = @Column(name = "due_date"))
    })
    private CaseClassification classification;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "createdBy", column = @Column(name = "created_by")),
            @AttributeOverride(name = "assignedTo", column = @Column(name = "assignedTo")),
            @AttributeOverride(name = "createdAt", column = @Column(name = "created_at")),
            @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at")),
            @AttributeOverride(name = "resolvedAt", column = @Column(name = "resolved_at"))
    })
    private CaseAssignment assignment;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "caseType", column = @Column(name = "case_type")),
            @AttributeOverride(name = "primaryTransactionId", column = @Column(name = "primary_transaction_id")),
            @AttributeOverride(name = "cardNumberMasked", column = @Column(name = "card_number_masked")),
            @AttributeOverride(name = "cardToken", column = @Column(name = "card_token")),
            @AttributeOverride(name = "customerId", column = @Column(name = "customer_id"))
    })
    private CaseIdentifier identifier;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "transactionAmount", column = @Column(name = "transaction_amount")),
            @AttributeOverride(name = "transactionCurrency", column = @Column(name = "transaction_currency")),
            @AttributeOverride(name = "transactionDateTime", column = @Column(name = "transaction_date_time")),
            @AttributeOverride(name = "merchantCategoryCode", column = @Column(name = "merchant_category_code")),
            @AttributeOverride(name = "scheme", column = @Column(name = "scheme")),
            @AttributeOverride(name = "transactionCountry", column = @Column(name = "transaction_country"))
    })
    private CaseTransaction transaction;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "resolution", column = @Column(name = "resolution")),
            @AttributeOverride(name = "resolutionNotes", column = @Column(name = "resolution_notes"))
    })
    private CaseOutcome outcome;
}
