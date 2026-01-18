package com.vericash.transaction.dto;

import java.util.Map;

public record TransactionRequest(
    String transactionType,
    Map<String, Object> data
) {}