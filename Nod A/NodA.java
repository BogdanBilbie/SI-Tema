import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 *
 * KM perfect functional in momentul de fata dar fara refresh la chei
 *

 *

 *

 *
 * De asemenea trebuie sa fac refresh la chei probabil nu in constructor
 *
 * Trebuie sa fac arhitectura de server-client
 *
 **/

public class NodA {

    private String path ="C:\\NodA\\src\\fisier.txt";
    private File file = new File(path);
    private FileReader fileReader = new FileReader(file);
    private BufferedReader bufferedReader;
    private int numarPacheteTrimise = 0;
    private boolean canRead = true;

    // Scrierea/Citirea datelor primite de la server
    private BufferedReader fromServer;
    private PrintWriter toServer;

    private String modCriptare = " "; // modurile de criptare

    private String key; //aici vom memora cheia primita de la KM
    private byte[] keyBytes = new byte[32];
    protected static final String k3 = "bob10klm14ark862"; // cheia folosita in decriptarea mesajelor de la KM

    Cipher cipherDecriptare; // pentru decriptarea cheii primite de la KM
    Cipher cipherCriptare; // pentru criptare

    IvParameterSpec iv ; // folosit pentru criptarea datelor citite din fiser


    public NodA(BufferedReader input, PrintWriter output) throws IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException {
        /** Initializare scriere/citire date de la server */
        this.fromServer = input;
        this.toServer = output;

        /** Deschidrea fisierului care trebuie criptat */
        bufferedReader = new BufferedReader(fileReader);

        try{
            cipherDecriptare = Cipher.getInstance("AES/ECB/PKCS5Padding");
        }
        catch(NoSuchAlgorithmException e){
            //handle the case of having no matching algorithm
        }
        catch(NoSuchPaddingException e){
            //handle the case of a padding problem
        }

        cipherDecriptare.init(Cipher.DECRYPT_MODE, new SecretKeySpec(k3.getBytes(StandardCharsets.UTF_8),"AES"));


        try{
            cipherCriptare = Cipher.getInstance("AES/ECB/PKCS5Padding");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e){
        }

        getKey();

    }

    public void getKey() throws BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        String cheieDeLaServer;
        StringBuilder stringBuilder = new StringBuilder();
        byte[] parametruIV = new byte[16];
        SecureRandom secureRandom = new SecureRandom();

        System.out.println("\nDati modul de criptare");
        //trimitem modul de criptare dorit catre server
        while(!modCriptare.equals("CBC") && !modCriptare.equals("OFB"))
            modCriptare = (new Scanner(System.in).nextLine()); // ? nu sunt sigur ca merge
        toServer.println(modCriptare); // trimitem catre server cererea pentru cheie
        // primim cheia corespunzatoare criptarii
        stringBuilder.append(fromServer.readLine());
        while(stringBuilder.length() < 32){
            stringBuilder.append('\n');
            if(stringBuilder.length() < 32)
                stringBuilder.append(fromServer.readLine());
        }

        cheieDeLaServer = new String(stringBuilder);
        keyBytes = cheieDeLaServer.getBytes(StandardCharsets.ISO_8859_1);
        // lasam cheia primita criptata pentru a o trimite catre B
        toServer.println(cheieDeLaServer); // trimitem catre nodul B cheia ce trebuie folosita pentru decriptare
        System.out.println(cheieDeLaServer.length());
        key = new String(cipherDecriptare.doFinal(keyBytes)); // obtinem cheia
        // initializam criptarea ECB
        System.out.println(key.length());
        keyBytes = key.getBytes(StandardCharsets.ISO_8859_1);
        System.out.println(keyBytes.length);
        cipherCriptare.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(keyBytes,"AES"));

        secureRandom.nextBytes(parametruIV);
        iv = new IvParameterSpec(parametruIV); // obtinem parametrul IV
        // trimitem parametrul IV catre B
        toServer.println(new String(cipherCriptare.doFinal(iv.getIV()),StandardCharsets.ISO_8859_1));
        System.out.println(new String(cipherCriptare.doFinal(iv.getIV()),StandardCharsets.ISO_8859_1).length());
        if(modCriptare.equals("CBC"))
            CBC();
        else if(modCriptare.equals("OFB"))
            OFB();
    }

    private void CBC() throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
//        System.out.println("Criptam CBC");
        int citit = 0;
        modCriptare = " ";
        while(canRead) {
            // obtinem IV-ul specific
            byte[] initializationVector = iv.getIV();
            char[] readed = new char[16];
            // citim datele de criptat din fisier
            for (int i = 0; i < 16 && (citit = bufferedReader.read()) != -1; i++) {
                readed[i] = (char) citit;
            }

            // ne asiguram ca nu mai continuam algoritmul dupa ce am terminat de citit din fisier
            if (citit == -1)
                canRead = false;
            System.out.println(readed);

            // transformam datele obtinute din fisier in byte
            byte[] plainText = new String(readed).getBytes(StandardCharsets.ISO_8859_1);

            // facem XOR intre datele citite din fisier si IV
            for (int i = 0; i < plainText.length; i++)
                plainText[i] = (byte) (plainText[i] ^ initializationVector[i]);

            // criptam cu ECB datele obtinute
            plainText = cipherCriptare.doFinal(plainText);

            // facem refresh la IV
            iv = new IvParameterSpec(plainText);

            // trimitem datele criptate catre B
            toServer.println(new String(plainText, StandardCharsets.ISO_8859_1));
            // In caz ca am terminat criptarea datelor din fisier anuntam serverul
            if (citit == -1)
                toServer.println("Am terminat");

            numarPacheteTrimise++;
            if (numarPacheteTrimise % 16 == 0)
                getKey();
        }

    }

    private void OFB() throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        System.out.println("Criptam OFB");

        int citit = 0;
        modCriptare = " ";
        while(canRead) {
            byte[] initializationVector = iv.getIV();
            char[] readed = new char[16];
            // citirea din fisier
            for (int i = 0; i < 16 && (citit = bufferedReader.read()) != -1; i++) {
                readed[i] = (char)citit;
            }

            // asigurarea ca nu mai rulam algoritmul daca am ajuns la finalul fisierului
            if (citit == -1)
                canRead = false;

            // transformam datele pe care le-am citit in bytes
            System.out.println(readed);

            byte[] plainText = new String(readed).getBytes(StandardCharsets.UTF_8);

            // criptam cu ECB IV-ul
            initializationVector = cipherCriptare.doFinal(initializationVector);

            // schimbam IV-ul folosit pentru criptarea urmatoare
            iv = new IvParameterSpec(initializationVector);

            // facem XOR intre datele citite din fisier si IV-ul criptat cu ECB
            for (int i = 0; i < plainText.length; i++)
                plainText[i] = (byte) (plainText[i] ^ initializationVector[i]);

            // trimitem catre B datele criptate
            toServer.println(new String(plainText,StandardCharsets.ISO_8859_1));

            // In caz ca am terminat criptarea datelor din fisier anuntam serverul
            if(citit == -1)
                toServer.println("Am terminat");

            numarPacheteTrimise++;
            if(numarPacheteTrimise%16 == 0)
                getKey();

        }

    }

}
