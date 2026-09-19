package rw.ac.auca.Ibimina_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name must contain only letters and spaces")
    @Column(nullable = false, length = 50)
    private String fullName;

    @NotNull(message = "Phone number is required")
    @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must be in the format 07XXXXXXXX")
    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    public Member() {
    }

    public Member(String fullName, String phoneNumber, Group group) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.group = group;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}