package Servicing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerPhone;
    private String laptopModel;
    private String problemDescription;
    private String status;
    private Double totalAmount;
    private Double paidAmount;
    private String paymentStatus;
    private LocalDate orderDate;
    private String solution; // or fixDescription
    private boolean closed = false;
    private LocalDateTime closedAt;
    
    @ManyToOne
    private Customer customer;
    // ✅ Business logic
    public void updatePaymentStatus() {
        if (paidAmount == null) paidAmount = 0.0;
        if (totalAmount == null) totalAmount = 0.0;

        if (paidAmount >= totalAmount) this.paymentStatus = "PAID";
        else if (paidAmount > 0) this.paymentStatus = "PARTIAL";
        else this.paymentStatus = "UNPAID";
    }

    // ✅ Getters & Setters

    public Long getId() { return id; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getLaptopModel() { return laptopModel; }
    public void setLaptopModel(String laptopModel) { this.laptopModel = laptopModel; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    
    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }
    
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    
}