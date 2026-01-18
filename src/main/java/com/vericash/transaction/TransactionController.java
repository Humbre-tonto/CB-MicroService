package com.vericash.transaction;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import com.vericash.transaction.factory.TransactionFactory;
import com.vericash.transaction.handler.TransactionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionFactory transactionFactory;

    public TransactionController(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    @PostMapping("/process")
    public TransactionResponse processTransaction(@RequestBody TransactionRequest request) {
        TransactionHandler<TransactionRequest, TransactionResponse> handler = transactionFactory.getHandler(request.transactionType());
        String xmlRequest = handler.prepareRequest(request);

        // In a real application, you would send the xmlRequest to the core banking system
        // and receive a response. For this example, we'll simulate a response.
        String simulatedXmlResponse = getSimulatedResponse(request);

        return handler.validateResponse(simulatedXmlResponse, request);
    }

    private String getSimulatedResponse(TransactionRequest request) {
        String transactionType = request.transactionType();
        if ("ACCOUNT_INQUIRY".equals(transactionType)) {
            return "<C24TRANRES><CMPHONENO>1234567890</CMPHONENO><BALANCECURRENCY>USD</BALANCECURRENCY><SCHMCODE>SBDOM</SCHMCODE></C24TRANRES>";
        } else if ("BALANCE_INQUIRY".equals(transactionType)) {
            return "<C24TRANRES><AVAILABLEBALANCE>100000</AVAILABLEBALANCE><BALANCECURRENCY>USD</BALANCECURRENCY></C24TRANRES>";
        }
        return "";
    }
}