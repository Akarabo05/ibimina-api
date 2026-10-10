package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.ContributionStatus;
import rw.ac.auca.Ibimina_api.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    @EntityGraph(attributePaths = "group")
    Page<Member> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "group")
    Page<Member> findByGroupId(Long groupId, Pageable pageable);

    long countByGroupIdAndActiveTrue(Long groupId);

    /** Active members of a group with no ACTIVE contribution inside the given period. */
    @EntityGraph(attributePaths = "group")
    @Query("""
            select m from Member m
            where m.group.id = :groupId and m.active = true
              and not exists (select c.id from Contribution c
                              where c.member = m and c.status = :status
                                and c.contributionDate between :fromDate and :toDate)
            order by m.fullName
            """)
    List<Member> findUnpaid(@Param("groupId") Long groupId,
                            @Param("status") ContributionStatus status,
                            @Param("fromDate") LocalDate fromDate,
                            @Param("toDate") LocalDate toDate);
}