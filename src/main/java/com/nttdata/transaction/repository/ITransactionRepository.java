package com.nttdata.transaction.repository;

import com.nttdata.transaction.entity.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface ITransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByAccountId(String accountId);
}
