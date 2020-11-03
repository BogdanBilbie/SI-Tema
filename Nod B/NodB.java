import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class NodB {

    private BufferedReader fromServer;
    private String modCriptare;
    protected static final String k3 = "bob10klm14ark862"; // cheia folosita in decriptarea mesajelor de la KM
    Cipher cipherDecriptare; // pentru decriptarea cheii primite de la KM
    IvParameterSpec iv ; // folosit pentru decriptarea datelor primite de la A
    String key;
    byte[] keyBytes = new byte[32];
    byte[] ivBytes = new byte[32];
    int numarPachetePrimite = 0;

    public NodB(BufferedReader input) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, IOException {
        this.fromServer = input;
        try{
            cipherDecriptare = Cipher.getInstance("AES/ECB/PKCS5Padding");
            DecriptareCheie();
        }
        catch(NoSuchAlgorithmException e){
            //handle the case of having no matching algorithm
        }
        catch(NoSuchPaddingException e){
            //handle the case of a padding problem
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


    }

    private void DecriptareCheie() throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        String recived = fromServer.readLine();
        String ivDecript;
        modCriptare = recived;
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(fromServer.readLine());
        while (stringBuilder.length() < 32){
                stringBuilder.append('\n');
                if(stringBuilder.length() < 32)
                    stringBuilder.append(fromServer.readLine());
        }

        cipherDecriptare.init(Cipher.DECRYPT_MODE, new SecretKeySpec(k3.getBytes(StandardCharsets.ISO_8859_1),"AES")); // folosit pentru decriptare cheie

        key = new String(stringBuilder);
        key = new String(cipherDecriptare.doFinal(key.getBytes(StandardCharsets.ISO_8859_1))); // obtinem cheia
        keyBytes = key.getBytes(StandardCharsets.ISO_8859_1);
        //Pregatim utilizarea ECB pentru decriptare
        cipherDecriptare.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes,"AES"));
        // Decriptam iv-ul folosit pentru criptarea primului bloc de date

        stringBuilder = new StringBuilder();
        stringBuilder.append(fromServer.readLine());
        while (stringBuilder.length() < 32){
            stringBuilder.append('\n');
            if(stringBuilder.length() < 32)
                stringBuilder.append(fromServer.readLine());
        }
        ivDecript = new String(stringBuilder);
        ivBytes = ivDecript.getBytes(StandardCharsets.ISO_8859_1);
        ivBytes = cipherDecriptare.doFinal(ivBytes);
        iv = new IvParameterSpec(ivBytes);

        if(modCriptare.equals("CBC")) CBC();
        else if(modCriptare.equals("OFB")) OFB();

    }

    private void CBC() throws IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        String recived;
        StringBuilder refacereCriptare;
        byte[] plainText;
        byte[] recivedBytes = new byte[32];
        while(!(recived = fromServer.readLine()).equals("Am terminat")) {
            refacereCriptare = new StringBuilder();
            refacereCriptare.append(recived);
            while (refacereCriptare.length() < 32) {
                refacereCriptare.append('\n');
                if(refacereCriptare.length() < 32)
                    refacereCriptare.append(fromServer.readLine());
            }
            byte[] initializationVector = iv.getIV();
            recived = new String(refacereCriptare);
            recivedBytes = recived.getBytes(StandardCharsets.ISO_8859_1);

            iv = new IvParameterSpec(recivedBytes);
            plainText = cipherDecriptare.doFinal(recivedBytes);
            for (int i = 0; i < plainText.length; i++) {
                plainText[i] = (byte) (plainText[i] ^ initializationVector[i]);
            }

            System.out.println(new String(plainText,StandardCharsets.ISO_8859_1));

            numarPachetePrimite++;
            if(numarPachetePrimite % 16 == 0){
                DecriptareCheie();
            }
        }
    }

    private void OFB() throws IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String recived;
        StringBuilder refacereCriptare;
        byte[] plainText;

        while(!(recived = fromServer.readLine()).equals("Am terminat")) {
            refacereCriptare = new StringBuilder();
            refacereCriptare.append(recived);
            while (refacereCriptare.length() < 16) {
                refacereCriptare.append('\n');
                if(refacereCriptare.length()<16)
                    refacereCriptare.append(fromServer.readLine());
            }
            byte[] initializationVector = iv.getIV();
            recived = new String(refacereCriptare);
            plainText = recived.getBytes(StandardCharsets.ISO_8859_1);
            cipherDecriptare.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key.getBytes(StandardCharsets.ISO_8859_1),"AES"));
            initializationVector = cipherDecriptare.doFinal(initializationVector);
            iv = new IvParameterSpec(initializationVector);
            for(int i = 0; i<plainText.length;i++){
                plainText[i] = (byte)(plainText[i]^initializationVector[i]);
            }
            System.out.println(new String(plainText,StandardCharsets.ISO_8859_1));

            numarPachetePrimite++;
            if(numarPachetePrimite % 16 == 0){
                DecriptareCheie();
            }
        }
    }

}
