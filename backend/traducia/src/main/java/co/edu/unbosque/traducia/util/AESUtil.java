package co.edu.unbosque.traducia.util;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Clase utilitaria que proporciona métodos para el cifrado, descifrado y
 * generación de hashes de cadenas de texto.
 * <p>
 * Utiliza el algoritmo AES en modo GCM para proteger información mediante
 * cifrado simétrico y la biblioteca Apache Commons Codec para generar
 * resúmenes criptográficos utilizando MD5 y SHA.
 * </p>
 * <p>
 * Permite realizar operaciones de cifrado y descifrado utilizando una llave
 * y un vector de inicialización (IV) proporcionados directamente o mediante
 * los valores de configuración de la aplicación.
 * </p>
 *
 * @author
 * @version 1.0
 * @see Cipher
 * @see GCMParameterSpec
 * @see DigestUtils
 */
public class AESUtil {
	
	/**
     * Algoritmo de cifrado simétrico utilizado por la clase.
     */
	private final static String ALGORITMO = "AES";
	
	/**
     * Transformación criptográfica utilizada para las operaciones de cifrado
     * y descifrado, correspondiente a AES en modo GCM sin relleno.
     */
	private final static String TIPOCIFRADO = "AES/GCM/NoPadding";
    
	/**
     * Cifra una cadena de texto utilizando AES en modo GCM y codifica
     * el resultado en Base64.
     *
     * @param llave clave secreta utilizada para realizar el cifrado.
     * @param iv vector de inicialización utilizado durante la operación.
     * @param texto contenido original que se desea cifrar.
     * @return contenido cifrado y codificado en Base64.
     */
	public static String encrypt(String llave, String iv, String texto) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPOCIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(texto.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		return new String(encodeBase64(encrypted));
	}
	
	/**
     * Descifra una cadena previamente cifrada mediante AES en modo GCM,
     * utilizando la llave y el vector de inicialización correspondientes.
     *
     * @param llave clave secreta utilizada para realizar el descifrado.
     * @param iv vector de inicialización utilizado durante la operación.
     * @param encrypted contenido cifrado y codificado en Base64.
     * @return contenido original descifrado o una cadena vacía si ocurre
     *         un error durante la operación de descifrado.
     */
	public static String decrypt(String llave, String iv, String encrypted) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPOCIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {

			e.printStackTrace();
		}

		byte[] enc = decodeBase64(encrypted);
		byte[] decrypted = null;
		try {
			decrypted = cipher.doFinal(enc);
			return new String(decrypted);
		} catch (IllegalBlockSizeException | BadPaddingException e) {

			e.printStackTrace();
		}
		return "";

	}
	
	 /**
     * Descifra una cadena utilizando la llave y el vector de inicialización
     * definidos en la configuración de la aplicación.
     *
     * @param encrypted contenido cifrado y codificado en Base64.
     * @return contenido original descifrado o una cadena vacía si ocurre
     *         un error durante la operación.
     */
	public static String decrypt(String encrypted) {
		String iv = "${crypt.iv}";
		String key = "${crypt.key}";
		return decrypt(key, iv, encrypted);
	}
	
	/**
     * Cifra una cadena utilizando la llave y el vector de inicialización
     * definidos en la configuración de la aplicación.
     *
     * @param plainText contenido original que se desea cifrar.
     * @return contenido cifrado y codificado en Base64.
     */
	public static String encrypt(String plainText) {
		String iv = "${crypt.iv}";
		String key = "${crypt.key}";
		return encrypt(key, iv, plainText);
	}
	
	/**
     * Genera el hash MD5 de una cadena de texto.
     *
     * @param content contenido del que se desea generar el hash.
     * @return resumen criptográfico MD5 representado en formato hexadecimal.
     */
	public static String hashingToMD5(String content) {
		return  DigestUtils.md5Hex(content);
	}
	
	/**
     * Genera el hash SHA-1 de una cadena de texto.
     *
     * @param content contenido del que se desea generar el hash.
     * @return resumen criptográfico SHA-1 representado en formato hexadecimal.
     */
	public static String hashingToSHA1(String content) {
		return DigestUtils.sha1Hex(content);
	}
	
	/**
     * Genera el hash SHA-256 de una cadena de texto.
     *
     * @param content contenido del que se desea generar el hash.
     * @return resumen criptográfico SHA-256 representado en formato hexadecimal.
     */
	public static String hashingToSHA256(String content) {
		return DigestUtils.sha256Hex(content);
	}
	
	/**
     * Genera el hash SHA-384 de una cadena de texto.
     *
     * @param content contenido del que se desea generar el hash.
     * @return resumen criptográfico SHA-384 representado en formato hexadecimal.
     */
	public static String hashingToSHA384(String content) {
		return DigestUtils.sha384Hex(content);
	}
	
	/**
     * Genera el hash SHA-512 de una cadena de texto.
     *
     * @param content contenido del que se desea generar el hash.
     * @return resumen criptográfico SHA-512 representado en formato hexadecimal.
     */
	public static String hashingToSHA512(String content) {
		return DigestUtils.sha512Hex(content);
	}

}

