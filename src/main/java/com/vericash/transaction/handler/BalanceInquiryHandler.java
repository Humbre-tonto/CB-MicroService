package com.vericash.transaction.handler;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

@Component
public class BalanceInquiryHandler implements TransactionHandler<TransactionRequest, TransactionResponse> {

    @Value("${transaction.balance-inquiry.processing-code.gtp}")
    private String processingCodeGtp;

    @Value("${transaction.balance-inquiry.processing-code.fincale}")
    private String processingCodeFincale;

    @Value("${transaction.balance-inquiry.target-system.gtp}")
    private String targetSystemGtp;

    @Value("${transaction.balance-inquiry.target-system.cor}")
    private String targetSystemCor;

    @Override
    public String getTransactionType() {
        return "BALANCE_INQUIRY";
    }

    @Override
    public String prepareRequest(TransactionRequest request) {
        Map<String, Object> data = request.data();
        String countrycode = (String) data.get("countryIso2");
        String tranCrncyCode = (String) data.get("currency");
        String accountNumber = (String) data.get("accountNumber");
        String clientId = (String) data.get("clientId");

        StringBuilder xml = new StringBuilder();
        xml.append("<C24TRANREQ>");
        xml.append("<COUNTRYCODE>").append(countrycode).append("</COUNTRYCODE>");
        xml.append("<TRANCRNCYCODE>").append(tranCrncyCode).append("</TRANCRNCYCODE>");

        if (accountNumber == null) {
            xml.append("<CUSTOMERID>").append(clientId).append("</CUSTOMERID>");
            xml.append("<PROCESSINGCODE>").append(processingCodeGtp).append("</PROCESSINGCODE>");
            xml.append("<TARGETSYSTEM>").append(targetSystemGtp).append("</TARGETSYSTEM>");
        } else {
            xml.append("<DRACCTNUM>").append(accountNumber).append("</DRACCTNUM>");
            xml.append("<PROCESSINGCODE>").append(processingCodeFincale).append("</PROCESSINGCODE>");
            xml.append("<TARGETSYSTEM>").append(targetSystemCor).append("</TARGETSYSTEM>");
        }

        xml.append("</C24TRANREQ>");

        return xml.toString();
    }

    @Override
    public TransactionResponse validateResponse(String response, TransactionRequest request) {
        String availableBalanceStr = XmlParser.getValue(response, "AVAILABLEBALANCE");
        String currencyCode = XmlParser.getValue(response, "BALANCECURRENCY");
        double availableBalanceDouble = Double.parseDouble(availableBalanceStr);

        // This would come from wallet info in a real scenario
        int balanceDecimalPoints = 2;
        int balanceDivideFactor = 100;

        String formattedBalance;
        BigDecimal numericBalance;

        if (isBankAccount(response)) {
            formattedBalance = formatBalance(availableBalanceDouble / balanceDivideFactor, currencyCode, balanceDecimalPoints);
            numericBalance = new BigDecimal(availableBalanceDouble / balanceDivideFactor);
        } else {
            formattedBalance = formatBalance(availableBalanceDouble, currencyCode, balanceDecimalPoints);
            numericBalance = new BigDecimal(availableBalanceDouble);
        }


        return new TransactionResponse("SUCCESS", Map.of(
            "formattedAvailableBalance", formattedBalance,
            "numericBalance", numericBalance,
            "currency", currencyCode
        ));
    }

    private boolean isBankAccount(String response) {
        // In a real scenario, we'd have a more reliable way to determine this
        return response.contains("<DRACCTNUM>");
    }

    private String formatBalance(Double amount, String currencyCode, int fractionDigits) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        try {
            DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
            decimalFormatSymbols.setCurrencySymbol(currencyCode + " ");
            ((DecimalFormat) numberFormat).setDecimalFormatSymbols(decimalFormatSymbols);
            ((DecimalFormat) numberFormat).setMinimumFractionDigits(fractionDigits);
            return numberFormat.format(amount);
        } catch (Exception ex) {
            return currencyCode + " " + amount;
        }
    }
}