package jwt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Based on https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
 */
public class Jwt {

    private static final String SECRET_KEY = "SUPER_SECRET_KEY*[2,0,1]";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String ISSUER = "com.martinsam.security";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private JSONObject payload = new JSONObject();
    private String signature;
    private String encodedHeader;

    private Jwt() {
        encodedHeader = encode(new JSONObject(JWT_HEADER));
    }

    public Jwt(JSONObject payload) {
        this(payload.getString("sub"), payload.getJSONArray("aud"), payload.getLong("exp"));
    }

    public Jwt(String sub, JSONArray aud, long expires) {
        this();
        payload.put("sub", sub);
        payload.put("aud", aud);
        payload.put("exp", expires);
        payload.put("iat", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        payload.put("iss", ISSUER);
        payload.put("jti", UUID.randomUUID().toString()); //how do we use this?
        signature = hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY);
    }

    public Jwt(String token) throws NoSuchAlgorithmException {
        this();
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Token format");
        }
        if (encodedHeader.equals(parts[0])) {
            encodedHeader = parts[0];
        } else {
            throw new NoSuchAlgorithmException("JWT Header is Incorrect: " + parts[0]);
        }

        payload = new JSONObject(decode(parts[1]));
        if (payload.isEmpty()) {
            throw new JSONException("Payload is Empty: ");
        }
        if (!payload.has("exp")) {
            throw new JSONException("Payload doesn't contain expiry " + payload);
        }
        signature = parts[2];
    }

    @Override
    public String toString() {
        return encodedHeader + "." + encode(payload) + "." + signature;
    }

    public boolean isValid() {
        return payload.getLong("exp") > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) //token not expired
                && signature.equals(hmacSha256(encodedHeader + "." + encode(payload), SECRET_KEY)); //signature matched
    }

    public String getSubject() {
        return payload.getString("sub");
    }

    public String getPayload(){
        return this.toString();
    }

    public List<String> getAudience() {
        JSONArray arr = payload.getJSONArray("aud");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }

    private static String encode(JSONObject obj) {
        return encode(obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private String hmacSha256(String data, String secret) {
        try {

            //MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);//digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(Jwt.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        final int EXPIRY_DAYS = 90;

        JSONObject jwtPayload = new JSONObject();
        jwtPayload.put("status", 0);

        JSONArray audArray = new JSONArray();
        audArray.put("admin");
        jwtPayload.put("sub", "John");

        jwtPayload.put("aud", audArray);
        LocalDateTime ldt = LocalDateTime.now().plusDays(EXPIRY_DAYS);
        jwtPayload.put("exp", ldt.toEpochSecond(ZoneOffset.UTC)); //this needs to be configured

        String token = new Jwt(jwtPayload).toString();
        System.out.println("token: " + token);

        System.out.println("Validate");

        Jwt incomingToken = new Jwt(token);
        if (incomingToken.isValid()) {
            List<String> audience = incomingToken.getAudience();
            System.out.println(incomingToken.payload);
        }
    }
}
