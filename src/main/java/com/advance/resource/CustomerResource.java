package com.advance.resource;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.advance.domain.Customer;
import com.advance.domain.HttpResponse;
import com.advance.domain.Invoice;
import com.advance.dto.UserDTO;
import com.advance.report.CustomerReport;
import com.advance.service.CustomerService;
import com.advance.service.UserService;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.MediaType.parseMediaType;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerResource {

	private final CustomerService customerService;
	private final UserService userService;

	@GetMapping("/list")
	public ResponseEntity<HttpResponse> getCustomers(@AuthenticationPrincipal UserDTO user,
			@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customers",
						customerService.getCustomer(page.orElse(0), size.orElse(10)), "stats", customerService.getStats()))
				.message("Customers retrieved!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@PostMapping("/create")
	public ResponseEntity<HttpResponse> createCustomer(@AuthenticationPrincipal UserDTO user,
			@RequestBody Customer customer) {
		return ResponseEntity.created(URI.create(""))
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customer",
								customerService.createCustomer(customer)))
						.message("Customer created").status(HttpStatus.CREATED).statusCode(HttpStatus.CREATED.value())
						.build());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<HttpResponse> getCustomer(@AuthenticationPrincipal UserDTO user,
			@PathVariable("id") Long id) {
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customer",
								customerService.getCustomer(id)))
						.message("Customer retrieved").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/search")
	public ResponseEntity<HttpResponse> searchCustomer(@AuthenticationPrincipal UserDTO user,
			@RequestParam Optional<String> name, @RequestParam Optional<Integer> page,
			@RequestParam Optional<Integer> size) {
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customer",
								customerService.searchCustomer(name.orElse(""), page.orElse(0), size.orElse(10))))
						.message("Customer found").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}
	
	@PutMapping("/update")
	public ResponseEntity<HttpResponse> updateCustomer(@AuthenticationPrincipal UserDTO user, @RequestBody Customer customer) {
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customer", customerService.updateCustomer(customer)))
				.message("Customer updated!")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build());
	}
	
	@PostMapping("/invoice/create")
	public ResponseEntity<HttpResponse> createInvoice(@AuthenticationPrincipal UserDTO user, @RequestBody Invoice invoice) {
		return ResponseEntity.created(URI.create("")).body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "invoice", customerService.createInvoice(invoice)))
				.message("Invoice created!")
				.status(HttpStatus.CREATED)
				.statusCode(HttpStatus.CREATED.value())
				.build()); 
	}
	
	@GetMapping("/invoice/new")
	public ResponseEntity<HttpResponse> newInvoice(@AuthenticationPrincipal UserDTO user) {
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customers", customerService.getCustomers()))
				.message("Customers retrieved")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build()); 
	}
	
	
	@GetMapping("/invoice/list")
	public ResponseEntity<HttpResponse> listInvoices(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "invoices", customerService.getInvoice(page.orElse(0), size.orElse(10))))
				.message("Invoices retrieved")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build());
	}
	
	@GetMapping("/invoice/get/{id}")
	public ResponseEntity<HttpResponse> getInvoice(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id) {
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "invoice", customerService.getInvoice(id)))
				.message("Invoice retrieved!")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build());
	}
	
	@PutMapping("/invoice/update")
	public ResponseEntity<HttpResponse> searchInvoice(@AuthenticationPrincipal UserDTO user, @RequestBody Invoice invoice) {
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "invoice", customerService.updateInvoice(invoice)))
				.message("Invoice updated!")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build()); 
	}
	
	@PostMapping("/invoice/addtocustomer/{id}")
	public ResponseEntity<HttpResponse> addInvoiceToCustomer(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id, @RequestBody Invoice invoice) {
		customerService.addInvoiceToCustomer(id, invoice);
		return ResponseEntity.ok().body(HttpResponse.builder()
				.timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserByEmail(user.getEmail()), "customers", customerService.getCustomers()))
				.message("Invoice updated!")
				.status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value())
				.build());
	}
	
	@GetMapping("/downolad/report")
	public ResponseEntity<Resource> downloadReport() {
		List<Customer> customers = new ArrayList<>(); 
		customerService.getCustomers().iterator().forEachRemaining(customers::add); 
		CustomerReport report = new CustomerReport(customers); 
		HttpHeaders headers = new HttpHeaders(); 
		headers.add("File-Name", "customer-report.xlsx"); 
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name=customer-report.xlsx"); 
		return ResponseEntity.ok().contentType(parseMediaType("application/vnd.ms-excel")).headers(headers).body(report.export()); 
		
	}
}
