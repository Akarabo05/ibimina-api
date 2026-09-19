package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Group name is required")
    @Size(min = 2, max = 60, message = "Group name must be between 2 and 60 characters")
    @Column(nullable = false, length = 60)
    private String groupName;

    @NotNull(message = "Meeting frequency is required")
    @Column(nullable = false, length = 30)
    private String meetingFrequency;

    @NotNull(message = "Contribution amount is required")
    @Column(nullable = false)
    private Double contributionAmount;

    public Group() {}

    public Group(String groupName, String meetingFrequency, Double contributionAmount) {
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
    public Double getContributionAmount() { return contributionAmount; }
    public void setContributionAmount(Double contributionAmount) { this.contributionAmount = contributionAmount; }
}