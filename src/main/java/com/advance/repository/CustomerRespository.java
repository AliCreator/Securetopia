package com.advance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.advance.domain.Customer;

public interface CustomerRespository extends PagingAndSortingRepository<Customer, Long>, ListCrudRepository<Customer, Long>{

	Page<Customer> findByNameContaining(String name, Pageable pageable); 
}
