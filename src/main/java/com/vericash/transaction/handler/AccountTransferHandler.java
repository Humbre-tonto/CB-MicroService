package com.vericash.transaction.handler;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Component
public class AccountTransferHandler implements TransactionHandler<TransactionRequest, TransactionResponse> {

    @Value("${transaction.account-transfer.acquiring-inst-id}")
    private String acquiringInstId;
    @Value("${transaction.account-transfer.mti}")
    private String mti;
    @Value("${transaction.account-transfer.processing-code.fincale}")
    private String processingCodeFincale;
    // Add other properties as needed...

    @Override
    public String getTransactionType() {
        return "ACCOUNT_TRANSFER";
    }

    @Override
    public String prepareRequest(TransactionRequest request) {
        Map<String, Object> data = request.data();
        String tranMTI = "0";

        Date date = new Date();
        SimpleDateFormat trandatetimeFormat = new SimpleDateFormat("MMddHHmmss");
        String trandatetime = trandatetimeFormat.format(date);
        SimpleDateFormat valuedateFormat = new SimpleDateFormat("MMdd");
        String valuedate = valuedateFormat.format(date);

        long randomNumber = 100000000 + new Random().nextInt(900000000);
        String stan = String.valueOf(randomNumber);

        String drAcctNum = (String) data.get("drAcctNum");
        String crAcctNum = (String) data.get("crAcctNum");
        BigDecimal amount = new BigDecimal((String) data.get("amount"));

        StringBuilder xml = new StringBuilder();
        xml.append("<C24TRANREQ>");
        xml.append("<ACQUIRINGINSTID>").append(acquiringInstId).append("</ACQUIRINGINSTID>");
        xml.append("<MTI>").append(mti).append("</MTI>");
        xml.append("<PROCESSINGCODE>").append(processingCodeFincale).append("</PROCESSINGCODE>");
        xml.append("<STAN>").append(stan).append("</STAN>");
        xml.append("<TRANDATETIME>").append(trandatetime).append("</TRANDATETIME>");
        xml.append("<VALUEDATE>").append(valuedate).append("</VALUEDATE>");
        xml.append("<TRANAMT>").append(amount.toString()).append("</TRANAMT>");
        xml.append("<DRACCTNUM>").append(drAcctNum).append("</DRACCTNUM>");
        xml.append("<CRACCTNUM>").append(crAcctNum).append("</CRACCTNUM>");
        // ... add other fields as needed
        xml.append("</C24TRANREQ>");

        return xml.toString();
    }

    @Override
    public TransactionResponse validateResponse(String response, TransactionRequest request) {
        String actionCode = com.vericash.transaction.parser.XmlUtils.getValue(response, "ACTION_CODE");
        if ("000".equals(actionCode)) {
            return new TransactionResponse("SUCCESS", "Account transfer successful");
        } else {
            return new TransactionResponse("ERROR", "Account transfer failed with action code: " + actionCode);
        }
    }
}