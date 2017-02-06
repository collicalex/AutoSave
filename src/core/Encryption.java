package core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

import org.bouncycastle.crypto.generators.SCrypt;

//Tutorial: http://stackoverflow.com/a/5520786
//Tutorial: https://www.flexiprovider.de/examples/ExampleCrypt.html
//Why AES?: http://www.javamex.com/tutorials/cryptography/ciphers.shtml

//Scrypt parameters: http://stackoverflow.com/questions/11126315/what-are-optimal-scrypt-work-factors
public class Encryption {

	private SecretKeySpec _keySpecContent;
	private SecretKeySpec _keySpecFilename;
	private Cipher _cipherContent;
	private Cipher _cipherFname;
	
	public Encryption(String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String salt = "?,.;/!§!%ùµ*£$¨^+=°)àç_è-('\"é&²~#{[|`\\^@]}";
		_keySpecContent = generateSecretKeyContent(keyString, salt);
		_keySpecFilename = generateSecretKeyContent(keyString, new StringBuilder(salt).reverse().toString());
		
		try {
			_cipherContent = Cipher.getInstance("AES/GCM/NOPADDING", "BC");
			_cipherFname = Cipher.getInstance("AES/GCM/NOPADDING", "BC");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}

	}
	
	private SecretKeySpec generateSecretKeyContent(String keyString, String salt) throws NoSuchAlgorithmException {
        byte[] key = SCrypt.generate(keyString.getBytes(), salt.getBytes(), 2^20, 8, 1, 16); //16 bytes length as we want 128bit length key (8 bit per byte, 128/8=16)
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        return keySpec;
	}

	
	private AlgorithmParameterSpec getInitializationVector(byte[] iv) {
		return new GCMParameterSpec(iv.length, iv);
	}
	
	//---------------------------------------------------------------------------
	//-- File content encryption
	//-- It use GCM mode which need an Initialization Vector (IV) (stronger mode)
	//-- IV is purely random
	//---------------------------------------------------------------------------
	
	public OutputStream encrypt(OutputStream os) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] iv = new byte[128];
        new SecureRandom().nextBytes(iv);
		os.write(iv); //The IV can be saved clearly, it has no impact into the security, it just need to be unique and not predictable
		_cipherContent.init(Cipher.ENCRYPT_MODE, _keySpecContent, getInitializationVector(iv));
		return new CipherOutputStream(os, _cipherContent);
	}
	
	public InputStream decrypt(InputStream is) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
		byte[] iv = new byte[128];
		if (is.read(iv) != iv.length) { //just read back the IV from the content
			throw new IOException("Unable to retrieve IV vector from input stream");
		}
		_cipherContent.init(Cipher.DECRYPT_MODE, _keySpecContent, getInitializationVector(iv));
		return new CipherInputStream(is, _cipherContent);
	}
	
	//-------------------------------------------------------------------------
	//-- Filename encryption
	//-- It use GCM mode which need an Initialization Vector (IV) (stronger mode)
	//-- IV is file creation time (I suppose that 2 files cannot be create at the same millisecond time!
	//-------------------------------------------------------------------------
	
	private byte[] longToBytes(long l) {
	    byte[] result = new byte[8];
	    for (int i = 7; i >= 0; i--) {
	        result[i] = (byte)(l & 0xFF);
	        l >>= 8;
	    }
	    return result;
	}
	
	public String encrypt(String str, File original) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException  {
		byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
		
	    try {
	    	BasicFileAttributes attr = Files.readAttributes(original.toPath(), BasicFileAttributes.class);
	    	FileTime ft = attr.creationTime();
	    	IV = longToBytes(ft.toMillis());
	    } catch (IOException e) {
	    	System.out.println("oops error! " + e.getMessage());
	    }
        
	    byte[] iv = new byte[128];
	    int j = 0;
	    for (int i = 0; i < iv.length; ++i) {
	    	iv[i] = IV[j];
	    	if (++j == IV.length) {
	    		j = 0;
	    	}
	    }
	    
	    _cipherFname.init(Cipher.ENCRYPT_MODE, _keySpecFilename, getInitializationVector(iv));
	    byte[] crypted = _cipherFname.doFinal(str.getBytes());
	    
	    byte[] result = new byte[IV.length + crypted.length];
	    System.arraycopy(IV, 0, result, 0, IV.length);
	    System.arraycopy(crypted, 0, result, IV.length, crypted.length);
	    
	    return encodeOffset248(result);
	}
	
	public String decrypt(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		byte[] result = decodeOffset248(str);
		
		byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
		byte[] crypted = new byte[result.length - IV.length];
		
		System.arraycopy(result, 0, IV, 0, IV.length);
		System.arraycopy(result, IV.length, crypted, 0, crypted.length);
		
	    byte[] iv = new byte[128];
	    int j = 0;
	    for (int i = 0; i < iv.length; ++i) {
	    	iv[i] = IV[j];
	    	if (++j == IV.length) {
	    		j = 0;
	    	}
	    }		
		
		_cipherFname.init(Cipher.DECRYPT_MODE, _keySpecFilename, getInitializationVector(iv));
		return new String(_cipherFname.doFinal(crypted));
	}

	
	//--------------------------------------------------------
	//-- Byte encoding (used Offset248 instead of Base64)
	//-- see: https://github.com/diafygi/Offset248
	//--------------------------------------------------------

	public String encodeOffset248(byte[] input) {
	    StringBuffer result = new StringBuffer(input.length);
	    for (int i = 0; i < input.length; ++i) {
	    	int v248 = (char)(input[i]) + 248;
	    	result.appendCodePoint(v248);
	    }
	    return result.toString();
	}
	
	public byte[] decodeOffset248(String input) {
		byte[] result = new byte[input.codePointCount(0, input.length())];
		
		for(int cp, j = 0, i = 0; i < input.length(); i += Character.charCount(cp)) {
			cp = input.codePointAt(i);
			result[j++] = (byte)(cp - 248);
		}

		return result;
	}	
	
	
	//--------------------------------------------------------
	//-- String obfuscation
	//-- Base64 encoding + rotation
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
			return '/';
		} else if (v == 64) {
			return '=';
		} else {
			throw new IOException("Unknwon base 64 value '" + v + "'");
		}
	}
	
	public static String caesarCipherEncrypt(String plain) throws IOException {
		int offset = plain.length();
		for (int i = 0; i< plain.length(); ++i) {
			offset += (char)plain.charAt(i);
		}
		offset = offset % 65;
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
