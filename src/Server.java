import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Jonathan Demelo 250519903
 * @version ASN1 CS3357
 * @category Server side: Receives information from server based off user input
 */
public class Server {
	private final static int SIZE = 65508;
	private int port;
	private InetAddress host;
	private ServerSocket serverTCP = null;
	private BufferedReader in = null;
	private PrintWriter out = null;

	public Server() throws UnknownHostException {
		setPort(5000);
		setHost(InetAddress.getByName("localhost"));
	}

	public void runServer() throws IOException {
		 // create port opening
		DatagramSocket server = new DatagramSocket(getPort());
		System.out.println("Starting Server..");
		DatagramPacket serverPacket = new DatagramPacket(new byte[SIZE], SIZE);
		server.receive(serverPacket); // received connection request
		String temp = new String(serverPacket.getData());

		if (temp.contains("1")) // if client connects to server
			connected(server, serverPacket);
	}
	
	/* Takes in the connected UDP Socket object and the current UDP packet */
	private void connected(DatagramSocket server, DatagramPacket serverPacket)
			throws IOException {
		System.out.println("Connected to Client..");
		/* after connection, output to client the response conditions */
		String serverResponse = "Press 1 for 'Echo', 2 for 'File Transfer', 3 for"
				+ " 'Client Information', anything else to 'Exit'";
		byte[] tempByte = serverResponse.getBytes(); // build byte array to store data
		serverPacket.setData(tempByte); // build packet
		server.send(serverPacket); // send first server message

		// Receive response on client function selection.
		server.receive(serverPacket); // grab response from client
		String clientResponse = new String(serverPacket.getData()); // transcribe to String
		serverPacket.setData("6000".getBytes()); // tell client new port
		server.send(serverPacket); // inform client of new port

		serverPacket.setData("TCP".getBytes());
		server.send(serverPacket); // inform client of new protocol

		serverTCP = new ServerSocket(6000); // build the TCP connection
		Socket socket = serverTCP.accept(); // accept client's new request

		/* based off the client's function request, begin the appropriate function */
		if (clientResponse.startsWith("1")) {
			/* will echo|reply the client's input back to the client*/
			echo(socket);
		} else if (clientResponse.startsWith("2")) {
			/* after asking the client which file they want to send and the file's
			 * destination name, the server will transfer the file from the client to
			 * the server */
			fileTransfer(socket);
		} else if (clientResponse.startsWith("3")) {
			/* the server will reply to the client with some basic information about
			 * the client */
			info(socket);
		} else { // if not a proper response, tell the client to end connections
			serverPacket.setData("endProgram".getBytes());
			server.send(serverPacket);
			System.exit(0);
		}
	}

	/* Takes in the TCP socket 
	 * echo(socket) will output all messages received from client
	 * */
	private void echo(Socket socket) throws IOException {
		/* build IO connections */
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		out.println("Server will now echo all input.. type 'exit' to quit."); // output

		while (true) {
			/* grab input from client */
			out.println("connectionkey");
			String temp = in.readLine();
			/* if that input is the exit request*/
			if (temp.equals("exit")) {
			/* send the exit response */
				out.println("exitProgram");
				socket.close();
				System.exit(0);
			}
			/* otherwise simply send back the packet */
			System.out.println("Client: " + temp); // output
		}
	}

	/* Takes in the TCP socket 
	 * fileTransfer(socket) will grab file information from client and
	 * transfer the file from the client's hard disk to the server's
	 * */
	private void fileTransfer(Socket socket) throws IOException {
		/* Build IO */
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		/* Output to client the requirements */
		out.println("Please enter the location of the original file.. (i.e /Users/Name/Documents/test.rtf)");
		out.println("connectionkey"); // ask for client response
		String fileName = in.readLine(); // location of original file
		
		out.println("Please type the new file's name and extension (ie. test.rtf)..");
		out.println("connectionkey"); // ask for client response
		fileName = in.readLine(); // toss old file loc, only need new.
		
		byte tempByte[] = new byte[SIZE]; // new byte array to store input
		/* build File IO structure */
        InputStream input = socket.getInputStream();
		FileOutputStream output = new FileOutputStream(fileName);
		BufferedOutputStream buffOut = new BufferedOutputStream(output);
        int reader = input.read(tempByte,0,tempByte.length); // size of the inputed file
        
        try {
        buffOut.write(tempByte, 0, reader); // place inputed file to file output
        } catch (ArrayIndexOutOfBoundsException e){ // if invalid location from client
        	out.println("File Location Invalid..");
        	out.println("exitProgram");
        	System.exit(0);
        }
        buffOut.flush(); // clear buffer
        buffOut.close();// close connection
        
        out.println("File has been Sent to Server..");
 		out.println("exitProgram");
 		socket.close();
 		System.exit(0);
	}

	/* Takes in the TCP socket 
	 * info(socket) will take the information from the client
	 * */
	private void info(Socket socket) throws IOException {
		/* Build IO structures */
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println("infoKey"); // sends token for retrieval of info
		String temp = in.readLine(); // gets input from client
		System.out.println("Client: " + temp);
		out.println("endProgram");
		socket.close(); // close connection
		System.exit(0);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getHost() {
		return host;
	}

	public void setHost(InetAddress host) {
		this.host = host;
	}

	public static void main(String args[]) throws IOException {
		Server s = new Server();
		s.runServer();
	}
}
