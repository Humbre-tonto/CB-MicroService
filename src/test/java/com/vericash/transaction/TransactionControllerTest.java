package com.vericash.transaction;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnSuccessForAccountInquiry() {
        TransactionRequest request = new TransactionRequest("ACCOUNT_INQUIRY", Map.of(
                "accountNumber", "12345",
                "msisdn", "1234567890"
        ));
        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/transactions/process",
                request,
                TransactionResponse.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().status()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldReturnErrorForInvalidDomiciliaryAccount() {
        TransactionRequest request = new TransactionRequest("ACCOUNT_INQUIRY", Map.of(
                "accountNumber", "12345",
                "msisdn", "1234567890"
        ));
        String simulatedXmlResponse = "<C24TRANRES><CMPHONENO>1234567890</CMPHONENO><BALANCECURRENCY>USD</BALANCECURRENCY><SCHMCODE>INVALID_SCHEME</SCHMCODE></C24TRANRES>";

        com.vericash.transaction.handler.AccountInquiryHandler handler = new com.vericash.transaction.handler.AccountInquiryHandler();
        handler.init();
        TransactionResponse transactionResponse = handler.validateResponse(simulatedXmlResponse, request);
        assertThat(transactionResponse.status()).isEqualTo("ERROR");
    }


    @Test
    void shouldReturnSuccessForBalanceInquiry() {
        TransactionRequest request = new TransactionRequest("BALANCE_INQUIRY", Map.of("clientId", "98765"));
        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/transactions/process",
                request,
                TransactionResponse.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().status()).isEqualTo("SUCCESS");
    }
}