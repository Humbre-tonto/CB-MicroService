package com.vericash.transaction.dto;

public record TransactionResponse(
    String status,
    Object data
) {}