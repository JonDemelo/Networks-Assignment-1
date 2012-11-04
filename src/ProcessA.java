/* Server stringBuff A */

import java.io.*;
import java.net.*;

public class ProcessA {
	private int mode, port;
	private String host;
	private ServerSocket server = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String temp, tempOut;

	private DatagramSocket dataServer = null;
	private byte[] dataIn = new byte[65508];
	private byte[] dataOut = new byte[65508];
	private DatagramPacket dataSent = null;
	private DatagramPacket dataReceived = null;
	private InetAddress address = null;

	ProcessA() {
		this.setPort(5000);
		// server = new ServerSocket(getPort());
	}

	public void runServer() throws IOException {
		System.out.println("Starting Server, Waiting for Client..");
		try {
			dataServer = new DatagramSocket(getPort());
			for (;;){
				dataReceived = new DatagramPacket(dataIn, dataIn.length);
				dataServer.receive(dataReceived);
				temp = new String(dataReceived.getData());
	
				if (temp.contains("1")) {
					setAddress(dataReceived.getAddress());
					connected(dataServer);
				}
			}
		} catch (Exception e) {}
		// Socket socket = server.accept();
		// connected(socket);
	}

	public void connected(DatagramSocket dataServer) throws IOException {
		try {
			/*
			 * in = new BufferedReader(new InputStreamReader(
			 * socket.getInputStream())); out = new
			 * PrintWriter(socket.getOutputStream(), true); String temp = "";
			 */

			while (true) {

				tempOut = "Press 1 for 'Echo', 2 for 'File Transfer', 3 for"
						+ " 'Client Information', anything else to 'Exit'";
				
				dataOut = tempOut.getBytes();
				dataSent = new DatagramPacket(dataOut, dataOut.length,
						getAddress(), getPort());
				dataServer.send(dataSent);

				// out.println("Press 1 for 'Echo', 2 for 'File Transfer', 3 for"
				// + " 'Client Information', 0 to 'Exit'"); // output
				// out.println("1");

				dataOut = "1".getBytes();
				dataSent = new DatagramPacket(dataOut, dataOut.length,
						getAddress(), getPort());
				dataServer.send(dataSent);

				// temp = in.readLine(); // get input
				dataReceived = new DatagramPacket(dataIn, dataIn.length);
				dataServer.receive(dataReceived);
				temp = new String(dataReceived.getData());

				if (temp.contains("1")) {
					echo(/* socket */);
				} else if (temp.contains("2")) {
					fileTransfer(/* socket */);
				} else if (temp.contains("3")) {
					info(/* socket */);
				} else if (temp.contains("0")) {
					out.println("Closing Connection.");
					// socket.close();
					System.exit(0);
				} else {
					out.println("You did not choose a correct option.");
				}
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// socket.close();

		}

	}

	public void fileTransfer(/* Socket socket */) throws IOException {

	}

	public void info(/* Socket socket */) throws IOException {

	}

	public void echo(/* Socket socket */) throws IOException {
		try {
			// out.println("Server will now echo all input.. type 'exit' to quit."); // output

			while (true) {
				try {
				//	out.println("1");
					dataOut = "1".getBytes();
					dataSent = new DatagramPacket(dataOut, dataOut.length,
							getAddress(), getPort());
					dataServer.send(dataSent);
					
				//	temp = in.readLine();
					dataReceived = new DatagramPacket(dataIn, dataIn.length);
					dataServer.receive(dataReceived);
					temp = new String(dataReceived.getData());
					
					if (temp.contains("exit")) {
						dataOut = "Closing Connection.".getBytes();
						dataSent = new DatagramPacket(dataOut, dataOut.length,
								getAddress(), getPort());
						dataServer.send(dataSent);
					//	out.println("Closing Connection.");
						// socket.close();
						System.exit(0);
					}

					//out.println(temp); // output
					dataOut = temp.getBytes();
					dataSent = new DatagramPacket(dataOut, dataOut.length,
							getAddress(), getPort());
					dataServer.send(dataSent);
				} catch (IOException e) {/**/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// socket.close();
		}
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
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

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public static void main(String[] args) throws IOException {
		ProcessA serv = new ProcessA();
		serv.runServer();
	}

}
