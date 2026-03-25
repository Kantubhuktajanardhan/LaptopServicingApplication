package Servicing;

import org.springframework.stereotype.Controller;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
public class LaptopServiceController {
    
    
	@PostMapping("/create-order")
	public String createOrder(@ModelAttribute ServiceOrder order, @RequestParam(required = false) String customerName,
	                          @RequestParam(required = false) String customerDob) {  	
    	// 🔍 check if customer exists
		Customer customer = customerRepository
		        .findByPhone(order.getCustomerPhone())
		        .orElseGet(() -> {
		            Customer newCustomer = new Customer();
		            newCustomer.setPhone(order.getCustomerPhone());
		            newCustomer.setName(customerName);

		            if (customerDob != null && !customerDob.isEmpty()) {
		                newCustomer.setDob(LocalDate.parse(customerDob));
		            }

		            return customerRepository.save(newCustomer);
		        });

        order.setStatus("Received");
        order.setOrderDate(LocalDate.now()); // ✅ ADD THIS
        order.updatePaymentStatus();
        order.setCustomer(customer);
        

        repository.save(order);
        return "redirect:/";
    }

    @GetMapping("/filter")
    public String filter(@RequestParam String status, Model model) {

        List<ServiceOrder> list = repository.findByStatusAndClosedFalse(status);

        model.addAttribute("orders", list);

        // ✅ Only total orders
        model.addAttribute("totalOrders", list.size());

        // ❌ Remove revenue & pending
        model.addAttribute("totalRevenue", 0);
        model.addAttribute("pendingAmount", 0);

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "dashboard";
    }
    
    @GetMapping("/update-status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status) {
        ServiceOrder order = repository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            repository.save(order);
        }
        return "redirect:/";
    }

    // ✅ Add Payment
    @GetMapping("/add-payment")
    public String addPayment(@RequestParam Long id, @RequestParam Double amount) {

        ServiceOrder order = repository.findById(id).orElse(null);

        if (order != null && amount != null && amount > 0) {

            double paid = order.getPaidAmount() == null ? 0 : order.getPaidAmount();
            double total = order.getTotalAmount() == null ? 0 : order.getTotalAmount();

            double newPaid = paid + amount;

            if (newPaid > total) {
                newPaid = total;
                amount = total - paid; // adjust payment
            }

            // ✅ UPDATE OLD FIELD (important)
            order.setPaidAmount(newPaid);
            order.updatePaymentStatus();
            repository.save(order);

            // ✅ NEW: SAVE PAYMENT HISTORY
            Payment payment = new Payment();
            payment.setAmount(amount);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setOrder(order);

            paymentRepository.save(payment);
        }

        return "redirect:/";
    }

    // ✅ Delete Order
    @GetMapping("/delete")
    public String deleteOrder(@RequestParam Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam String phone, Model model) {

        List<ServiceOrder> list =
            repository.findByClosedFalseAndCustomerPhoneContaining(phone);

        model.addAttribute("orders", list);
        model.addAttribute("totalOrders", list.size());

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "dashboard";
    }
    
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {

    	Page<ServiceOrder> pageData = repository.findByClosedFalse(PageRequest.of(page, 5));

    	List<ServiceOrder> orders = pageData.getContent();

        double total = orders.stream()
                .mapToDouble(o -> o.getTotalAmount() == null ? 0 : o.getTotalAmount())
                .sum();

        double paid = orders.stream()
                .mapToDouble(o -> o.getPaidAmount() == null ? 0 : o.getPaidAmount())
                .sum();

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalRevenue", paid);
        model.addAttribute("pendingAmount", total - paid);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "dashboard";
    }
    
        
    @GetMapping("/search-between-dates")
    public String searchBetweenDates(@RequestParam String startDate,
                                     @RequestParam String endDate,
                                     Model model) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        if (start.isAfter(end)) {
            return "redirect:/";
        }

        List<ServiceOrder> orders =
            repository.findByClosedFalseAndOrderDateBetween(start, end);

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "dashboard";
    }
    
    @GetMapping("/order-details")
    public String orderDetails(@RequestParam Long id, Model model) {

        ServiceOrder order = repository.findById(id).orElse(null);

        model.addAttribute("order", order);

        return "order-details";
    }
    
    @PostMapping("/update-solution")
    public String updateSolution(@RequestParam Long id,
                                 @RequestParam String solution) {

        ServiceOrder order = repository.findById(id).orElse(null);

        if (order != null) {
            order.setSolution(solution);
            repository.save(order);
        }

        return "redirect:/order-details?id=" + id;
               
    }
    
    @GetMapping("/close-order")
    public String closeOrder(@RequestParam Long id) {

        ServiceOrder order = repository.findById(id).orElse(null);

        if (order != null && !order.isClosed()) {
            order.setClosed(true);
            order.setClosedAt(LocalDateTime.now()); // ✅ save time
            repository.save(order);
        }

        return "redirect:/";
    }
    
    @GetMapping("/closed-orders")
    public String closedOrders(Model model) {

        List<ServiceOrder> orders = repository.findByClosedTrue();

        model.addAttribute("orders", orders);

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalOrders", orders.size());

        return "closed-orders";
    }
    
    @GetMapping("/closed-search")
    public String closedSearch(@RequestParam String phone, Model model) {

        List<ServiceOrder> orders =
            repository.findByClosedTrueAndCustomerPhoneContaining(phone);

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "closed-orders";
    }
    
    @GetMapping("/closed-search-between-dates")
    public String closedSearchBetweenDates(@RequestParam String startDate,
                                           @RequestParam String endDate,
                                           Model model) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<ServiceOrder> orders =
            repository.findByClosedTrueAndOrderDateBetween(start, end);

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "closed-orders";
    }
    
    @GetMapping("/reopen-order")
    public String reopenOrder(@RequestParam Long id) {

        ServiceOrder order = repository.findById(id).orElse(null);

        if (order != null && order.isClosed()) {
            order.setClosed(false);
            order.setClosedAt(null); // optional reset
            repository.save(order);
        }

        return "redirect:/closed-orders";
    }
    
    @GetMapping("/revenue")
    public String revenuePage(Model model) {

        List<Payment> payments = paymentRepository.findAll();

        double totalRevenue = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        model.addAttribute("payments", payments);
        model.addAttribute("totalRevenue", totalRevenue);

        return "revenue";
    }
    
    @GetMapping("/revenue-between-dates")
    public String revenueBetweenDates(@RequestParam String startDate,
                                      @RequestParam String endDate,
                                      Model model) {

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59);

        List<Payment> payments =
                paymentRepository.findByPaymentDateBetween(start, end);

        double totalRevenue = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        model.addAttribute("payments", payments);
        model.addAttribute("totalRevenue", totalRevenue);

        return "revenue";
    }
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository repository;

    public LaptopServiceController(OrderRepository repository,PaymentRepository paymentRepository,CustomerRepository customerRepository)
    {
	this.repository = repository;
	this.paymentRepository = paymentRepository;
	this.customerRepository = customerRepository;
    }
    
    private final CustomerRepository customerRepository;
    
    @GetMapping("/get-customer")
    @ResponseBody
    public Customer getCustomer(@RequestParam String phone) {
        return customerRepository.findByPhone(phone).orElse(null);
    }
    
    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }
    
    @GetMapping("/download-excel")
    public void downloadExcel(HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");

        Workbook workbook = new XSSFWorkbook();

        // SHEET 1: ORDERS
        Sheet sheet = workbook.createSheet("Orders");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Order ID");
        header.createCell(1).setCellValue("Customer Name");
        header.createCell(2).setCellValue("Customer DOB");
        header.createCell(3).setCellValue("Phone Number");
        header.createCell(4).setCellValue("Laptop Model");
        header.createCell(5).setCellValue("Problem Description");
        header.createCell(6).setCellValue("Fix");
        header.createCell(7).setCellValue("Total Amount");
        header.createCell(8).setCellValue("Paid Amount");
        header.createCell(9).setCellValue("Order Date");
        header.createCell(10).setCellValue("Status");
        header.createCell(11).setCellValue("Closed");
        header.createCell(12).setCellValue("Closed At");

        List<ServiceOrder> orders = repository.findAll();

        int rowNum = 1;

        for (ServiceOrder o : orders) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(o.getId());

            // Customer Name
            row.createCell(1).setCellValue(
            	    (o.getCustomer() != null && o.getCustomer().getName() != null && !o.getCustomer().getName().isEmpty())
            	        ? o.getCustomer().getName()
            	        : "NA"
            	);

            // DOB
            row.createCell(2).setCellValue(
            	    (o.getCustomer() != null && o.getCustomer().getDob() != null)
            	        ? o.getCustomer().getDob().toString()
            	        : "NA"
            	);

            // Phone
            row.createCell(3).setCellValue(
            	    (o.getCustomer() != null && o.getCustomer().getPhone() != null)
            	        ? o.getCustomer().getPhone()
            	        : (o.getCustomerPhone() != null ? o.getCustomerPhone() : "NA")
            	);

            row.createCell(4).setCellValue(o.getLaptopModel());
            row.createCell(5).setCellValue(o.getProblemDescription());

            // Fix
            row.createCell(6).setCellValue(
                o.getSolution() != null ? o.getSolution() : ""
            );

            row.createCell(7).setCellValue(
                o.getTotalAmount() != null ? o.getTotalAmount() : 0
            );

            row.createCell(8).setCellValue(
                o.getPaidAmount() != null ? o.getPaidAmount() : 0
            );

            row.createCell(9).setCellValue(
                o.getOrderDate() != null ? o.getOrderDate().toString() : ""
            );

            row.createCell(10).setCellValue(o.getStatus());

            row.createCell(11).setCellValue(o.isClosed() ? "YES" : "NO");

            row.createCell(12).setCellValue(
                o.getClosedAt() != null ? o.getClosedAt().toString() : ""
            );
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    @PostMapping("/upload-excel")
    public String uploadExcel(@RequestParam("file") MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            String phone = row.getCell(1).getStringCellValue();

            // check if order exists
            List<ServiceOrder> existing =
                    repository.findByCustomerPhoneContaining(phone);

            if (existing.isEmpty()) {
                ServiceOrder order = new ServiceOrder();
                order.setCustomerPhone(phone);
                order.setLaptopModel(row.getCell(2).getStringCellValue());
                order.setStatus(row.getCell(3).getStringCellValue());

                repository.save(order);
            }
        }

        workbook.close();

        return "redirect:/reports";
    }
    
    @GetMapping("/customers")
    public String customersPage() {
        return "customers";
    }
    
    @GetMapping("/customer-search")
    public String searchCustomers(@RequestParam String keyword, Model model) {

        List<Customer> customers =
            customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(keyword, keyword);

        model.addAttribute("customers", customers);

        return "customers";
    }
    
    
    
    
}