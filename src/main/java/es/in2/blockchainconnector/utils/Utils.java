package es.in2.blockchainconnector.utils;

import es.in2.blockchainconnector.exception.BrokerNotificationParserException;
import es.in2.blockchainconnector.exception.HashLinkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Utils {

    public static final String SHA_256_ALGORITHM = "SHA-256";
    public static final String HASHLINK_PREFIX = "?hl=";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT_HEADER = "Accept";

    public static final String HASH_PREFIX = "0x";

    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }



    public static String calculateSHA256Hash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return HASH_PREFIX + bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String extractHlValue(String entityUrl) {
        try {
            URI uri = new URI(entityUrl);
            String query = uri.getQuery();
            if (query == null) {
                return "";
            }
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "hl".equals(keyValue[0])) {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
        } catch (URISyntaxException e) {
            throw new BrokerNotificationParserException("Error while extracting hl value from datalocation");
        }
        return null;
    }

    public static boolean hasHLParameter(String urlString) {
        try {
            URL url = new URL(urlString);
            Map<String, String> queryParams = splitQuery(url);
            log.debug("Query params: {}", queryParams);
            return queryParams.containsKey("hl");
        } catch (MalformedURLException e) {
            throw new HashLinkException("Error parsing datalocation");
        }
    }


    private static Map<String, String> splitQuery(URL url) {
        if (url.getQuery() == null || url.getQuery().isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> queryPairs = new HashMap<>();
        String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(pair.substring(0, idx), idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null);
        }
        return queryPairs;
    }



}