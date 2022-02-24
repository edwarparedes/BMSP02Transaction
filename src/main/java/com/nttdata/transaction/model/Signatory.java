package com.nttdata.transaction.model;

import lombok.Data;

@Data
public class Signatory {
    private String name;
    private String lastName;
    private String documentType;
    private String documentNumber;
}
