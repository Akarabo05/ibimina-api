package rw.ac.auca.Ibimina_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.ac.auca.Ibimina_api.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}