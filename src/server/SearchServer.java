import java.io.*;  
import java.net.*; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
public class SearchServer {
	public static final int Search_PORT = 6666;
	
	public static void main(String[] args) throws UnknownHostException, IOException, Exception {
		System.out.println("The server started .. ");
		new Thread() {
			public void run() {
				try {
					ServerSocket ss = new ServerSocket(Search_PORT);
					while (true) {
                        System.out.println("Waiting for client to connect");
						new searching(ss.accept()).start();
                        System.out.println("accepted");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
    private static class searching extends Thread {
        Socket socket;

		public searching(Socket s) {
			this.socket = s;    
        }

        public void run() {
			try {
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());  
                DataInputStream in = new DataInputStream(socket.getInputStream());
                while(true){
                    String msg=(String)in.readUTF();
                    System.out.println("Client: "+ msg);
                    dout.writeUTF("Thank You For Connecting.");
                }//dout.flush();
                //dout.close();
                //socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
}