
package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByGroupNameIgnoreCase(String groupName);
}