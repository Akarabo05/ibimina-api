package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public Group create(Group group) {
        validate(group);
        return groupRepository.save(group);
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    public Group findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
    }

    public Group update(Long id, Group updatedGroup) {
        Group existing = findById(id);
        validate(updatedGroup);
        existing.setGroupName(updatedGroup.getGroupName());
        existing.setMeetingFrequency(updatedGroup.getMeetingFrequency());
        existing.setContributionAmount(updatedGroup.getContributionAmount());
        return groupRepository.save(existing);
    }

    public void delete(Long id) {
        Group existing = findById(id);
        groupRepository.delete(existing);
    }

    // Business logic validation
    private void validate(Group group) {
        if (group.getContributionAmount() != null && group.getContributionAmount() <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero");
        }
        if (group.getMeetingFrequency() != null) {
            String freq = group.getMeetingFrequency().trim().toLowerCase();
            if (!freq.equals("weekly") && !freq.equals("monthly")) {
                throw new IllegalArgumentException("Meeting frequency must be 'weekly' or 'monthly'");
            }
        }
    }
}