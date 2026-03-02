package com.capgemini.demo.casehelper;

import com.capgemini.demo.casefacade.CaseFacade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="case_history")
public class CaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    @Column(name = "history_id")
    private long historyId;

    @ManyToOne()//fetch = FetchType.LAZY
    @JoinColumn(name = "case_id")
    private CaseFacade caseId;

    //private long caseId;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "comment")
    private String comment;

    @Column(name = "changed_by")
    private String changedBy; //get from JWT

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
