package com.advance.service.implementation;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.advance.domain.Customer;
import com.advance.domain.Invoice;
import com.advance.domain.Stats;
import com.advance.repository.CustomerRespository;
import com.advance.repository.InvoiceRepository;
import com.advance.rowmapper.StatRowMapper;
import com.advance.service.CustomerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.advance.query.CustomerQuery.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
	private final CustomerRespository customerRespository; 
	private final InvoiceRepository invoiceRepository; 
	private final NamedParameterJdbcTemplate jdbc; 

	@Override
	public Customer createCustomer(Customer customer) {
		customer.setCreatedAt(new Date());
		return customerRespository.save(customer);
	}

	@Override
	public Customer updateCustomer(Customer customer) {
		return customerRespository.save(customer);
	}

	@Override
	public Page<Customer> getCustomer(int page, int size) {
		return customerRespository.findAll(PageRequest.of(page, size));
	}

	@Override
	public Iterable<Customer> getCustomers() {
		return customerRespository.findAll();
	}

	@Override
	public Customer getCustomer(Long id) {
		return customerRespository.findById(id).get();
	}

	@Override
	public Page<Customer> searchCustomer(String name, int page, int size) {
		return customerRespository.findByNameContaining(name, PageRequest.of(page, size));
	}

	@Override
	public Invoice createInvoice(Invoice invoice) {
		invoice.setInvoiceNumber(RandomStringUtils.randomAlphabetic(8).toUpperCase());
		return invoiceRepository.save(invoice);
	}

	@Override
	public Invoice updateInvoice(Invoice invoice) {
		return invoiceRepository.save(invoice);
	}

	@Override
	public Page<Invoice> getInvoice(int page, int size) {
		return invoiceRepository.findAll(PageRequest.of(page, size));
	}

	@Override
	public void addInvoiceToCustomer(Long id, Invoice invoice) {
		invoice.setInvoiceNumber(RandomStringUtils.randomAlphabetic(8));
		Customer customer = customerRespository.findById(id).get();
		invoice.setCustomer(customer);
		invoiceRepository.save(invoice); 
	}

	@Override
	public Invoice getInvoice(Long id) {
		return invoiceRepository.findById(id).get();
	}

	@Override
	public Stats getStats() {
		return jdbc.queryForObject(GET_STATS_QUERY, Map.of(), new StatRowMapper());
	}

}
