import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        KM KeyManager = new KM();
        int numarPacheteTrimise = 0;
        int lungimePachetePartiale;
        String modCriptare;
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(9991);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Socket nodA = null;
        try {
            nodA = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Acceptare esuata pentru nodul A.");
            e.printStackTrace();
        }

        Socket nodB = null;
        try {
            nodB = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Acceptare esuata pentru nodul B.");
            e.printStackTrace();
        }


        InputStream inputStream = nodA.getInputStream();

        OutputStream outputStreamNodA = nodA.getOutputStream();
        OutputStream outputStreamNodB = nodB.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        PrintWriter toNodA = new PrintWriter(outputStreamNodA,true);
        PrintWriter toNodB = new PrintWriter(outputStreamNodB,true);


        toNodA.println("GO");

        String MesajNodA = "Asteptam modul de criptare cerut de Nodul A.";

        MesajNodA = bufferedReader.readLine() ;

        toNodB.println("GO");
        System.out.println("Modul cerut de nodul A este: " + MesajNodA);
        modCriptare = MesajNodA;

        toNodB.println(MesajNodA); // anuntam pe B modul cerut de A

        toNodA.println(new String(KeyManager.getKey(MesajNodA), StandardCharsets.ISO_8859_1));

        // ne asiguram ca trimitem toata cheia catre nodul B
        int lungimeCheie = 0;
        MesajNodA = bufferedReader.readLine();
        toNodB.println(MesajNodA);
        System.out.println("lungimea la cheia primita este: " + MesajNodA.length() + " iar lungimea in bytes: " + MesajNodA.getBytes(StandardCharsets.ISO_8859_1).length);
        lungimeCheie += MesajNodA.length();
        while(lungimeCheie < 32) {
            MesajNodA = bufferedReader.readLine();
            toNodB.println(MesajNodA);
            lungimeCheie = lungimeCheie + 1 + MesajNodA.length();
            System.out.println("lungimea la cheia primita este: " + MesajNodA.length() + " iar lungimea in bytes: " + MesajNodA.getBytes(StandardCharsets.ISO_8859_1).length + " valoare lungime pachet trimis: " + lungimeCheie);
        }
        // ne asiguram ca trimitem tot IV-ul catre nodul B
        lungimeCheie = 0;
        MesajNodA = bufferedReader.readLine();
        toNodB.println(MesajNodA);
        lungimeCheie += MesajNodA.length();
        while(lungimeCheie < 32) {
            MesajNodA = bufferedReader.readLine();
            toNodB.println(MesajNodA);
            lungimeCheie = lungimeCheie + 1 + MesajNodA.length();
        }


        /** Problema in cazul in care cheia contine newline trebuie verificat ca trimiterea cheii catre B sa fie full */

        while (!(MesajNodA = bufferedReader.readLine()).equals("Am terminat")){
            System.out.println(MesajNodA.length() + "pachet initial");
            if(modCriptare.equals("CBC"))
            {
                if(MesajNodA.length() == 32){
                    toNodB.println(MesajNodA);
                    numarPacheteTrimise++;
                    System.out.println("Am primit un pachet complet");
                }
                else{
                    lungimePachetePartiale = MesajNodA.length();
                    toNodB.println(MesajNodA);
                    while(lungimePachetePartiale < 32) {
                        System.out.println(lungimePachetePartiale + "Pachet partial in primire ");
                        MesajNodA = bufferedReader.readLine();
                        lungimePachetePartiale = lungimePachetePartiale + 1 + MesajNodA.length();
                        toNodB.println(MesajNodA);
                    }
                    System.out.println(lungimePachetePartiale + " Am completat pachetul");
                    numarPacheteTrimise++;
                }
            }
            else if(modCriptare.equals("OFB")){
                    if(MesajNodA.length() == 16){
                        toNodB.println(MesajNodA);
                        numarPacheteTrimise++;
                        System.out.println("Am primit un pachet complet");
                    }
                    else{
                        lungimePachetePartiale = MesajNodA.length();
                        toNodB.println(MesajNodA);
                        while(lungimePachetePartiale < 16) {
                            System.out.println(lungimePachetePartiale + "Pachet partial in primire ");
                            MesajNodA = bufferedReader.readLine();
                            lungimePachetePartiale = lungimePachetePartiale + 1 + MesajNodA.length();
                            toNodB.println(MesajNodA);
                        }
                        System.out.println(lungimePachetePartiale + " Am completat pachetul");
                        numarPacheteTrimise++;
                }
            }

            if(numarPacheteTrimise % 16 == 0){
                MesajNodA = bufferedReader.readLine() ;
                System.out.println("Modul cerut de nodul A este: " + MesajNodA);
                modCriptare = MesajNodA;

                toNodB.println(MesajNodA); // anuntam pe B modul cerut de A

                toNodA.println(new String(KeyManager.getKey(MesajNodA), StandardCharsets.ISO_8859_1));

                // ne asiguram ca trimitem toata cheia catre nodul B
                lungimeCheie = 0;
                MesajNodA = bufferedReader.readLine();
                toNodB.println(MesajNodA);
                System.out.println("lungimea la cheia primita este: " + MesajNodA.length() + " iar lungimea in bytes: " + MesajNodA.getBytes(StandardCharsets.ISO_8859_1).length);
                lungimeCheie += MesajNodA.length();
                while(lungimeCheie < 32) {
                    MesajNodA = bufferedReader.readLine();
                    toNodB.println(MesajNodA);
                    lungimeCheie = lungimeCheie + 1 + MesajNodA.length();
                    System.out.println("lungimea la cheia primita este: " + MesajNodA.length() + " iar lungimea in bytes: " + MesajNodA.getBytes(StandardCharsets.ISO_8859_1).length + " valoare lungime pachet trimis: " + lungimeCheie);
                }
                // ne asiguram ca trimitem tot IV-ul catre nodul B
                lungimeCheie = 0;
                MesajNodA = bufferedReader.readLine();
                toNodB.println(MesajNodA);
                lungimeCheie += MesajNodA.length();
                while(lungimeCheie < 32) {
                    MesajNodA = bufferedReader.readLine();
                    toNodB.println(MesajNodA);
                    lungimeCheie = lungimeCheie + 1 + MesajNodA.length();
                }
            }

        }
            toNodB.println(MesajNodA);


        toNodA.println("Inchide");

    }
}
