package com.vericash.transaction.factory;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import com.vericash.transaction.handler.TransactionHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionFactory {

    private final Map<String, TransactionHandler<TransactionRequest, TransactionResponse>> handlerMap = new HashMap<>();

    public TransactionFactory(List<TransactionHandler<TransactionRequest, TransactionResponse>> handlers) {
        for (TransactionHandler<TransactionRequest, TransactionResponse> handler : handlers) {
            handlerMap.put(handler.getTransactionType(), handler);
        }
    }

    public TransactionHandler<TransactionRequest, TransactionResponse> getHandler(String transactionType) {
        TransactionHandler<TransactionRequest, TransactionResponse> handler = handlerMap.get(transactionType);
        if (handler == null) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        return handler;
    }
}