package cipher.impl;

import cipher.ICipher;

public class ColumnTranspositionByPattern implements ICipher<byte[]> {
    private final int[] pattern;

    public ColumnTranspositionByPattern(int[] pattern) {
        this.pattern = pattern;
        if (!this.validate()) throw new IllegalArgumentException();
    }

    @Override
    public boolean validate() {
        int[] temp = new int[pattern.length];

        int i = 0;
        boolean isValid = true;

        while ((i < pattern.length) && isValid) {
            if ((pattern[i] < pattern.length) && (pattern[i] >= 0))
                temp[pattern[i]] = pattern[i];
            else
                isValid = false;
            i++;
        }
        return isValid;
    }

    @Override
    public byte[] encrypt(byte[] plainText) {
        int plainTextLength = plainText.length;
        int patternLength = pattern.length;

        byte[] cryptogram = new byte[plainTextLength];

        int k = 0;
        for (int value : pattern)
            for (int j = value; j < plainTextLength; j += patternLength)
                cryptogram[k++] = plainText[j];

        return cryptogram;
    }

    @Override
    public byte[] decrypt(byte[] cryptogram) {
        int cryptogramLength = cryptogram.length;
        int patternLength = pattern.length;

        byte[] plainText = new byte[cryptogramLength];
        int k = 0;

        for (int value : pattern)
            for (int j = value; j < cryptogramLength; j += patternLength) plainText[j] = cryptogram[k++];

        return plainText;
    }

    public static void main(String[] args) {
        ColumnTranspositionByPattern cipher = new ColumnTranspositionByPattern(new int[]{0, 2, 1});
        String plainText = "Hola ñandu";
        byte[] encrypted = cipher.encrypt(plainText.getBytes());
        byte[] decrypted = cipher.decrypt(encrypted);

        System.out.println("encrypted: " + new String(encrypted));
        System.out.println("decrypted: " + new String(decrypted));
    }
}
