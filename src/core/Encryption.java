package core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//Tutorial: http://stackoverflow.com/a/5520786
//Tutorial: https://www.flexiprovider.de/examples/ExampleCrypt.html
//Why AES?: http://www.javamex.com/tutorials/cryptography/ciphers.shtml
public class Encryption {

	private SecretKeySpec _keySpec;
	private Cipher _cipher;
	
	public Encryption(String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException {
		_keySpec = generateSecretKey(keyString);
		_cipher = Cipher.getInstance("AES/GCM/NOPADDING");
	}
	
	private SecretKeySpec generateSecretKey(String keyString) throws NoSuchAlgorithmException {
		//TODO add salt?
		//Hash keyString with SHA-256 and crop the output to 128-bit for key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(keyString.getBytes());
        byte[] key = new byte[16]; //8bit par byte, 16 bytes -> 128bits
        System.arraycopy(digest.digest(), 0, key, 0, key.length);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return keySpec;
	}
	
	private AlgorithmParameterSpec getInitializationVector(byte[] iv) {
		return new GCMParameterSpec(iv.length, iv);
	}
	
	public OutputStream encrypt(OutputStream os) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] iv = new byte[128];
        new SecureRandom().nextBytes(iv);
		os.write(iv);
		_cipher.init(Cipher.ENCRYPT_MODE, _keySpec, getInitializationVector(iv));
		return new CipherOutputStream(os, _cipher);
	}
	
	public InputStream decrypt(InputStream is) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
		byte[] iv = new byte[128];
		if (is.read(iv) != iv.length) {
			throw new IOException("Unable to retrieve IV vector from input stream");
		}
		_cipher.init(Cipher.DECRYPT_MODE, _keySpec, getInitializationVector(iv));
		return new CipherInputStream(is, _cipher);
	}
	
	//--------------------------------------------------------
	
	private static int getB64Value(char c) throws IOException {
		if ((c >= 'A') && (c <= 'Z')) {
			return (c - 'A') + 0;
		} else if ((c >= 'a') && (c <= 'z')) {
			return (c - 'a') + 26;
		} else if ((c >= '0') && (c <= '9')) {
			return (c - '0') + 52;
		} else if (c == '+') {
			return 62;
		} else if (c == '/') {
			return 63;
		} else if (c == '=') {
			return 64;
		} else {
			throw new IOException("Unknwon base 64 char '" + c + "'");
		}
	}
	
	private static char getB64Char(int v) throws IOException {
		if ((v >= 0) && (v <= 25)) {
			return (char) ('A' + (v-0));
		} else if ((v >= 26) && (v <= 51)) {
			return (char) ('a' + (v-26));
		} else if ((v >= 52) && (v <= 61)) {
			return (char) ('0' + (v-52));
		} else if (v == 62) {
			return '+';
		} else if (v == 63) {
			return '/'; // must be '/'
		} else if (v == 64) {
			return '=';
		} else {
			throw new IOException("Unknwon base 64 value '" + v + "'");
		}
	}
	
	public static String caesarCipherEncrypt(String plain) throws IOException {
		int offset = (plain.length() % 65);
		String b64encoded = Base64.getEncoder().encodeToString(plain.getBytes());
		String reverse = new StringBuffer(b64encoded).reverse().toString();
		StringBuilder tmp = new StringBuilder();
		tmp.append(getB64Char(offset));
		for (int i = 0; i < reverse.length(); ++i) {
			int b64 = getB64Value(reverse.charAt(i));
			b64 = (b64 + offset) % 65;
			tmp.append(getB64Char(b64));
		}
		return tmp.toString().replaceAll("/", "é"); //maybe encode all b64 by string replacement?
	}
	
	public static String caesarCipherDecrypt(String secret) throws IOException {
		secret = secret.replaceAll("é", "/"); //maybe decode all b64 by string replacement?
		int offset = getB64Value(secret.charAt(0));
		offset = 65 - (offset%65);
		StringBuilder tmp = new StringBuilder();
		for (int i = 1; i < secret.length(); ++i) {
			int b64 = getB64Value(secret.charAt(i));
			b64 = (b64 + offset) % 65;
			tmp.append(getB64Char(b64));
		}
		String reversed = new StringBuffer(tmp.toString()).reverse().toString();
		reversed = reversed.replaceAll("é", "/");
		return new String(Base64.getDecoder().decode(reversed));
	}	
}
