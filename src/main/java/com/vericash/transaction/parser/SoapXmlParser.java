package com.vericash.transaction.parser;

import org.apache.commons.text.StringEscapeUtils;

public class SoapXmlParser {

    public static String extractCleanXml(String soapResponse) {
        if (soapResponse == null || soapResponse.isEmpty()) {
            return null;
        }

        try {
            String startTag = "<return>";
            String endTag = "</return>";
            int startIndex = soapResponse.indexOf(startTag);
            if (startIndex == -1) {
                return null;
            }
            startIndex += startTag.length();

            int endIndex = soapResponse.indexOf(endTag, startIndex);
            if (endIndex == -1) {
                return null;
            }

            String content = soapResponse.substring(startIndex, endIndex);

            // Handle CDATA
            if (content.startsWith("<![CDATA[")) {
                content = content.substring(9, content.length() - 3).trim();
            } else {
                // Handle escaped XML
                content = StringEscapeUtils.unescapeXml(content).trim();
            }

            // Remove xml declaration if present
            if (content.startsWith("<?xml")) {
                content = content.substring(content.indexOf("?>") + 2).trim();
            }

            return content;
        } catch (Exception e) {
            // In a real app, log this error
            e.printStackTrace();
            return null;
        }
    }
}