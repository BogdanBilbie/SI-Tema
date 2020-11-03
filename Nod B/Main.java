import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class Main {
    public static void main(String[] args){
        final String hostname = "localhost";
        final int port = 9991;

        try{
            Socket socket = new Socket(hostname,port);

            InputStream inputStream = socket.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while(!bufferedReader.readLine().equals("GO")){

            }
            NodB nodB = new NodB(bufferedReader);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

    }

}
