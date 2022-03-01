package com.nttdata.transaction.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class Transaction {
    @Id
    private String id;
    private String type;
    private LocalDateTime creationTime;
    private Double transactionFee;
    private Double amount;
    private String destinationAccountNumber;
    private String accountId;
}
