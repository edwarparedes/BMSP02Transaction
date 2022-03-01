package com.nttdata.transaction.controller;

import com.nttdata.transaction.entity.Transaction;
import com.nttdata.transaction.model.Account;
import com.nttdata.transaction.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    ITransactionService service;

    @GetMapping
    public Flux<Transaction> getTransactions(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Transaction>> getTransactionById(@PathVariable("id") String id){
        return service.getTransactionById(id)
                .map(savedMessage -> ResponseEntity.ok(savedMessage))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    Mono<Transaction> postTransaction(@RequestBody Transaction transaction){
        return service.save(transaction);
    }

    @PutMapping
    Mono<Transaction> updTransaction(@RequestBody Transaction transaction){
        return service.update(transaction);
    }

    @DeleteMapping("/{id}")
    void dltTransaction(@PathVariable("id") String id){
        service.delete(id);
    }

    @GetMapping("/account/{accountId}")
    public Mono<Account> getAccount(@PathVariable("accountId") String accountId){
        return service.getAccount(accountId);
    }

    @PutMapping("/account")
    Mono<Account> updAccount(@RequestBody Account account){
        return service.updateAccount(account);
    }

    @GetMapping("accountnumber/{accountNumber}")
    public Mono<ResponseEntity<Account>> getByAccountNumber(@PathVariable("accountNumber") String accountNumber){
        return service.getByAccountNumber(accountNumber)
                .map(savedMessage -> ResponseEntity.ok(savedMessage))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/gettransactionsbyaccount/{accountId}")
    public Flux<Transaction> getTransactionsByAccount(@PathVariable("accountId") String accountId){
        return service.findByAccountId(accountId);
    }

    @PostMapping("/transferMoney/{accountNumber}/{toAccountNumber}")
    Mono<Transaction> transferMoney(@RequestBody Transaction transaction, @PathVariable("accountNumber") String accountNumber, @PathVariable("toAccountNumber") String toAccountNumber){
        return service.transferMoney(transaction, accountNumber,  toAccountNumber);
    }

    @GetMapping("/commissionsCharged")
    public Flux<Transaction> commissionsCharged(@RequestParam String accountId, @RequestParam String fromDate, @RequestParam String toDate){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //convert String to LocalDate
        LocalDate localDate1 = LocalDate.parse(fromDate, formatter);
        LocalDate localDate2 = LocalDate.parse(toDate, formatter);

        return service.commissionsCharged(accountId, localDate1, localDate2);
    }

}
