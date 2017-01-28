package core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//Tutorial: http://stackoverflow.com/a/5520786
//Tutorial: https://www.flexiprovider.de/examples/ExampleCrypt.html
//Why AES?: http://www.javamex.com/tutorials/cryptography/ciphers.shtml
public class Encryption {

	private SecretKeySpec _keySpec;
	private Cipher _cipherContent;
	private Cipher _cipherFname;
	
	public Encryption(String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		_keySpec = generateSecretKeyContent(keyString);
		
		try {
			_cipherContent = Cipher.getInstance("AES/GCM/NOPADDING", "BC");
			_cipherFname = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}

	}
	
	private SecretKeySpec generateSecretKeyContent(String keyString) throws NoSuchAlgorithmException {
		//Hash keyString with SHA-256 and crop the output to 128-bit for key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(keyString.getBytes());
        byte[] key = new byte[16]; //8bit par byte, 128bits --> 16 bytes
        System.arraycopy(digest.digest(), 0, key, 0, key.length);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return keySpec;
	}

	
	private AlgorithmParameterSpec getInitializationVector(byte[] iv) {
		return new GCMParameterSpec(iv.length, iv);
	}
	
	//---------------------------------------------------------------------------
	//-- File content encryption
	//-- It use GCM mode which need an Initialization Vector (IV) (stronger mode)
	//---------------------------------------------------------------------------
	
	public OutputStream encrypt(OutputStream os) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] iv = new byte[128];
        new SecureRandom().nextBytes(iv);
		os.write(iv); //The IV can be saved clearly, it has no impact into the security, it just need to be unique and not predictable
		_cipherContent.init(Cipher.ENCRYPT_MODE, _keySpec, getInitializationVector(iv));
		return new CipherOutputStream(os, _cipherContent);
	}
	
	public InputStream decrypt(InputStream is) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
		byte[] iv = new byte[128];
		if (is.read(iv) != iv.length) { //just read back the IV from the content
			throw new IOException("Unable to retrieve IV vector from input stream");
		}
		_cipherContent.init(Cipher.DECRYPT_MODE, _keySpec, getInitializationVector(iv));
		return new CipherInputStream(is, _cipherContent);
	}
	
	//-------------------------------------------------------------------------
	//-- Filename encryption
	//-- It use ECB mode which does not need an Initialization Vector (IV)
	//-------------------------------------------------------------------------
	
	public String encrypt(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException  {
		_cipherFname.init(Cipher.ENCRYPT_MODE, _keySpec);
        return caesarCipherEncrypt(_cipherFname.doFinal(str.getBytes()));
	}
	
	public String decrypt(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		_cipherFname.init(Cipher.DECRYPT_MODE, _keySpec);
		return new String(_cipherFname.doFinal(caesarCipherDecrypt(str)));
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
	
	public static String caesarCipherEncrypt2(String plain) throws IOException {
		return caesarCipherEncrypt(plain.getBytes());
	}
	
	public static String caesarCipherDecrypt2(String secret) throws IOException {
		return new String(caesarCipherDecrypt(secret));
	}
	
	public static String caesarCipherEncrypt(byte[] plain) throws IOException {
		int offset = plain.length;
		for (int i = 0; i< plain.length; ++i) {
			offset += (char)plain[i];
		}
		offset = offset % 65;
		String b64encoded = Base64.getEncoder().encodeToString(plain);
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
	
	public static byte[] caesarCipherDecrypt(String secret) throws IOException {
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
		return Base64.getDecoder().decode(reversed);
	}	
}
