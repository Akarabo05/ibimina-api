package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.GroupDtos;
import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groups;
    private final AuditService audit;

    public GroupService(GroupRepository groups, AuditService audit) {
        this.groups = groups;
        this.audit = audit;
    }

    @Transactional
    public GroupDtos.Response create(GroupDtos.Request r, String actor) {
        String name = r.groupName().trim();
        if (groups.existsByGroupNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A group named '" + name + "' already exists");
        }
        Group saved = groups.save(new Group(name, r.meetingFrequency(), r.contributionAmount()));
        audit.log(actor, "CREATE_GROUP", "Group", String.valueOf(saved.getId()), "Created group " + name);
        return toResponse(saved);
    }

    @Transactional
    public GroupDtos.Response update(Long id, GroupDtos.Request r, String actor) {
        Group g = find(id);
        g.setGroupName(r.groupName().trim());
        g.setMeetingFrequency(r.meetingFrequency());
        g.setContributionAmount(r.contributionAmount());
        audit.log(actor, "UPDATE_GROUP", "Group", String.valueOf(id), "Updated group " + g.getGroupName());
        return toResponse(g);
    }

    @Transactional(readOnly = true)
    public List<GroupDtos.Response> list() {
        return groups.findAll().stream().map(GroupService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GroupDtos.Response get(Long id) {
        return toResponse(find(id));
    }

    public Group find(Long id) {
        return groups.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group " + id + " not found"));
    }

    static GroupDtos.Response toResponse(Group g) {
        return new GroupDtos.Response(g.getId(), g.getGroupName(), g.getMeetingFrequency(), g.getContributionAmount());
    }
}