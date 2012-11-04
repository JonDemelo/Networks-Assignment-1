import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Jonathan Demelo 250519903
 * @version ASN1 CS3357
 * @category Client side: Receives information from client and determines proper response
 */
public class Client {
	private final static int SIZE = 65508; // max byte size
	private int port;
	private InetAddress host;
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private Boolean triggerFile = false; // used to keep out of file transfer area prematurely.
	private String originalLoc = ""; // used in cases of file transfer for origin.
	private File file;
	private String funcNumber;
	private FileInputStream inputFile;
	
	
	public Client() throws UnknownHostException {
		setPort(5000);
		setHost(InetAddress.getByName("localhost"));
	}

	public void runClient() throws IOException {
		// UDP
		Scanner input = new Scanner(System.in);
		// ask user for server IP
		System.out.print("Enter Target Server IP (localhost, etc.)\nClient: "); 
		try {
		this.setHost(InetAddress.getByName(input.nextLine()));
		} catch (Exception e){ // catch if user enters in invalid IP
			System.out.println("This IP does not exist..");
			System.out.println("Closing Client..");
			System.exit(0);
		}
		DatagramSocket client = new DatagramSocket(); // UDP Socket creation
		byte[] tempByte = "1".getBytes(); // send connection key to IP
		// Build first UDP packet.
		DatagramPacket clientPacket = new DatagramPacket(tempByte,
				tempByte.length, getHost(), getPort());
		client.send(clientPacket); // send to destination

		// server's returned message
		clientPacket = new DatagramPacket(new byte[SIZE], SIZE); // empty packet
		client.receive(clientPacket); // grab response
		String serverResponse = new String(clientPacket.getData());
		// i.e. Press 1 for 'Echo', 2 for ...
		System.out.println("Server: " + serverResponse); 

		tempByte = new byte[SIZE];
		System.out.print("Client: "); // client's response for function number.
		funcNumber = input.nextLine();
		/* Check to see if input was proper. If not, close the connection. */
		if (!funcNumber.equals("1") && !funcNumber.equals("2") && !funcNumber.equals("3")){
			System.out.println("You Did Not Input a Correct Option..");
			System.out.println("Closing Connection..");
			System.exit(0); // close program.
		}
		tempByte = funcNumber.getBytes();
			
		clientPacket = new DatagramPacket(tempByte, tempByte.length, getHost(),
				getPort());
		client.send(clientPacket); // send the function type to server

		clientPacket = new DatagramPacket(new byte[SIZE], SIZE); // new port
		client.receive(clientPacket); // receive the new port from the server
		String tempPort = new String(clientPacket.getData());
		tempPort = tempPort.replaceAll("[^\\d]", ""); // remove all non digits
		this.setPort(Integer.parseInt(tempPort)); // set the new port (6000)

		clientPacket = new DatagramPacket(new byte[SIZE], SIZE); // new protocol
		client.receive(clientPacket); // recieve the new protocol TCP vs. UDP
		String protocol = new String(clientPacket.getData());

		if (protocol.contains("TCP")) { // prepare TCP connection
			setSocket(new Socket(getHost(), getPort())); // build TCP socket
			in = new BufferedReader(new InputStreamReader(getSocket()
					.getInputStream())); // new input stream
			// new output stream
			out = new PrintWriter(getSocket().getOutputStream(), true);
			
			while (true) {
				/* Preparing Input from Server */
				serverResponse = in.readLine();

				if (serverResponse.contains("exitProgram")) { 
					/* if server is requesting the connections to close */
					System.out.println("Closing Connection..");
					System.exit(0);
				} else if (serverResponse.contains("connectionkey")) {
					/* if the server wants some reply from the client */
					System.out.print("Client: ");
					// Get what the user types
					String clientResponse = input.nextLine();
					out.println(clientResponse); // send input to server
					
					/* Since file transfer request extra information from the client
					 * the server asks for the original file's location, as well as the
					 * client's desired file name of the sent file. 
					 *  */
					if(funcNumber.contains("2")){ // extra for file transfer
						/* a trigger is used as the client uses the first input file location
						 * whereas the server only requires the second destination file name */
						if(triggerFile == true){ 
							// if the first destination has already been inputed, else skip
							try{
								file = new File (originalLoc); // build file object
							    tempByte = new byte [(int) file.length()]; // build byte array
								inputFile = new FileInputStream(file);
							} catch (FileNotFoundException e){ // if error in location
					        	System.out.println("This File Does Not Exist..");
					        	System.out.println("Closing Connection..");
					        	System.exit(0);
					        }
							/* new input from file to client socket */
						    BufferedInputStream buffIn = new BufferedInputStream(inputFile);
						    buffIn.read(tempByte, 0, tempByte.length);
						    /* new output from client socket to the server */
						    OutputStream outputFile = socket.getOutputStream();
						    /* write to the server*/
						    outputFile.write(tempByte, 0, tempByte.length);
						    outputFile.flush(); // clear outputstream
						    buffIn.close(); // close input buffer.
						} else { 
							/* if it's the original location input, simply save in String
							 * for future use, as the server does not need to have this information
							 * */
							originalLoc = clientResponse;
							triggerFile = true; // allow into final area next input. 
						}
					} 
				} else if (serverResponse.contains("infoKey")){
					/* infoKey token accesses the ability to send client information */
					/* Grab current time and date from Date object */
					Date date = new Date();
					/* Send to server */
					out.println(date + " " + socket.getInetAddress() + " " + socket.getPort());
				} else {
					/* Server simply wanted to output to the client's screen */
					System.out.println("Server: " + serverResponse);
				}
			}
		} else if (protocol.contains("UDP")) { // UDP
			while (true) {
				tempByte = new byte[SIZE]; // new byte array
				clientPacket = new DatagramPacket(new byte[SIZE], SIZE); // packet
				client.receive(clientPacket); // get packet from server
				/* transcribe server's response */
				serverResponse = new String(clientPacket.getData());

				/* if the server requests to close the connection */
				if (serverResponse.contains("endProgram")) {
					System.out.println("Server: Connection Closed..");
					System.exit(0);
				} else if (serverResponse.contains("connectionkey")) { 
					// if key for getting client input is called
					System.out.print("Client: ");
					tempByte = input.nextLine().getBytes();
					clientPacket = new DatagramPacket(tempByte,
							tempByte.length, getHost(), getPort());
					client.send(clientPacket);
				} else { // else if the server just wants to display its output
					System.out.println("Server: " + serverResponse);
				} 
			}
		} else { // if the connection cannot be made, close.
			System.out.println("Server Error, Closing Client..");
			System.exit(0);
		}
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

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public static void main(String args[]) throws IOException {
		Client c = new Client();
		c.runClient();
	}
}
