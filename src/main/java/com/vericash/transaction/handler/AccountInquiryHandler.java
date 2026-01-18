package com.vericash.transaction.handler;

import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionRequest;
import com.vericash.transaction.dto.TransactionResponse;
import com.vericash.transaction.parser.XmlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@Component
public class AccountInquiryHandler implements TransactionHandler<TransactionRequest, TransactionResponse> {

    @Value("${transaction.account-inquiry.uba-receiver-bank}")
    private String ubaReceiverBank;
    @Value("${transaction.account-inquiry.acquiring-inst-id}")
    private String acquiringInstId;
    @Value("${transaction.account-inquiry.mti}")
    private String mti;
    @Value("${transaction.account-inquiry.processing-code.fincale}")
    private String processingCodeFincale;
    @Value("${transaction.account-inquiry.processing-code.nip}")
    private String processingCodeNip;
    @Value("${transaction.account-inquiry.target-system.cor}")
    private String targetSystemCor;
    @Value("${transaction.account-inquiry.target-system.nip}")
    private String targetSystemNip;

    private final Properties domiciliaryAccountsProp = new Properties();

    @PostConstruct
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("domiciliary-accounts-scheme-codes.properties")) {
            if (input == null) {
                // In a real app, you'd probably log an error here.
                return;
            }
            domiciliaryAccountsProp.load(input);
        } catch (Exception e) {
            // Handle exception
        }
    }

    @Override
    public String getTransactionType() {
        return "ACCOUNT_INQUIRY";
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

        String countrycode = (String) data.get("countryIso2");
        String trancrncycode = (String) data.get("currency");
        String accountNumber = (String) data.get("accountNumber");
        String shortCode = (String) data.get("shortCode");


        StringBuilder xml = new StringBuilder();
        xml.append("<C24TRANREQ>");
        xml.append("<ACQUIRINGINSTID>").append(acquiringInstId).append("</ACQUIRINGINSTID>");
        xml.append("<COUNTRYCODE>").append(countrycode).append("</COUNTRYCODE>");
        xml.append("<DRACCTNUM>").append(accountNumber).append("</DRACCTNUM>");

        if (shortCode == null || shortCode.equals(ubaReceiverBank)) {
            xml.append("<PROCESSINGCODE>").append(processingCodeFincale).append("</PROCESSINGCODE>");
            xml.append("<TARGETSYSTEM>").append(targetSystemCor).append("</TARGETSYSTEM>");
        } else {
            xml.append("<PROCESSINGCODE>").append(processingCodeNip).append("</PROCESSINGCODE>");
            xml.append("<DESTINATIONCODE>").append(shortCode).append("</DESTINATIONCODE>");
            xml.append("<TARGETSYSTEM>").append(targetSystemNip).append("</TARGETSYSTEM>");
        }

        xml.append("<MTI>").append(mti).append("</MTI>");
        xml.append("<STAN>").append(stan).append("</STAN>");
        xml.append("<TRANCRNCYCODE>").append(trancrncycode).append("</TRANCRNCYCODE>");
        xml.append("<TRANDATETIME>").append(trandatetime).append("</TRANDATETIME>");
        xml.append("<VALUEDATE>").append(valuedate).append("</VALUEDATE>");
        xml.append("<TRANAMT>").append(tranMTI).append("</TRANAMT>");
        xml.append("</C24TRANREQ>");

        return xml.toString();
    }

    @Override
    public TransactionResponse validateResponse(String response, TransactionRequest request) {
        boolean isSme = (boolean) request.data().getOrDefault("isSme", false);
        boolean isFamily = (boolean) request.data().getOrDefault("isFamily", false);

        String bankMsisdn = com.vericash.transaction.parser.XmlUtils.getValue(response, "C_M_PHONE_NO");
        String senderMsisdn = (String) request.data().get("msisdn");

        if (bankMsisdn == null || bankMsisdn.trim().isEmpty()) {
            return new TransactionResponse("ERROR", "Invalid MSISDN in response");
        }

        if (!isSme && !isFamily) {
            if (!bankMsisdn.equals(senderMsisdn)) {
                return new TransactionResponse("ERROR", "MSISDN does not match");
            }
        }

        String currency = XmlUtils.getValue(response, "BALANCE_CURRENCY");
        String schemeCode = XmlUtils.getValue(response, "SCHM_CODE");

        if (isDomiciliaryCurrency(currency) && !isDomiciliaryAccount(currency, schemeCode)) {
            return new TransactionResponse("ERROR", "Invalid domiciliary account");
        }

        return new TransactionResponse("SUCCESS", "Account validation successful");
    }

    private boolean isDomiciliaryCurrency(String currency) {
        return domiciliaryAccountsProp.containsKey(currency);
    }

    private boolean isDomiciliaryAccount(String currency, String schemeCode) {
        String currencySchemeCode = domiciliaryAccountsProp.getProperty(currency);
        if (currencySchemeCode != null) {
            return Arrays.asList(currencySchemeCode.split(",")).contains(schemeCode);
        }
        return false;
    }
}