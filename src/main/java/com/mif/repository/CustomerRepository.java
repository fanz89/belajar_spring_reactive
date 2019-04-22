package com.mif.repository;

import com.mif.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, Long> {

    Mono<Customer> findByEmail(String email);

    Flux<Customer> findByFullnameContainingIgnoreCase(String fullname);

}