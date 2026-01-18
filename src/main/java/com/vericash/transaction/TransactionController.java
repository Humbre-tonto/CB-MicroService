package com.vericash.transaction;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import com.vericash.transaction.factory.TransactionFactory;
import com.vericash.transaction.handler.TransactionHandler;
import com.vericash.transaction.parser.SoapXmlParser;
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

        String simulatedSoapResponse = getSimulatedResponse(request);
        String cleanXml = SoapXmlParser.extractCleanXml(simulatedSoapResponse);

        return handler.validateResponse(cleanXml, request);
    }

    private String getSimulatedResponse(TransactionRequest request) {
        String transactionType = request.transactionType();
        if ("ACCOUNT_INQUIRY".equals(transactionType)) {
            return """
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:fin="http://finaclews.org">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <fin:sendTransactionResponse>
                             <return><![CDATA[<C24TRANRES>
			<ACTION_CODE>000</ACTION_CODE>
			<STAN>071458825615</STAN>
			<TRAN_DATE_TIME>20150312100714</TRAN_DATE_TIME>
			<BALANCE_CURRENCY>NGN</BALANCE_CURRENCY>
			<AVAILABLE_BALANCE>6000000</AVAILABLE_BALANCE>
			<LEDGER_BALANCE>60000</LEDGER_BALANCE>
			<COUNTRY_CODE>NG</COUNTRY_CODE>
			<ACCOUNT_INFO>
				<ACCT_NAME>Mazen</ACCT_NAME>
				<SCHM_TYPE>CAIND</SCHM_TYPE>
				<ACCT_STATUS/>
				<ACCT_SOL_ID>048</ACCT_SOL_ID>
				<SCHM_CODE>SBDOM</SCHM_CODE>
				<GL_SUB_HEAD>21008</GL_SUB_HEAD>
				<LIEN_AMT>0000000000000000</LIEN_AMT>
			</ACCOUNT_INFO>
			<CUSTOMER_INFO>
				<TITLE/>
				<EMAIL/>
				<MOBILE>
					<C_M_PHONE_NO>1234567890</C_M_PHONE_NO>
				</MOBILE>
				<COUNTRY>NG</COUNTRY>
				<GENDER/>
				<FIRST_NAME/>
				<MIDDLE_NAME/>
				<LAST_NAME/>
				<OCCUPATION/>
				<LANGUAGE/>
				<ADDRESS_1/>
				<ADDRESS_2/>
				<ADDRESS_3/>
				<REGION/>
				<BIRTH_DATE/>
				<ID_TYPE/>
				<ID_NUMBER>8088877999</ID_NUMBER>
			</CUSTOMER_INFO>
                    </C24TRANRES>]]></return>
                          </fin:sendTransactionResponse>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """;
        } else if ("BALANCE_INQUIRY".equals(transactionType)) {
            return """
                    <NS1:Envelope xmlns:NS1="http://schemas.xmlsoap.org/soap/envelope/">
                       <NS1:Body>
                          <NS2:sendTransactionResponse xmlns:NS2="http://finaclews.org">
                             <return><?xml version="1.0" encoding="UTF-8"?><C24TRANRES><ACTION_CODE>000</ACTION_CODE><STAN>202601135927</STAN><TRAN_DATE_TIME>20260118135927</TRAN_DATE_TIME><AVAILABLE_BALANCE>+0000007829037510</AVAILABLE_BALANCE><LEDGER_BALANCE>+0000007829037910</LEDGER_BALANCE><BALANCE_CURRENCY>NGN</BALANCE_CURRENCY><COUNTRY_CODE>NG</COUNTRY_CODE></C24TRANRES></return>
                          </NS2:sendTransactionResponse>
                       </NS1:Body>
                    </NS1:Envelope>
                    """;
        } else if ("ACCOUNT_TRANSFER".equals(transactionType)) {
            return """
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:fin="http://finaclews.org">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <fin:sendTransactionResponse>
                             <return><![CDATA[<C24TRANRES><ACTION_CODE>000</ACTION_CODE><STAN>071458825615</STAN><TRAN_DATE_TIME>20150312100714</TRAN_DATE_TIME></C24TRANRES>]]></return>
                          </fin:sendTransactionResponse>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """;
        }
        return "";
    }
}
