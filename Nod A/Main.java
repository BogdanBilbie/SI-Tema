import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args){
        final String hostname = "localhost";
        final int port = 9991;

        try{
            Socket socket = new Socket(hostname,port);

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(outputStream,true);

            while(!bufferedReader.readLine().equals("GO")){

            }
            System.out.println("Am primit mesajul de plecare");
            NodA nodA = new NodA(bufferedReader,printWriter);

        } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

}
