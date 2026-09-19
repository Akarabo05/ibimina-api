package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public Member create(Member member) {
        validate(member);
        return memberRepository.save(member);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }

    public Member update(Long id, Member updatedMember) {
        Member existing = findById(id);
        validate(updatedMember);
        existing.setFullName(updatedMember.getFullName());
        existing.setPhoneNumber(updatedMember.getPhoneNumber());
        existing.setGroup(updatedMember.getGroup());
        return memberRepository.save(existing);
    }

    public void delete(Long id) {
        Member existing = findById(id);
        memberRepository.delete(existing);
    }

    // Business logic validation
    private void validate(Member member) {
        if (member.getFullName() != null && !member.getFullName().matches("^[A-Za-z ]+$")) {
            throw new IllegalArgumentException("Full name must contain only letters and spaces");
        }
        if (member.getPhoneNumber() != null && !member.getPhoneNumber().matches("^07[0-9]{8}$")) {
            throw new IllegalArgumentException("Phone number must be in the format 07XXXXXXXX");
        }
    }
}