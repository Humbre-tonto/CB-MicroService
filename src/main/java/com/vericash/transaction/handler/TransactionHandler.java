package com.vericash.transaction.handler;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;

public interface TransactionHandler<T, R> {
    String prepareRequest(T request);
    R validateResponse(String response, T request);
    String getTransactionType();
}