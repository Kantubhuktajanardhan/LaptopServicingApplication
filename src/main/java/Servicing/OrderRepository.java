package Servicing;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.List;

public interface OrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByStatus(String status);
    List<ServiceOrder> findByCustomerPhoneContaining(String phone);
    List<ServiceOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
 // Active orders
    List<ServiceOrder> findByClosedFalse();

    // Closed orders
    List<ServiceOrder> findByClosedTrue();

    // Closed + phone search
    List<ServiceOrder> findByClosedTrueAndCustomerPhoneContaining(String phone);

    // Closed + date range
    List<ServiceOrder> findByClosedTrueAndOrderDateBetween(LocalDate start, LocalDate end);
    
    List<ServiceOrder> findByStatusAndClosedFalse(String status);
    
    Page<ServiceOrder> findByClosedFalse(Pageable pageable);
    
    List<ServiceOrder> findByClosedFalseAndCustomerPhoneContaining(String phone);

    List<ServiceOrder> findByClosedFalseAndOrderDateBetween(LocalDate start, LocalDate end);
}