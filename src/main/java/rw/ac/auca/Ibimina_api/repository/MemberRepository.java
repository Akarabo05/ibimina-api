package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}