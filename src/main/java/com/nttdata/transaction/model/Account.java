package com.nttdata.transaction.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class Account {
    private String id;
    private String accountNumber;
    private Double maintenanceFee;
    private int movementLimit;
    private Double transactionFee;
    private Double balance;
    private LocalDateTime creationTime;
    List<Holder> holders;
    List<Signatory> signatories;
    private String customerProfile;
    private String customerId;
    private String productId;
}
