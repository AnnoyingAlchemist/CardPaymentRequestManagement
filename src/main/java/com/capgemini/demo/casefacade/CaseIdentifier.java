package com.capgemini.demo.casefacade;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CaseIdentifier {

    private String caseType;
    private String primaryTransactionId;
    private String cardNumberMasked;
    private String cardToken;
    private String customerId;
}
