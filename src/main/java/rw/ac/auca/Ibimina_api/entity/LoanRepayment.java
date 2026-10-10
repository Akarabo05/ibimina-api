package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments", indexes = @Index(name = "idx_repayment_loan", columnList = "loan_id"))
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String recordedBy;

    public LoanRepayment() {}

    public LoanRepayment(Loan loan, BigDecimal amount, String recordedBy) {
        this.loan = loan;
        this.amount = amount;
        this.recordedBy = recordedBy;
    }

    public Long getId() { return id; }
    public Loan getLoan() { return loan; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getRecordedBy() { return recordedBy; }
}