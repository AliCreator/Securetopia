package com.advance.service;

import org.springframework.data.domain.Page;

import com.advance.domain.Customer;
import com.advance.domain.Invoice;
import com.advance.domain.Stats;

public interface CustomerService {

	Customer createCustomer(Customer customer); 
	Customer updateCustomer(Customer customer); 
	Page<Customer> getCustomer(int page, int size); 
	Iterable<Customer> getCustomers(); 
	Customer getCustomer(Long id); 
	Page<Customer> searchCustomer(String name, int page, int size); 
	
	Invoice createInvoice(Invoice invoice); 
	Invoice updateInvoice(Invoice invoice); 
	Page<Invoice> getInvoice(int page, int size); 
	Invoice getInvoice(Long id);
	void addInvoiceToCustomer(Long id, Invoice invoice); 
	Stats getStats(); 
}
