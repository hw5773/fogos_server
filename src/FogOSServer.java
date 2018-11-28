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
        new FogOSServer().start();
    }

    void start() {
        SignalServer ss = new SignalServer();
        ss.start();
        FS1 = FlexIDSession.accept();

        try {
            if(FS1 == null) {
                System.out.println("Server failed.");
                System.exit(0);
            }

            System.out.println("Server sends a message to the client.");

            int dataSize = 1000000;
            int i = 0;

            System.out.println("Server sends a entire data size to the client.");
            FS1.send(Conversion.int32ToByteArray(dataSize));
            byte[] message = "a".getBytes();
            while(true) {
                if (i <= dataSize) {
                    if (FS1.send(message) > 0) // always true unless it exceeds server's wbuf size
                        i++;
                }
                if ((i > dataSize) && (FS1.checkMsgToSend() < 0)) break;
            }

            System.out.println("done");
            //Thread.sleep(1000000000);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            FS1.close();
        }
    }

    class SignalServer extends Thread {
        @Override
        public void run() {
            try {
                System.out.println("Start the Signal Server");
                ServerSocket signal = new ServerSocket(3334);

                Socket socket = signal.accept();
                System.out.println("Accept the signal from the client");
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                JSONObject request = new JSONObject(input.readLine());
                String flex_id = request.getString("flex_id");
                String status = request.getString("status");

                System.out.println("Received) ID: " + flex_id + " / Status: " + status);

                JSONObject response = new JSONObject();
                response.put("flex_id", new String(FS1.getDFID().getIdentity()));
                response.put("ip", "147.46.216.213");
                response.put("port", 3337);
                out.println(response);

                FS1.mobility();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}