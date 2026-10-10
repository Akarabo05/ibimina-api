package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 60, message = "Group name must be between 2 and 60 characters")
    @Column(nullable = false, unique = true, length = 60)
    private String groupName;

    @NotBlank(message = "Meeting frequency is required")
    @Column(nullable = false, length = 30)
    private String meetingFrequency;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "100.00", message = "Contribution amount must be at least 100")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal contributionAmount;

    public Group() {}

    public Group(String groupName, String meetingFrequency, BigDecimal contributionAmount) {
        this.groupName = groupName;
        this.meetingFrequency = meetingFrequency;
        this.contributionAmount = contributionAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getMeetingFrequency() { return meetingFrequency; }
    public void setMeetingFrequency(String meetingFrequency) { this.meetingFrequency = meetingFrequency; }
    public BigDecimal getContributionAmount() { return contributionAmount; }
    public void setContributionAmount(BigDecimal contributionAmount) { this.contributionAmount = contributionAmount; }
}