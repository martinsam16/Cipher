package cipher.impl;

import cipher.ICipher;

public class Rot13 implements ICipher<String> {
    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public String encrypt(String plainText) {
        char caracter;
        StringBuilder cryptogram = new StringBuilder();

        for (int i = 0; i < plainText.length(); i++) {
            caracter = plainText.charAt(i);
            if (caracter >= 'a' && caracter <= 'm') caracter += 13;
            else if (caracter >= 'A' && caracter <= 'M') caracter += 13;
            else if (caracter >= 'n' && caracter <= 'z') caracter -= 13;
            else if (caracter >= 'N' && caracter <= 'Z') caracter -= 13;

            cryptogram.append(caracter);
        }
        return cryptogram.toString();
    }

    @Override
    public String decrypt(String cryptogram) {
        return encrypt(cryptogram);
    }

    public static void main(String[] args) {
        Rot13 rot13 = new Rot13();
        String plainText = "Hola ñandu";
        String encrypted = rot13.encrypt(plainText);
        String decrypted = rot13.decrypt(encrypted);
        System.out.println("encrypted: " + encrypted);
        System.out.println("decrypted: " + decrypted);
    }
}
