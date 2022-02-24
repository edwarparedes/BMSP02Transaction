package com.nttdata.transaction.service;

import com.nttdata.transaction.entity.Transaction;
import com.nttdata.transaction.model.Account;
import com.nttdata.transaction.repository.ITransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements ITransactionService {

    @Autowired
    ITransactionRepository repository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public Flux<Transaction> getAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Transaction> getTransactionById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        transaction.setCreationTime(LocalDateTime.now());

        return getAccount(transaction.getAccountId()).doOnNext(da -> {
            if(transaction.getType().equalsIgnoreCase("Withdrawal") && transaction.getAmount() > da.getBalance()){
                throw new RuntimeException("The account has insufficient funds");
            }
        }).flatMap(ga -> {
            if(transaction.getType().equalsIgnoreCase("Deposit")){
                ga.setBalance(ga.getBalance() + transaction.getAmount());
            }
            else if(transaction.getType().equalsIgnoreCase("Withdrawal")){
                ga.setBalance(ga.getBalance() - transaction.getAmount());
            }
            return updateAccount(ga).flatMap(ua -> {
                return repository.save(transaction);
            });
        });
        //return repository.save(transaction).fl;
    }

    @Override
    public Mono<Transaction> update(Transaction transaction) {
        return repository.save(transaction);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id).subscribe();
    }

    @Override
    public Mono<Account> getAccount(String accountId) {
        Mono<Account> accountMono = webClientBuilder.build()
                .get()
                .uri("http://localhost:8003/account/{accountId}", accountId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Account.class);
        return accountMono;
    }

    @Override
    public Mono<Account> updateAccount(Account account) {
        Mono<Account> accountMono = webClientBuilder.build()
                .put()
                .uri("http://localhost:8003/account")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(account))
                .retrieve()
                .bodyToMono(Account.class);
        return accountMono;
    }

    @Override
    public Flux<Transaction> findByAccountId(String accountId) {
        return repository.findByAccountId(accountId);
    }

}
