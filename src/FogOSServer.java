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
    	new SignalServer().start();
        new FogOSServer().start();
    }

    void start() {
        FS1 = FlexIDSession.accept();

        try {
            if(FS1 == null) {
                System.out.println("Server failed.");
                System.exit(0);
            }

            System.out.println("Server sends a message to the client.");

            System.out.println("Server sends a entire data size to the client.");
            int dataSize = 1000000;
            FS1.send(Conversion.int32ToByteArray(dataSize));
            byte[] message = "a".getBytes();
            int i = 0;
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

    static class SignalServer extends Thread {
        @Override
        public void run() {
            try {
            	while(true) {
	                System.out.println("Start the Signal Server");
	                ServerSocket signal = new ServerSocket(3334);
	
	                Socket socket = signal.accept();
	                System.out.println("Accept the signal from the client");
	                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	
	                JSONObject request = new JSONObject(input.readLine()); // Check the flex_id and resolve a new flex_id which serves the contents.
	                
	                String type = request.getString("type"); // reconnect or terminate
	                String flex_id = request.getString("flex_id");
	                
	                JSONObject response = new JSONObject();
	                if(type == "reconnect") {
		                System.out.println("Received) ID: " + flex_id + " / Status: changed");
		                int newport = 3337;
		                
		                response.put("type", "reconnectACK");
		                response.put("flex_id", new String(FS1.getDFID().getIdentity()));
		                response.put("ip", "147.46.216.213");
		                response.put("port", newport);
		                out.println(response);
	
		                FS1.handleReconnect(newport);
	                }
	                else if(type == "terminate") {
	                	response.put("type", "terminateACK");
	                	response.put("flex_id",  new String(FS1.getDFID().getIdentity()));
	                	out.println(response);
	                	
	                	FS1.close();
	                }
	                
	                signal.close();
            	}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}