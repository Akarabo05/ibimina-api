package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "members", indexes = @Index(name = "idx_member_group", columnList = "group_id"))
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Full name may only contain letters, spaces, apostrophes and hyphens")
    @Column(nullable = false, length = 50)
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must be in the format 07XXXXXXXX")
    @Column(nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @NotNull(message = "Group is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    public Member() {}

    public Member(String fullName, String phoneNumber, String email, Group group) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.group = group;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
}