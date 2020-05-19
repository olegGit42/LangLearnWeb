package com.oleapp.colibriweb.service.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";

	public static void encrypt(String key, File inputFile, File outputFile) throws CryptoException {
		doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
	}

	public static void decrypt(String key, File inputFile, File outputFile) throws CryptoException {
		doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
	}

	public static void encrypt(String key, String inputString, File outputFile) throws CryptoException {
		doCrypto(Cipher.ENCRYPT_MODE, key, inputString, outputFile);
	}

	public static void decrypt(String key, String inputString, File outputFile) throws CryptoException {
		doCrypto(Cipher.DECRYPT_MODE, key, inputString, outputFile);
	}

	public static String encrypt(String key, File inputFile) throws CryptoException {
		return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile);
	}

	public static String decrypt(String key, File inputFile) throws CryptoException {
		return doCrypto(Cipher.DECRYPT_MODE, key, inputFile);
	}

	public static String encrypt(String key, String inputString) throws CryptoException {
		return doCrypto(Cipher.ENCRYPT_MODE, key, inputString);
	}

	public static String decrypt(String key, String inputString) throws CryptoException {
		return doCrypto(Cipher.DECRYPT_MODE, key, inputString);
	}

	private static void doCrypto(int cipherMode, String key, File inputFile, File outputFile) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);

			FileInputStream inputStream = new FileInputStream(inputFile);
			byte[] inputBytes = new byte[(int) inputFile.length()];
			inputStream.read(inputBytes);

			byte[] outputBytes = cipher.doFinal(inputBytes);

			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(outputBytes);

			inputStream.close();
			outputStream.close();

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException | IOException ex) {
			throw new CryptoException("Error encrypting/decrypting file", ex);
		}
	}

	private static void doCrypto(int cipherMode, String key, String inputString, File outputFile) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);

			byte[] inputBytes = inputString.getBytes(StandardCharsets.UTF_8);
			byte[] outputBytes = cipher.doFinal(inputBytes);

			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(outputBytes);

			outputStream.close();

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException | IOException ex) {
			throw new CryptoException("Error encrypting/decrypting file", ex);
		}
	}

	private static String doCrypto(int cipherMode, String key, String inputString) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);

			byte[] inputBytes = inputString.getBytes(StandardCharsets.UTF_8);
			byte[] outputBytes;

			if (cipherMode == Cipher.ENCRYPT_MODE) {
				outputBytes = cipher.doFinal(inputBytes);
				outputBytes = Base64.getEncoder().encode(outputBytes);
			} else {
				inputBytes = Base64.getDecoder().decode(inputBytes);
				outputBytes = cipher.doFinal(inputBytes);
			}

			return new String(outputBytes, StandardCharsets.UTF_8);

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException ex) {
			throw new CryptoException("Error encrypting/decrypting file", ex);
		}
	}

	private static String doCrypto(int cipherMode, String key, File inputFile) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);

			FileInputStream inputStream = new FileInputStream(inputFile);
			byte[] inputBytes = new byte[(int) inputFile.length()];
			inputStream.read(inputBytes);
			inputStream.close();

			byte[] outputBytes = cipher.doFinal(inputBytes);

			return new String(outputBytes, StandardCharsets.UTF_8);

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException | IOException ex) {
			throw new CryptoException("Error encrypting/decrypting file", ex);
		}
	}
}
