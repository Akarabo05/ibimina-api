package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDate;

@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.0", message = "Amount must be at least 100")
    @Column(nullable = false)
    private Double amount;

    @NotNull(message = "Contribution date is required")
    @Column(nullable = false)
    private LocalDate contributionDate;

    @NotNull(message = "Member is required")
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Contribution() {}

    public Contribution(Double amount, LocalDate contributionDate, Member member) {
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.member = member;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
}