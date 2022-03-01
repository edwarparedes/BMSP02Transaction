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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        /*return getAccount(transaction.getAccountId()).doOnNext(da -> {
            if(transaction.getType().equalsIgnoreCase("Withdrawal") && transaction.getAmount() > da.getBalance()){
                throw new RuntimeException("The account has insufficient funds");
            }else if(transaction.getType().equalsIgnoreCase("Withdrawal") && (transaction.getAmount() + da.getTransactionFee()) > da.getBalance() && da.getMovementLimit() == 0){
                throw new RuntimeException("The account does not have sufficient funds due to the transaction fee ");
            }else if((da.getBalance() + transaction.getAmount()) < da.getTransactionFee() && transaction.getType().equalsIgnoreCase("Deposit") && da.getMovementLimit() == 0){

            }
        }).flatMap(ga -> {
            if(transaction.getType().equalsIgnoreCase("Deposit")){
                if(ga.getMovementLimit() > 0){
                    transaction.setTransactionFee(0.0);
                    ga.setMovementLimit(ga.getMovementLimit() - 1);
                    ga.setBalance(ga.getBalance() + transaction.getAmount());
                }else{
                    transaction.setTransactionFee(ga.getTransactionFee());
                    ga.setBalance((ga.getBalance() + transaction.getAmount()) - ga.getTransactionFee());
                }
            }
            else if(transaction.getType().equalsIgnoreCase("Withdrawal")){
                if(ga.getMovementLimit() > 0){
                    transaction.setTransactionFee(0.0);
                    ga.setMovementLimit(ga.getMovementLimit() - 1);
                    ga.setBalance(ga.getBalance() - transaction.getAmount());
                }else{
                    transaction.setTransactionFee(ga.getTransactionFee());
                    ga.setBalance(ga.getBalance() - (transaction.getAmount() + ga.getTransactionFee()));
                }
            }
            return updateAccount(ga).flatMap(ua -> {
                return repository.save(transaction);
            });
        });*/

        return getAccount(transaction.getAccountId()).flatMap(ga -> {
            Double amount = transaction.getAmount();    // Monto a depositar o retirar
            int movementLimit = ga.getMovementLimit();  // Límite de transacciones
            Double commission = 0.0;                    // Comisión por transacción
            Double balance = ga.getBalance();           // Saldo por cuenta
            Double newBalance = 0.0;                    // Nuevo saldo

            if(movementLimit == 0){
                commission = ga.getTransactionFee();
            }
            transaction.setTransactionFee(commission);

            if(transaction.getType().equalsIgnoreCase("Deposit"))
                newBalance = balance + (amount - commission);
            else if(transaction.getType().equalsIgnoreCase("Withdrawal"))
                newBalance = balance - (amount + commission);

            if(!(transaction.getType().equalsIgnoreCase("Deposit")) && !(transaction.getType().equalsIgnoreCase("Withdrawal")))
                throw new RuntimeException("Transaction type is not allowed");
            else if(amount <= 0)
                return Mono.error(new RuntimeException("The amount must be greater than 0"));
            else if(movementLimit == 0 && amount <= commission && transaction.getType().equalsIgnoreCase("Deposit"))
                return Mono.error(new RuntimeException("The amount must be greater than the commission"));
            else if(newBalance < 0)
                return Mono.error(new RuntimeException("The account has insufficient funds"));
            else{
                if(movementLimit > 0)
                    ga.setMovementLimit(movementLimit - 1);
                ga.setBalance(newBalance);
                return updateAccount(ga).flatMap(ua -> {
                    return repository.save(transaction);
                });
            }
        });
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

    @Override
    public Mono<Account> getByAccountNumber(String accountNumber) {
        Mono<Account> accountMono = webClientBuilder.build()
                .get()
                .uri("http://localhost:8003/account/accountnumber/{accountNumber}", accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Account.class);
        return accountMono;
    }

    @Override
    public Mono<Transaction> transferMoney(Transaction transaction, String accountNumber, String toAccountNumber) {
        transaction.setCreationTime(LocalDateTime.now());

//        return getByAccountNumber(accountNumber).flatMap(ga -> {
//            return getByAccountNumber(toAccountNumber).flatMap(b -> {
//
//                Double amount = transaction.getAmount();    // Monto a depositar o retirar
//                int movementLimit = ga.getMovementLimit();  // Límite de transacciones
//                Double commission = 0.0;                    // Comisión por transacción
//                Double balance = ga.getBalance();           // Saldo por accountNumber
//                Double newBalance = 0.0;                    // Nuevo saldo
//
//                Double balance2 = b.getBalance();           // Saldo por toAccountNumber
//                Double newBalance2 = 0.0;                    // Nuevo saldo de toAccountNumber
//
//                if(movementLimit == 0){
//                    commission = ga.getTransactionFee();
//                }
//                transaction.setTransactionFee(commission);
//
//                if(transaction.getType().equalsIgnoreCase("Deposit")){
//                    //newBalance = balance + (amount - commission);
//
//                    newBalance = balance - (amount + commission);
//                    newBalance2 = balance2 + amount;
//
//                }
//                else if(transaction.getType().equalsIgnoreCase("Withdrawal"))
//                    newBalance = balance - (amount + commission);
//
//                if(!(transaction.getType().equalsIgnoreCase("Deposit")) && !(transaction.getType().equalsIgnoreCase("Withdrawal")))
//                    throw new RuntimeException("Transaction type is not allowed");
//                else if(amount <= 0)
//                    return Mono.error(new RuntimeException("The amount must be greater than 0"));
//                else if(movementLimit == 0 && amount <= commission && transaction.getType().equalsIgnoreCase("Deposit"))
//                    return Mono.error(new RuntimeException("The amount must be greater than the commission"));
//                else if(newBalance < 0)
//                    return Mono.error(new RuntimeException("The account has insufficient funds"));
//                else{
//                    if(movementLimit > 0)
//                        ga.setMovementLimit(movementLimit - 1);
//                    ga.setBalance(newBalance);
//                    b.setBalance(newBalance2);
//                    return updateAccount(ga).flatMap(ua -> {
//                        return updateAccount(b).flatMap(ba -> {
//                            return repository.save(transaction);
//                        });
//
//                    });
//                }
//            });
//        });

        return getByAccountNumber(accountNumber).flatMap(ga -> {
            return getByAccountNumber(toAccountNumber).flatMap(b -> {
                transaction.setAccountId(ga.getId());
                transaction.setDestinationAccountNumber(toAccountNumber);

                Double amount = transaction.getAmount();    // Monto a depositar o retirar
                int movementLimit = ga.getMovementLimit();  // Límite de transacciones
                Double commission = 0.0;                    // Comisión por transacción
                Double balance = ga.getBalance();           // Saldo por accountNumber
                Double newBalance = 0.0;                    // Nuevo saldo

                Double balance2 = b.getBalance();           // Saldo por toAccountNumber
                Double newBalance2 = 0.0;                    // Nuevo saldo de toAccountNumber

                if(movementLimit == 0){
                    commission = ga.getTransactionFee();
                }
                transaction.setTransactionFee(commission);

                if(transaction.getType().equalsIgnoreCase("Deposit")){
                    //newBalance = balance + (amount - commission);

                    newBalance = balance - (amount + commission);
                    newBalance2 = balance2 + amount;

                }
                else if(transaction.getType().equalsIgnoreCase("Withdrawal"))
                    newBalance = balance - (amount + commission);

                if(!(transaction.getType().equalsIgnoreCase("Deposit")))
                    throw new RuntimeException("Transaction type is not allowed");
                else if(amount <= 0)
                    return Mono.error(new RuntimeException("The amount must be greater than 0"));
                else if(movementLimit == 0 && amount <= commission && transaction.getType().equalsIgnoreCase("Deposit"))
                    return Mono.error(new RuntimeException("The amount must be greater than the commission"));
                else if(newBalance < 0)
                    return Mono.error(new RuntimeException("The account has insufficient funds"));
                else{
                    if(movementLimit > 0)
                        ga.setMovementLimit(movementLimit - 1);
                    ga.setBalance(newBalance);
                    b.setBalance(newBalance2);
                    return updateAccount(ga).flatMap(ua -> {
                        return updateAccount(b).flatMap(ba -> {
                            return repository.save(transaction);
                        });

                    });
                }
            });
        });
    }

    @Override
    public Flux<Transaction> commissionsCharged(String accountId, LocalDate fromDate, LocalDate toDate) {
        return getAll().filter(a -> {
            return a.getAccountId().equalsIgnoreCase(accountId);
        }).filter(b -> {
            LocalDate date = b.getCreationTime().toLocalDate();
            return date.isBefore(toDate) && date.isAfter(fromDate);
        }).filter(c -> {
            return c.getTransactionFee() > 0;
        });
    }
}
