package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.Loan;
import rw.ac.auca.Ibimina_api.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"member", "member.group"})
    Page<Loan> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "member.group"})
    Page<Loan> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<LoanStatus> statuses);
}