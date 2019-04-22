import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

//import com.sun.swing.internal.plaf.metal.resources.metal;

public class cdht{
	public int ID; //[0,255]
	public int port;
	public int MSS;
	private float dropProb; //(0,1)
	//successors
	private int succID1; //[0,255]
	private int succID2; //[0,255]
	public int succPort1;
	public int succPort2;
	public InetAddress host;
	public DatagramSocket socket;
	public int expectedSeqNum;
	public int predPort1;
	public int predPort2;
	
	public cdht (int _ID, int _succID1, int _succID2, int _MSS,float _dropProb) throws UnknownHostException, SocketException{
		ID = _ID;
		port = 5000 + _ID;
		succID1 = _succID1;
		succPort1 = _succID1 + 5000;
		succID2 = _succID2;
		succPort2 = _succID2 + 5000;
		MSS = _MSS;
		dropProb = _dropProb;
		host = InetAddress.getByName("127.0.0.1");
		socket = new DatagramSocket(port, host);
		//TIMEOUT FOR UDP SOCKET IS 1 SECOND
		socket.setSoTimeout(1000);
		//unknown at this time
		predPort1 = 0;
		predPort2 = 0;
	}
	//sends a peerToPeerPacket object via datagram packet to port (int destination)
	public void sendObject(peerToPeerPacket obj, cdht me, int destination) throws IOException {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		oo = new ObjectOutputStream(bStream);
		oo.writeObject(obj);
		oo.close();				
		byte[] serializedPacket = bStream.toByteArray();
		DatagramPacket pingPacket = new DatagramPacket(serializedPacket, serializedPacket.length, me.host, destination);
		me.socket.send(pingPacket);
	}
	
	//sets predecessor ports
	public void setPredPort(int pingSource, cdht me) {
		if(pingSource >= me.predPort1 && pingSource < me.port) {
			me.predPort1 = pingSource;
		}else if(pingSource < me.predPort1 && pingSource >= me.predPort2) {
			me.predPort2 = pingSource;
		}
	}
	//processes file request
	//forwards it to first successor
	//sets youAreOwner to true if first successor is the responder
	public void processRequest(fileRequestPacket r, cdht me) throws IOException {		
		//if successor 1 is the recipient of the packet, change youAreOwner boolean to true 
		if(me.succPort1 == r.destPort || (me.port < r.destPort && r.destPort < me.succPort1)){
			//notifies that successor 1 should respond to request and not forward the request
			r.setResponder();
		}
		System.out.println("File "+r.fileName+" is not stored here.");
		//for
		me.sendObject(r, me, succPort1);
		System.out.println("File request message has been forwarded to my successor.");
	
	}
	
	
	public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException {
		if(args.length < 5) {
			System.out.println("Usage: <ID> <successor ID> <successor ID> <MSS> <drop probability>");
			return;
		}
		if(Float.parseFloat(args[4]) > 0 && Float.parseFloat(args[4])<1) {
			System.out.println("Usage: 0 < drop probability < 1");
		}
		try{
			Integer.parseInt(args[0]);
			Integer.parseInt(args[1]);
			Integer.parseInt(args[2]);
			Integer.parseInt(args[3]);
			Float.parseFloat(args[4]);
		}catch(NumberFormatException e){
			System.out.println("Usage: <int: ID> <int: successor ID> <int: successor ID> <int: MSS> <float: drop probability>");
		}
		//instantiate the client
		cdht me = new cdht(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Float.parseFloat(args[4]));
		//instantiate and run user input handler
		userInputHandler uih = new userInputHandler(me);
		Thread uihThread = new Thread(uih);
		uihThread.start();
		//instantiate and run two separate pingHandlers
		pingHandler pinger1 = new pingHandler(me, me.succPort1);
		pingHandler pinger2 = new pingHandler(me, me.succPort2);
		Thread pingThread1 = new Thread(pinger1);
		//pingThread1.start();
		Thread pingThread2 = new Thread(pinger2);
		//pingThread2.start();
		//init fileTransferHandler
		fileTransferHandler fth = null;
		Thread fthThread = null;
		//init fileData buffer for received file chunks
		byte[] fileData = null;
		//init fileReceiveHandler
		fileReceiveHandler frh = null;
		//main loop for receiving any and all packets
		while(true) {
			//given the types of packets being processed in cdht, the max size of any packet will be 200+MSS
			DatagramPacket receivedPacket = new DatagramPacket(new byte[me.MSS+200], me.MSS+200);
			//receive datagram packet
			try {
				me.socket.receive(receivedPacket);
			}catch (SocketTimeoutException e) {
				continue;
			}
			//add a time sent to each packet!!!!!!!!!!!!!!!!!!!!!!!!!
			//convert the packet to an object to extract header
			ObjectInputStream socketStream = new ObjectInputStream(new ByteArrayInputStream(receivedPacket.getData()));
			peerToPeerPacket receivedPacketObject = (peerToPeerPacket) socketStream.readObject();
			socketStream.close();
			
			//receives ping? response
			if(receivedPacketObject.type.equals("ping")) {
				System.out.println("		Rec ping from "+receivedPacket.getPort());
				if(receivedPacketObject.sourcePort == me.succPort1) {
					//pinger1 stops waiting for a ping
					System.out.println("Receives ping response "+me.succPort1+" in the loop");
					pinger1.stopWaiting();
				}else if(receivedPacketObject.sourcePort == me.succPort2) {
					//pinger2 stops waiting for a ping
					System.out.println("Receives ping response "+me.succPort2+" in the loop");
					pinger2.stopWaiting();
				}
			//receives ping? request, sends a response ping
			//sets predecessor ports based on the source of the ping?
			//for some reason, after the first successful ping session a client will receive a ping response from a server they haven't pinged yet??
			}else if(receivedPacketObject.type.equals("ping?")) {
				System.out.println("Rec ping? from "+receivedPacket.getPort());
				me.setPredPort(receivedPacketObject.sourcePort, me);
				peerToPeerPacket ping = new peerToPeerPacket("ping", me.port, receivedPacketObject.sourcePort);
				//send response ping
				me.sendObject(ping, me, receivedPacketObject.sourcePort);
				System.out.println("Sent ping to "+receivedPacket.getPort());
			
			//receives a file request packet
			}else if(receivedPacketObject.type.equals("freq?")) {
				fileRequestPacket freq = (fileRequestPacket) receivedPacketObject;
				//if client has the file
				if(freq.amIResponder()) {
					//respond to request, setup TCP connection
					fth = new fileTransferHandler(me, freq);
					fthThread = new Thread(fth);
					//start thread to send requested file
					fthThread.start();
				//else forward request to successor
				}else{
					me.processRequest(freq, me);
				}
			//receives a file response packet (contains fileChunk)
			}else if(receivedPacketObject.type.equals("fres")){
				fileResponsePacket fres = (fileResponsePacket) receivedPacketObject;
				//if first packet for TCP connection, start new thread
				if(fres.seqNum == 0) {
					frh = new fileReceiveHandler(me, fres);
					Thread frhThread = new Thread(frh);
					frhThread.start();
				}else {
					frh.currPacket = fres;
				}
			//receives an ACK packet, calls stopWaiting function if expected
			}else if(receivedPacketObject.type.equals("ack")) {
				ackPacket ack = (ackPacket) receivedPacketObject;
				//test if ACK is in order
				if(fth != null && fth.getWaitingStatus() == true && ack.ackNum == fth.getAwaitingSeqNum()){
					fth.stopWaiting();
				}else {
					System.out.println("We either received an out of order ACK or client wasn't awaiting an ACK");
				}
			//receives peer departure packet		
			}else if(receivedPacketObject.type.equals("dep")) {
				departurePacket dep = (departurePacket) receivedPacketObject;
				//update successor ports
				if(dep.sourcePort == me.succPort1) {
					me.succPort1 = dep.mySucc1;
					me.succPort2 = dep.mySucc2;
				}else if(dep.sourcePort == me.succPort2) {
					me.succPort2 = dep.mySucc1;
				}
				//ack departure message
				ackPacket depack = new ackPacket("depack", me.port, dep.sourcePort,-1);
				
			//receives an ACK for departure
			}else if(receivedPacketObject.type.equals("depack")){
				//can now terminate receiver loop
				break;
			}
			receivedPacket = null;
			receivedPacketObject = null;
		}
	}
}

/*
// Serialize to a byte array and send
ByteArrayOutputStream bStream = new ByteArrayOutputStream();
ObjectOutput oo = new ObjectOutputStream(bStream); 
oo.writeObject(pingPacket);
oo.close();

byte[] serializedPacket = bStream.toByteArray();
DatagramPacket ping = new DatagramPacket(serializedPacket, serializedPacket.length, me.host, me.succPort1);
me.socket.send(ping);
 * 
 * 
 * 
//Receive a byte array and convert to object
ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(recBytes));
Message messageClass = (Message) iStream.readObject();
iStream.close();
 */
