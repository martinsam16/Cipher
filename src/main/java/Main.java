import cipher.ICipher;
import cipher.impl.ColumnTranspositionByPattern;
import cipher.impl.Rot13;
import jwt.Jwt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class Main implements ICipher<String> {
    private String SECRET_KEY;

    public Main(String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText.contains("ñ")) throw new IllegalArgumentException();
        final int EXPIRY_DAYS = 90;

        JSONObject jwtPayload = new JSONObject();
        jwtPayload.put("status", 0);

        JSONArray audArray = new JSONArray();
        audArray.put("admin");
        jwtPayload.put("sub", plainText);

        jwtPayload.put("aud", audArray);
        LocalDateTime ldt = LocalDateTime.now().plusDays(EXPIRY_DAYS);
        jwtPayload.put("exp", ldt.toEpochSecond(ZoneOffset.UTC)); //this needs to be configured

        String token = new Jwt(jwtPayload).toString();
        ColumnTranspositionByPattern columnTranspositionByPattern = new ColumnTranspositionByPattern(obtainPattern());
        Rot13 rot13 = new Rot13();

        String transpositionEncrypted = new String(columnTranspositionByPattern.encrypt(token.getBytes()));
        String rot13Encrypted = rot13.encrypt(transpositionEncrypted);
        return rot13Encrypted;
    }

    @Override
    public String decrypt(String cryptogram) {
        ColumnTranspositionByPattern columnTranspositionByPattern = new ColumnTranspositionByPattern(obtainPattern());
        Rot13 rot13 = new Rot13();
        String rot13Decrypted = rot13.decrypt(cryptogram);
        String transpositionDecrypted = new String(columnTranspositionByPattern.decrypt(rot13Decrypted.getBytes()));
        return transpositionDecrypted;
    }

    public int[] obtainPattern() {
        String[] numbers = this.SECRET_KEY.split(",");
        int[] pattern = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            pattern[i] = Integer.parseInt(numbers[i]);
        }
        return pattern;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {

        try {
            Main main = new Main("2,0,3,1");
            String plainText = "Contra98$#979[]{";
            System.out.println("plainText: " + plainText);

            //Encrypt
            String encrypted = main.encrypt(plainText);
            System.out.println("encrypted: " + encrypted);

            //Decrypt
            String decrypted = main.decrypt(encrypted);
            Jwt token = new Jwt(decrypted);
            if (token.isValid()) {
                System.out.println("decrypted: " + token.getPayload());
                System.out.println("subject: " + token.getSubject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
