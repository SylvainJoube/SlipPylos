package slip.security.common;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

//import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.Base64;

public class RSAKey {
	
	private static String encodeToHex(byte[] keyAsArray) {
		String keyAsString = "";
		String newLineChar = System.getProperty("line.separator");
	    int lineCharCount = 0;
		 for (int position = 0; position < keyAsArray.length; position++) {
		    	int readByte = (int)keyAsArray[position] - Byte.MIN_VALUE; // MIN_VALUE est négatif !
		    	
		    	String currentHexNumber = Integer.toHexString(readByte);
		    	if (currentHexNumber.length() == 1) currentHexNumber = "0" + currentHexNumber;
		    	
		    	lineCharCount++;
		    	if (lineCharCount >= 32) {
		    		lineCharCount = 0;
		    		keyAsString += newLineChar;
		    	}
		    	keyAsString += currentHexNumber;
		    }
		return keyAsString;
	}
	
	
	
	
	
	/**
	 * Sauvegarde de la clef RSA.
	 * La clef publique est encodée via X509EncodedKeySpec.
	 * La clef privée est encodée via PKCS8EncodedKeySpec.
	 * @param rsaKey  Clef publique ou privée.
	 * @return  Une chaine de caractères décrivant totalement la clef (privée ou publique).
	 */
	public static String saveKey(Key rsaKey) {
		if (rsaKey == null) return "";
	    byte[] keyAsArray = rsaKey.getEncoded();
	    String key64 = Base64.getEncoder().encodeToString(keyAsArray);
	    //String keyHex = encodeToHex(keyAsArray);
	    //System.out.println("key64len = " + Integer.toString(key64.length()));
	    //System.out.println("keyHex = " + Integer.toString(keyHex.length()));
	    //Base64.getDecoder().decode(arg0);
	    return key64;
	}

	public static PrivateKey loadPrivateKey(String privateKeyString) {
		// La clef privée est enregistrée via PKCS8EncodedKeySpec
		byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
		try {
		    KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
		    PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		    return privateKey;
		} catch (Exception e) {
			//String key64 = Base64.getEncoder().encodeToString(privateKeyBytes);
		}
		return null;
	}

	public static PublicKey loadPublicKey(String publicKeyString) { // byte[] privateKeyBytes
		// La clef privée est enregistrée via PKCS8EncodedKeySpec
		byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
		try {
		    KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
		    PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		    return publicKey;
		} catch (Exception e) {
			//String key64 = Base64.getEncoder().encodeToString(privateKeyBytes);
		}
		return null;
	}
	
	
	/*
	public static PrivateKey loadPrivateKey(String strPrivateKey) {
		
		try {
		    byte[] privateKeyBytes;
		    KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
		    PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		} catch (Exception e) {
			
		}
		
		return null;
	}
	
	
	public static PublicKey loadPublicKey(String strPublicKey) {
		

	    byte[] privateKeyBytes;
	    byte[] publicKeyBytes;
	    KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
	    PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
	    PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		
		return null;
	}
	*/
	
	/*
	public void fct() {
		
		
		PrivateKey myPrivateKey;// = pair.getPrivate();
	    byte[] myPrivateKeyAsArray = myPrivateKey.getEncoded();
	    String keyAsString = "";
	    
	    System.out.println("myPrivateKeyAsArray.length = " + myPrivateKeyAsArray.length + " String.valueOf(2055)" + String.valueOf(2055));
	    int intFromByte;
	    String newLine = System.getProperty("line.separator");
	    System.out.println("ceci est" + newLine + "une nouvelle" + newLine + "ligne.");
	    
	    int lineCharCount = 0;
	    if (myPrivateKeyAsArray != null)
	    for (int ind = 0; (ind < myPrivateKeyAsArray.length) && (ind < 8000); ind++) {
	    	int readByte = (int)myPrivateKeyAsArray[ind] - Byte.MIN_VALUE; // MIN_VALUE est négatif !
	    	//String.valueOf(readByte, 16);
	    	
	    	//Integer.valueOf(readByte, 16);
	    	String currentNumber = Integer.toHexString(readByte);
	    	if (currentNumber.length() == 1) currentNumber = "0" + currentNumber;
	    	
	    	lineCharCount++;
	    	if (lineCharCount >= 10) {
	    		lineCharCount = 0;
	    		keyAsString += newLine;
	    	}
	    	
	    	keyAsString += currentNumber + " ";
	    	//System.out.print("" + readByte + " ");
	    	//System.out.flush();
	    	
	    }
	    
	    System.out.println("privateKey =  " + keyAsString);
	}*/
    
}
