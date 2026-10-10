package rw.ac.auca.Ibimina_api.repository;

import rw.ac.auca.Ibimina_api.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoanIdOrderByPaidAtDesc(Long loanId);
}