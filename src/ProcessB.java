/* Client Process B*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ProcessB {
	private int port;
	private String host, input;
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String clientResponse = "";
	private String serverResponse = "";
	
	private DatagramSocket dataClient = null;
	private byte[] dataIn = new byte[65508];
    private byte[] dataOut = new byte[65508];
    private DatagramPacket dataSent = null;
    private DatagramPacket dataReceived = null;
    private InetAddress address = null;  
    


	ProcessB(){
		this.setPort(5000);
		this.setHost("localhost");
		try {
			this.setAddress(InetAddress.getByName(getHost()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void runClient() throws IOException{
		try{
			dataClient = new DatagramSocket();
			
		    dataOut = "1".getBytes();
            dataSent = new DatagramPacket(dataOut, dataOut.length, getAddress(), getPort());
            dataClient.send(dataSent);

			Scanner input = new Scanner(System.in);
			// setSocket(new Socket(getAddress(),getPort()));
			// in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// out = new PrintWriter(socket.getOutputStream(),true);
			while(true){
				/* Preparing Input from Server */
				// serverResponse = in.readLine();
				try{
					dataReceived.setData(dataIn);
				    dataClient.receive(dataReceived);
					serverResponse = new String(dataReceived.getData());
					
					if(serverResponse.equals("1")){ // if key for getting client input is called
						/* Outputting to Server */
					    clientResponse = input.nextLine(); // Get what the user types.
					    
					    dataOut = clientResponse.getBytes();
			            dataSent = new DatagramPacket(dataOut, dataOut.length, getAddress(), getPort());
			            dataClient.send(dataSent);
					    // out.println(clientResponse);
					} else {
						System.out.println(serverResponse);
					}
				} catch (Exception e){}
			}
			    
		} catch (Exception e){ /**/ }
		finally {/* socket.close(); */}
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public static void main(String[] args) throws IOException {
		ProcessB client = new ProcessB();
		client.runClient();
	}

}
