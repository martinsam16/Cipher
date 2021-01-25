package cipher;

public interface ICipher<T> {
    boolean validate();
    T encrypt(T plainText);
    T decrypt(T cryptogram);
}
