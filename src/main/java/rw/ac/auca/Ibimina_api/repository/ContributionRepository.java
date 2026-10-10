package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.Contribution;
import rw.ac.auca.Ibimina_api.entity.ContributionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    @EntityGraph(attributePaths = {"member", "member.group"})
    Page<Contribution> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "member.group"})
    Page<Contribution> findByMemberId(Long memberId, Pageable pageable);

    @Query("select sum(c.amount) from Contribution c where c.member.id = :memberId and c.status = :status")
    BigDecimal sumByMember(@Param("memberId") Long memberId, @Param("status") ContributionStatus status);

    @Query("select sum(c.amount) from Contribution c where c.member.group.id = :groupId and c.status = :status")
    BigDecimal sumByGroup(@Param("groupId") Long groupId, @Param("status") ContributionStatus status);

    @Query("""
            select sum(c.amount) from Contribution c
            where c.member.group.id = :groupId and c.status = :status
              and c.contributionDate between :fromDate and :toDate
            """)
    BigDecimal sumByGroupBetween(@Param("groupId") Long groupId,
                                 @Param("status") ContributionStatus status,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);
}