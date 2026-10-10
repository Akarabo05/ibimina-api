package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Append-only: never edited or deleted. A mistake is corrected by reversing it. */
@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_contribution_member", columnList = "member_id"),
        @Index(name = "idx_contribution_date", columnList = "contribution_date")
})
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Amount must be at least 100")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Contribution date is required")
    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    @NotNull(message = "Member is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true, length = 30)
    private String receiptNumber;

    @Column(nullable = false, length = 50)
    private String recordedBy;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ContributionStatus status = ContributionStatus.ACTIVE;

    @Column(length = 255)
    private String reversalReason;
    @Column(length = 50)
    private String reversedBy;
    private LocalDateTime reversedAt;

    public Contribution() {}

    public Contribution(BigDecimal amount, LocalDate contributionDate, Member member) {
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.member = member;
    }

    @PrePersist
    void onCreate() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public ContributionStatus getStatus() { return status; }
    public void setStatus(ContributionStatus status) { this.status = status; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    public String getReversedBy() { return reversedBy; }
    public void setReversedBy(String reversedBy) { this.reversedBy = reversedBy; }
    public LocalDateTime getReversedAt() { return reversedAt; }
    public void setReversedAt(LocalDateTime reversedAt) { this.reversedAt = reversedAt; }
}