import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/* 
 * The following program implements the RIP protocol.
 * There are two classes LinkServer and LinClient which
 * are acting as client and server. The client class
 * initiates the connections and passes the message to the
 * server and the server class prints the table, implements
 * the timer function and after every 5sec prints the 
 * routing table, handles the failure of node by checking 
 * whether node responds or not and implements CIDR.
 * 
 * There is a main class which takes in the file
 * and provides us with the link and the network and the class
 * also creates objects for client and server. 
 * 
 * Author :   Neha Upadhyay    nxu3128@rit.edu
 */


/*
 * The class createsPort and communicates with the 
 * server and passes the messages.
 */
class Linkclient
{

	ArrayList<Integer> list = new ArrayList<Integer>();
	
	//takes out the port of the current and the neighboring node
	public void createPort(String port1) {
		int linkport2 = Integer.valueOf(port1.substring(10, 15));
		int linkneighborport = Integer.valueOf(port1.substring(27,port1.length()));
		
		
		// checks whether the element that is the port is in the list or not
		
		if(!list.contains(linkneighborport))
		{
			list.add(linkneighborport);
			Thread clientThread = new Thread()
			{
				public void run()
				{
					try {
						// sends ping to the server
						DatagramSocket socket = new DatagramSocket();		
						InetAddress getaddress = InetAddress.getByName("localhost");
						String message = "Client Connects";     	
						byte[] sendMessage = message.getBytes();	     	   	
						DatagramPacket sentPacket = new DatagramPacket(sendMessage, sendMessage.length, getaddress, linkneighborport);	     	
						socket.send(sentPacket);
			     	
						// receives from the server
						byte[] messageReceive = new byte[256];
						DatagramSocket receiver = new DatagramSocket(linkneighborport);				
						DatagramPacket packetReceive = new DatagramPacket(messageReceive, messageReceive.length);
						receiver.receive(packetReceive);
						String messageNew = new String(messageReceive, 0, packetReceive.getLength());
						System.out.println("Server: " +messageNew);
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		
			clientThread.start();
			
		}
	}
}
/*
 * The below class is the server class. It prints the routing table,
 * communicates with the client, creates threads for receiving messages 
 * handles the failure of the node, , prints routing tables every 5secs by
 * implementing the timer class.
 */
class Linkserver extends TimerTask
{

	public static int cost = 0;
	ArrayList<Integer> listNew = new ArrayList<Integer>();
	static ArrayList<Integer> hops = new ArrayList<Integer>();
	static ArrayList<Integer> port = new ArrayList<Integer>();
	static ArrayList<String> address = new ArrayList<String>();

	// prints the rip table
	public static void printTable(int cost,int linkneighborport, String network)
	{
		port.add(linkneighborport);
		hops.add(cost);
		address.add(network);
		System.out.println("Address        NextHop           Cost");
		System.out.println("=====================================");
		for(int i = 0 ; i <hops.size();i++)
		{
			System.out.println(address.get(i) + "               " + port.get(i) + "              " + hops.get(i));
		}
		
	}
	
	// adds the addresses to the arraylist
	public static void printAddress(String network,int cost, int hop)
	{
		address.add(network);
		port.add(0);
		hops.add(0);

	}

	// takes out the port of the current and the neighboring link
	
	public void createPort(String port1, String network) {
		System.setProperty("java.net.preferIPv4Stack", "true");

		int linkport2 = Integer.valueOf(port1.substring(10, 15));
		int linkneighborport = Integer.valueOf(port1.substring(27,port1.length()));
		cost = 0;
		address.add(network);
		
		// checks whether the element is in the list or not
		if(!listNew.contains(linkneighborport))
		{
			listNew.add(linkneighborport);
			address.add(network);
			
			// makes each link a thread and runs
			
			Thread serverThread = new Thread()
			{
			
				public void run()
				{
					try {
					
						// receives from client
						long startTime = System.currentTimeMillis();
						byte[] messageReceive = new byte[256];
						DatagramSocket receiver = new DatagramSocket(linkneighborport);				
						DatagramPacket packetReceive = new DatagramPacket(messageReceive, messageReceive.length);
						receiver.receive(packetReceive);
						String message = new String(messageReceive, 0, packetReceive.getLength());
						System.out.println(" Message " +message);
						long stopTime = System.currentTimeMillis();
						long timeTaken = stopTime - startTime;
						if(timeTaken > 5000)
						{
							printTable(cost,linkneighborport,network);
							this.stop();
						}
						
						// sends to client
						DatagramSocket socket = new DatagramSocket();
						String messageFinal = " Your message Received ";     	
						byte[] sendMessage = messageFinal.getBytes();	     	
			     		InetAddress getaddress = InetAddress.getByName("localhost");
			     		DatagramPacket sentPacket = new DatagramPacket(sendMessage, sendMessage.length, getaddress, linkneighborport);	     	
			     		socket.send(sentPacket);
			     		
					// handles the SocketException and updates the routing hop and count to 16
			     	// if the client doesn't reply 
					} catch (Exception e) {
						e.printStackTrace();
						cost = 16;
						printTable(cost,linkneighborport,network);

						
					}
				}
			};	
			cost = cost + 1;
			serverThread.start();
			Timer display = new Timer();
			display.schedule(new Linkserver(),5000); //display result in every 5secs
			
			printTable(cost,linkneighborport,network);
		}
		
		
	}
	// run function for the timer class. Used for printing table in every 5 secs
	
	@Override
	public void run() {
		System.out.println("Address        NextHop           Cost");
		System.out.println("=====================================");
		for(int i = 0 ; i <hops.size();i++)
		{
			System.out.println(address.get(i) + "               " + port.get(i) + "              " + hops.get(i));
		}
	}
	
}

/*
 * The below class implements the main function and it processes the
 * incoming file. Link and Network are taken out and object for
 * server and client are made and address and the link are passed.
 * It also handles and sends the CIDR messages.
 * 
 */
public class LinkNew {

	// takes in the file as command line arguments and creates the  object of client and server
	public static void main(String args[])
	{
		try
		{
			Linkclient linkclient = new Linkclient();
			Linkserver linkserver = new Linkserver();
			String path = args[0];
			String newPath = path;
			String linesInFile = "";
			String newLines = "";
			String port1 = "", network = "";
			System.out.println(" Path of the file is : " +path);
			BufferedReader buffer = new BufferedReader(new FileReader(path));
			BufferedReader bufferNew = new BufferedReader(new FileReader(newPath));

			// passes the addresses to server
			// handles the network address and uses CIDR
			// passes the CIDR address
			while((newLines = bufferNew.readLine()) != null)
			{
				if(newLines.contains("NETWORK: "))
				{
					 network = newLines.substring(9,newLines.length());
					 String[] arr = network.split("/");
					 int val = Integer.parseInt(arr[1]);
					 String newAddress = arr[0];
					 int addressMask = 0xffffffff << (32 - val);
					 
					 byte[] bytes = new byte[]{ 
					            (byte)(addressMask >>> 24), (byte)(addressMask >> 16 & 0xff), (byte)(addressMask >> 8 & 0xff), (byte)(addressMask & 0xff) };

					 InetAddress addrNew = InetAddress.getByAddress(bytes);
					    
					 linkserver.printAddress(addrNew.getHostAddress(),0,0);
				}
			}
			// passes the link to the client and server
			while((linesInFile = buffer.readLine()) != null)
			{
				if(linesInFile.contains("LINK: "))
				{
					 port1 = linesInFile.substring(6, linesInFile.length());
					 linkclient.createPort(port1);
					 linkserver.createPort(port1,network);
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}
}
