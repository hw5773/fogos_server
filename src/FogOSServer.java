import org.json.JSONObject;
import versatile.flexidsession.Conversion;
import versatile.flexidsession.FlexIDSession;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FogOSServer {
    static FlexIDSession FS1;

    public static void main(String[] args) {
        FS1 = FlexIDSession.accept();
        SignalServer ss = new SignalServer();
        ss.start();

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

    static class SignalServer extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket signal = new ServerSocket(3334);
                while (true) {
                    Socket socket = signal.accept();
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter
                        JSONObject request = new JSONObject(input.readLine());
                        String flex_id = request.getString("flex_id");
                        String status = request.getString("status");

                        System.out.println("Received) ID: " + flex_id + " / Status: " + status);

                        // TODO: We should open a new listener with the port 3336
                        FS1.mobility();

                        JSONObject response = new JSONObject();
                        response.put("ip", "147.46.216.213");
                        response.put("port", "3336");


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