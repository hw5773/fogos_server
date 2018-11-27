
import org.json.JSONObject;
import versatile.flexidsession.Conversion;
import versatile.flexidsession.FlexIDSession;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class FogOSServer {

    public static void main(String[] args) {
        FlexIDSession FS1 = FlexIDSession.accept();

        try {
            if(FS1 == null) {
                System.out.println("Server failed.");
                System.exit(0);
            }

            System.out.println("Server sends a message to the client.");

            int dataSize = 10000;
            int i = 0;

            System.out.println("Server sends a entire data size to the client.");
            FS1.send(Conversion.int32ToByteArray(dataSize));
            byte[] message = "a".getBytes();
            while(true) {
                if(FS1.send(message) > 0) // always true unless it exceeds server's wbuf size
                    i++;
                if(i >= dataSize) break;
            }

            System.out.println("done");
            Thread.sleep(1000000);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            FS1.close();
        }
    }

    class SignalServer implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket signal = new ServerSocket(3334);
                while (true) {
                    Socket socket = signal.accept();
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        JSONObject json = new JSONObject(input.readLine());
                        String flex_id = json.getString("flex_id");
                        String status = json.getString("status");

                        System.out.println("Received) ID: " + flex_id + " / Status: " + status);


                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}