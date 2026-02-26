package com.capgemini.demo.casehelper;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name="case_history")
public class CaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    @Column(name = "history_id")
    private int historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private int caseId;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "comment")
    private String comment;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    /*
•	History ID
•	Case ID
•	Old Status
•	New Status
    •	Comment
    •	Changed By (user ID from JWT)
    •	Timestamp

     */
}
