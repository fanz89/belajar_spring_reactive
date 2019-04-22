package com.mif.controller;

import com.mif.model.Customer;
import com.mif.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("")
    public Flux<Customer> findAll() {
        return customerRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Customer>> findById(@PathVariable(value = "id") Long id) {

        return customerRepository.findById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("name/{name}")
    public Flux<Customer> findByName(@PathVariable(value = "name") String name) {
        return customerRepository.findByFullnameContainingIgnoreCase(name)
                .switchIfEmpty(Flux.error(new RuntimeException("Customer dengan nama " + name + " tidak ditemukan!!")));
    }

    @PostMapping("")
    public Mono<ResponseEntity<?>> create(@Valid @RequestBody Customer customer) {

        return customerRepository.findById(customer.getCustomerId())
                .flatMap(existingCustomer -> Mono.error(new RuntimeException("Customer sudah terdaftar dengan ID : " + customer.getCustomerId())))
                .then(customerRepository.findByEmail(customer.getEmail())
                        .flatMap(existingCustomer -> Mono.error(new RuntimeException("Customer sudah terdaftar dengan Email : " + customer.getEmail()))))
                .then(customerRepository.save(customer)
                        .then(Mono.just(new ResponseEntity<>(customer, HttpStatus.OK)))
                );

    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Customer>> update(@PathVariable(value = "id") Long id, @Valid @RequestBody Customer customer) {
        return customerRepository.findById(id)
                .flatMap(existingCustomer -> {
                    existingCustomer.setFullname(customer.getFullname());
                    existingCustomer.setEmail(customer.getEmail());
                    existingCustomer.setPhoneNumber(customer.getPhoneNumber());
                    return customerRepository.save(existingCustomer);
                }).map(updatedCustomer -> new ResponseEntity<>(updatedCustomer, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable(value = "id") Long id) {
        return customerRepository.findById(id)
                .flatMap(existingCustomer -> customerRepository.delete(existingCustomer)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                ).defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


}
