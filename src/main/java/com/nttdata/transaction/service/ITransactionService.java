package com.nttdata.transaction.service;

import com.nttdata.transaction.entity.Transaction;
import com.nttdata.transaction.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ITransactionService {

    Flux<Transaction> getAll();

    Mono<Transaction> getTransactionById(String id);

    Mono<Transaction> save(Transaction transaction);

    Mono<Transaction> update(Transaction transaction);

    void delete(String id);

    Mono<Account> getAccount(String accountId);

    Mono<Account> updateAccount(Account account);

    Flux<Transaction> findByAccountId(String accountId);

    Mono<Account> getByAccountNumber(String accountNumber);

    Mono<Transaction> transferMoney(Transaction transaction, String accountNumber, String toAccountNumber);

    Flux<Transaction> commissionsCharged(String accountId, LocalDate fromDate, LocalDate toDate);


}