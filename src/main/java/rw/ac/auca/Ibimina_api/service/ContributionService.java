package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.entity.Contribution;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContributionService {

    @Autowired
    private ContributionRepository contributionRepository;

    public Contribution create(Contribution contribution) {
        validate(contribution);
        return contributionRepository.save(contribution);
    }

    public List<Contribution> findAll() {
        return contributionRepository.findAll();
    }

    public Contribution findById(Long id) {
        return contributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contribution not found with id: " + id));
    }

    public Contribution update(Long id, Contribution updatedContribution) {
        Contribution existing = findById(id);
        validate(updatedContribution);
        existing.setAmount(updatedContribution.getAmount());
        existing.setContributionDate(updatedContribution.getContributionDate());
        existing.setMember(updatedContribution.getMember());
        return contributionRepository.save(existing);
    }

    public void delete(Long id) {
        Contribution existing = findById(id);
        contributionRepository.delete(existing);
    }


    //Validation
    private void validate(Contribution contribution) {
        if (contribution.getContributionDate() != null
                && contribution.getContributionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Contribution date cannot be in the future");
        }
        if (contribution.getMember() == null || contribution.getMember().getId() == null) {
            throw new IllegalArgumentException("A valid member must be specified");
        }
    }
}