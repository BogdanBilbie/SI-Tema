import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KM {
    protected String k1 = "bob10klm14ark862"; // pentru CBC
    protected String k2 = "bob10klm14ark862"; // pentru OFB
    protected String k3 = "bob10klm14ark862"; // pentru AES
    protected byte[] generat = new byte[16];
    Cipher cipher ;

    SecureRandom secureRandom = new SecureRandom(); // folosit pentru generarea cheilor k1 si k2

    public KM() throws InvalidKeyException, UnsupportedEncodingException {
        try{
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        }
        catch(NoSuchAlgorithmException e){
            //handle the case of having no matching algorithm
        }
        catch(NoSuchPaddingException e){
            //handle the case of a padding problem
        }
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k3.getBytes(StandardCharsets.UTF_8),"AES"));
    }

    public byte[] getKey(String s) throws BadPaddingException, IllegalBlockSizeException {
        k1 = " ";
        if(s.equals("CBC")){
            while(k1.length()!= 16){
                secureRandom.nextBytes(generat);
                k1 = new String(generat);
            }
            return cipher.doFinal(k1.getBytes(StandardCharsets.ISO_8859_1));
        }
        if(s.equals("OFB")){
            k2 = " ";
            while(k2.length()!= 16){
                secureRandom.nextBytes(generat);
                k2 = new String(generat);
            }
            return cipher.doFinal(k2.getBytes(StandardCharsets.ISO_8859_1));
        }
        return null;
    }


}
