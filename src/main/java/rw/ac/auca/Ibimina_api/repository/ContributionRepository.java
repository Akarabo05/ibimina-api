package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
}