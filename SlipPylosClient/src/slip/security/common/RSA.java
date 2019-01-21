package slip.security.common;

import javax.crypto.Cipher;

//import jdk.nashorn.internal.ir.debug.JSONWriter;
//import java.io.InputStream;
import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;


public class RSA {
	
	public static final int RSA_KEY_SIZE = 512; // pour avoir des chaînes courtes, mais il faudrait 2048+ pour que la sécurité soit réelle !!
	
	// Clef privée et publique RSA 2048bits, encodée en Base64
	// Sauvegarde sous forme de texte, pour des tests 
	public static final String STR_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXWa04dq+qjX02QVOfG4lpdliYDmTlYAcLfIgKl3QRluBmAAjxhuiVaa0kc69jU69MdcBLPBN2oeWZPLTBUcXeuG3MYV7XuWR91mxZ/BKvMhm1wHZthYYcRjQjUsYGK0aX6cDqaXD+DFhL74xBcAEvcEy1J8+ZIjOFfsObr+ugf+AU8LmDHr9/BUdeLjKMUUPwAHxDb8HaRL2moT7dnH3Id7MUq4zeZNDIm4lNM7M1lWXIOWG++cTqorXTn3WLRTezBLEfcuPZQu36cHt4Ar4krVLznvn3E4KtL9+1bMCc8CfTuMfGbUlbgR3LQejsZfsyO9fYwrSHe7oFcqLhCX7FAgMBAAECggEAZOk1xV/c4CpWQcZsqrkBdX+isj9mpkjQaaguTGGO0et20otTazY3/OboulUnq2Iwjxozi/YSRBbNrs369qo+87CkBJEnW04Q4pYEyDp5erY8ziH01DEiqddlC+g0gAh6mO8R4TlMTRaOCJM/QKIdKDQH8QEKOV/EWk2avkMdJ3UTUTt/wEWGeta6p3dHms8x4jx57cw8ADVsI/8PxWeQAg84d1sEVmM/Kue9YBfER2pYigNWLv8mI1GktxbXg7gRNlSXsi7Veh9hkOsCTxeiOm5HlQ54qaUWGGvXbrn1tSdrWYwnKHRxdgRe0MDYkIMOASA7zl1nP5Wr3oVnIujkQQKBgQDauKFRdw9vkOlNEP9MDRCglTJL8y3JpWBFTVTKMOjVilscExfd80Vzp8tAZe2dQDIcj1aCzXetftue8FyZVBGi2leOHZ13GsXuMIiOs+SKlVHlpTkAa0BLcN1CS3tGdTg/HmtGogUDb/x7tY9bP9s3RZRsxtDUumXfFQID1aCvDwKBgQCxJXrlNn4SV3zfCL4jajcYnP6UjbbN240gjeoCGZVBEucY46dkw1U7HKYNzV8undgVo/W2D0kpf4LM+Hjcn1j0SBjymS7mdUAAKXITm8CMgXsZTAT3/7L4ogSIEsIc+/WWBm+3A8/jzuTH1NUWnjnIrjwDB0xkjXR2zUfP1Wp06wKBgQC32w+v3TdafyO+JpWUJj9d3UyET4yjvqJoXxLxS5/NMRpZRSuA5Sfyio/uOEA/OWFmZI9CMNlzO/n9ZutP8D1K+eMzkW12W0kaai3AVzO70r0fH34E/iLzx5IWUkDz/0Eivb1LiJJSS2afzwUMnWb21URpE88jovRTS+N/uanyvQKBgAWFsJYSo814krj1MdAy0HLg+gKxhEBYlsasBd5447E6oJ+jASXf/PkxxG9rtoriesj56n/5bANyKSawnDvsb751vOlbIx4mC4+1uwuncFIw+yBnwUPl4bNkgZWoWArFQ/ugSb0/zixA19ru5JGm7xA1dkN158i+rCHD9nghJAdvAoGBALToOKm6MFdTB2VtREQd2TuaSgam2cIkgyIUBOcWdnwKJ4Z61qERq9s0QSc4oCFJb57VaUzJxB6n0YIwjXMkcczqO1wOek3m16KWYYu2L4iXzpc5pYJAQPnIGQS0Foeb9Viz4sdvtInyVrScA4bIq4OEqFE6BfvPQqM/NVCMPsyU";
	public static final String STR_PUBLIC_KEY  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl1mtOHavqo19NkFTnxuJaXZYmA5k5WAHC3yICpd0EZbgZgAI8YbolWmtJHOvY1OvTHXASzwTdqHlmTy0wVHF3rhtzGFe17lkfdZsWfwSrzIZtcB2bYWGHEY0I1LGBitGl+nA6mlw/gxYS++MQXABL3BMtSfPmSIzhX7Dm6/roH/gFPC5gx6/fwVHXi4yjFFD8AB8Q2/B2kS9pqE+3Zx9yHezFKuM3mTQyJuJTTOzNZVlyDlhvvnE6qK10591i0U3swSxH3Lj2ULt+nB7eAK+JK1S85759xOCrS/ftWzAnPAn07jHxm1JW4Edy0Ho7GX7MjvX2MK0h3u6BXKi4Ql+xQIDAQAB";
	
	private static PrivateKey PRIVATE_KEY = RSAKey.loadPrivateKey(STR_PRIVATE_KEY);
	private static PublicKey  PUBLIC_KEY = RSAKey.loadPublicKey(STR_PUBLIC_KEY);
	
	/** Charger la clef publique
	 * @param strPublicKey  Clef publique encodée en Base64
	 * @return  vrai si la clef a bien été chargée
	 */
	public static boolean setRSAPublicKey(String strPublicKey) {
		PUBLIC_KEY = RSAKey.loadPublicKey(strPublicKey);
		return (PUBLIC_KEY != null);
	}
	/** Charger la clef privée
	 * @param strPrivateKey Clef privée encodée en Base64
	 * @return  vrai si la clef a bien étét chargée
	 */
	public static boolean setRSAPrivateKey(String strPrivateKey) {
		PRIVATE_KEY = RSAKey.loadPrivateKey(strPrivateKey);
		return (PRIVATE_KEY != null);
	}
	
	/** Générer un couple clef privée-publique
	 * @return
	 * @throws Exception
	 */
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_SIZE, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
    
    /** Crypter un tableau d'octets via la clef publique 
     * @param clearByteArray
     * @return
     * @throws Exception
     */
    public static byte[] encryptByteArray(byte[] clearByteArray) throws Exception {
    	if (PUBLIC_KEY == null) return null;
        Cipher cipherEncrypt = Cipher.getInstance("RSA");
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
        byte[] cryptedArray = cipherEncrypt.doFinal(clearByteArray);
        return cryptedArray;
    }

    /**Décrypter un tableau de'octets
     * @param cryptedArray
     * @return
     * @throws Exception
     */
    public static byte[] decryptByteArray(byte[] cryptedArray) throws Exception {
    	if (PRIVATE_KEY == null) return null;
        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
        return decriptCipher.doFinal(cryptedArray);
    }
    
    /*
    public static byte[] decryptByteArray_withPublicKey(byte[] cryptedArray) {
    	try {
	    	if (PRIVATE_KEY == null) return null;
	        Cipher decriptCipher = Cipher.getInstance("RSA");
	        decriptCipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
	        return decriptCipher.doFinal(cryptedArray);
    	} catch (Exception e) {
    		return null;
    	}
    }*/
    
    /*
    public static byte[] encryptByteArray(byte[] clearByteArray) throws Exception {
        Cipher cipherEncrypt = Cipher.getInstance("RSA");
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
        byte[] cipherText = cipherEncrypt.doFinal(plainText.getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }
    */
    
    // Appliquer un hash sha256
  	public static String sha256(String input) throws RuntimeException {
  		try {
  			return sha256(input.getBytes("UTF-8"));
  		} catch (Exception e) {
  			throw new RuntimeException(e); // Erreur critique
  		}
  	}	

  	public static String sha256(byte[] inputByteArray){		
  		try {
  			MessageDigest digest = MessageDigest.getInstance("SHA-256");	        
  			//Applies sha256 to our input, 
  			byte[] hash = digest.digest(inputByteArray);	        
  			StringBuffer hexString = new StringBuffer(); // Contiendra le hash en hexidecimal
  			for (int i = 0; i < hash.length; i++) {
  				String hex = Integer.toHexString(0xff & hash[i]);
  				if(hex.length() == 1) hexString.append('0');
  				hexString.append(hex);
  			}
  			return hexString.toString();
  		}
  		catch(Exception e) {
  			throw new RuntimeException(e); // Erreur critique
  		}
  	}
    

    public static String sign(byte[] inputArray, PrivateKey privateKey) {
    	try {
	        Signature privateSignature = Signature.getInstance("SHA256withRSA");
	        privateSignature.initSign(privateKey);
	        privateSignature.update(inputArray);
	        byte[] signature = privateSignature.sign();
	        return Base64.getEncoder().encodeToString(signature);
    	} catch (Exception e) {
    		return null;
    	}
    }

    public static boolean check(byte[] inputArray, String signature, PublicKey publicKey) {
    	try {
    		if (publicKey == null) return false;
	        Signature publicSignature = Signature.getInstance("SHA256withRSA");
	        publicSignature.initVerify(publicKey);
	        publicSignature.update(inputArray);
	        byte[] signatureBytes = Base64.getDecoder().decode(signature);
	        return publicSignature.verify(signatureBytes);
    	} catch (Exception e) {
    		return false;
    	}
    }
    

    public static String sign(String plainText, String privateKeyAsString) {
    	return sign(plainText.getBytes(UTF_8), privateKeyAsString);
    }

    public static String sign(String plainText, PrivateKey privateKey) {
    	return sign(plainText.getBytes(UTF_8), privateKey);
    	
    	/*Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);*/
    }
    
    public static String sign(byte[] inputArray, String privateKeyAsString) {
    	if (privateKeyAsString == null || inputArray == null) return null;
    	PrivateKey privateKey = RSAKey.loadPrivateKey(privateKeyAsString);
    	if (privateKey == null) return null;
    	return sign(inputArray, privateKey);
    }

    public static boolean check(String plainText, String signature, PublicKey publicKey) {
    	return check(plainText.getBytes(UTF_8), signature, publicKey);
    }

    public static boolean check(String plainText, String signature, String publicKeyAsString) {
    	return check(plainText.getBytes(UTF_8), signature, publicKeyAsString);
    }

    public static boolean check(byte[] inputArray, String signature, String publicKeyAsString) {
    	if (inputArray == null || signature == null || publicKeyAsString == null) return false;
    	PublicKey publicKey = RSAKey.loadPublicKey(publicKeyAsString);
    	if (publicKey == null) return false;
    	return check(inputArray, signature, publicKey);
    }
    


    
    
    private static void generateKeyAndSave() {
    	
    }
    

    public static void main(String... argv) throws Exception {
    	
    }

    public static void mainnne(String... argv) throws Exception {
        //First generate a public/private key pair
    	System.out.println("Génération...");
    	
        KeyPair pair = generateRSAKeyPair();
        
        
        //pair.getPrivate();
    	System.out.println("Génération OK");

    	System.out.println("Vérification des clefs...");
        PrivateKey myPrivateKey = pair.getPrivate();
        PublicKey myPublicKey = pair.getPublic();
        
        String privStr = RSAKey.saveKey(myPrivateKey);
        PrivateKey loadedPrivateKey = RSAKey.loadPrivateKey(privStr);
        System.out.println("myPrivateKey bien lue : " + myPrivateKey.equals(loadedPrivateKey) + " len = " + privStr.length());
        
        String pubStr = RSAKey.saveKey(myPublicKey);
        PublicKey loadedPublicKey = RSAKey.loadPublicKey(pubStr);
        System.out.println("myPublicKey bien lue : " + myPublicKey.equals(loadedPublicKey) + " len = " + pubStr.length());

		String newLineChar = System.getProperty("line.separator");
        
        String myString = "--- PRIVATE KEY ---" + newLineChar + privStr + newLineChar + "--- PUBLIC KEY ---" + newLineChar + pubStr;
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        

        System.out.println("myString =  " + myString);
        //System.out.println("privStr2 =  " + privStr2);
        
        
        //KeyPair pair = getKeyPairFromKeyStore();

        //Our secret message
        String message = "the answer to life the universe and everything";
        
        
        
        /*
        //Encrypt the message
    	System.out.println("Encryption du message...");
        String cipherText = encrypt(message, pair.getPublic());
        System.out.println("cipherText : " + cipherText);
    	System.out.println("Encryption du message OK !");

    	System.out.println("Decryptage du message...");
        //Now decrypt it
        String decipheredMessage = decrypt(cipherText, pair.getPrivate());
    	System.out.println("Decryptage du message OK !");

        System.out.println(decipheredMessage);*/

        //Let's sign our message
        String signature = sign("foobar", pair.getPrivate());

        //Let's check the signature
        boolean isCorrect = check("foobar", signature, pair.getPublic());
        System.out.println("Signature correct: " + isCorrect);
    }
}
