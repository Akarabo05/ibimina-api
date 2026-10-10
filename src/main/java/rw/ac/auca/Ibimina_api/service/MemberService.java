package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.MemberDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository members;
    private final GroupService groupService;
    private final AuditService audit;

    public MemberService(MemberRepository members, GroupService groupService, AuditService audit) {
        this.members = members;
        this.groupService = groupService;
        this.audit = audit;
    }

    @Transactional
    public MemberDtos.Response create(MemberDtos.Request r, String actor) {
        if (members.existsByPhoneNumber(r.phoneNumber())) {
            throw new IllegalArgumentException("A member with phone number " + r.phoneNumber() + " already exists");
        }
        Group group = groupService.find(r.groupId());
        Member saved = members.save(new Member(r.fullName().trim(), r.phoneNumber(), blankToNull(r.email()), group));
        audit.log(actor, "CREATE_MEMBER", "Member", String.valueOf(saved.getId()), "Registered " + saved.getFullName());
        return toResponse(saved);
    }

    @Transactional
    public MemberDtos.Response update(Long id, MemberDtos.Request r, String actor) {
        Member m = find(id);
        if (!m.getPhoneNumber().equals(r.phoneNumber()) && members.existsByPhoneNumber(r.phoneNumber())) {
            throw new IllegalArgumentException("A member with phone number " + r.phoneNumber() + " already exists");
        }
        m.setFullName(r.fullName().trim());
        m.setPhoneNumber(r.phoneNumber());
        m.setEmail(blankToNull(r.email()));
        m.setGroup(groupService.find(r.groupId()));
        audit.log(actor, "UPDATE_MEMBER", "Member", String.valueOf(id), "Updated " + m.getFullName());
        return toResponse(m);
    }

    /** Members are deactivated, never deleted, so their contribution history stays intact. */
    @Transactional
    public MemberDtos.Response setActive(Long id, boolean active, String actor) {
        Member m = find(id);
        m.setActive(active);
        audit.log(actor, active ? "ACTIVATE_MEMBER" : "DEACTIVATE_MEMBER", "Member", String.valueOf(id), m.getFullName());
        return toResponse(m);
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberDtos.Response> list(Long groupId, Pageable pageable) {
        Page<Member> page = groupId == null ? members.findAll(pageable) : members.findByGroupId(groupId, pageable);
        return PageResponse.of(page.map(MemberService::toResponse));
    }

    @Transactional(readOnly = true)
    public MemberDtos.Response get(Long id) {
        return toResponse(find(id));
    }

    public Member find(Long id) {
        return members.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member " + id + " not found"));
    }

    static MemberDtos.Response toResponse(Member m) {
        return new MemberDtos.Response(m.getId(), m.getFullName(), m.getPhoneNumber(), m.getEmail(),
                m.getGroup().getId(), m.getGroup().getGroupName(), m.isActive());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}