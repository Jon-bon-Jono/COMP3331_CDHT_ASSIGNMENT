import java.io.IOException;
import java.util.Scanner;

public class userInputHandler implements Runnable{
	cdht me;
	
	public userInputHandler(cdht _me) {
		me = _me;
	}
	@Override
	public void run() {
		while(true) {
			Scanner scanner = new Scanner(System. in);
	        String inputString = scanner. nextLine(); 
	        String arr[] = inputString.split(" ", 3);
	        //request command
	        if(arr.length == 3) {
	        	String fileName = arr[1];
	        	String ownerPeer = arr[2];
	        	fileRequestPacket fr = new fileRequestPacket("freq?", me.port , Integer.parseInt(ownerPeer), fileName);
	        	System.out.println("File request message for "+fileName+" has been sent to my successor.");
	        	try {
	        		//cdht is now expecting a response packet with seqNum = 0
	        		me.expectedSeqNum = 0;
					me.processRequest(fr, me);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        //quit command
	        }else if(arr.length == 1 && arr[1].equals("quit")) {
	        	//create a 2 dep packets and send them to respective predecessors
	        	departurePacket dep1 = new departurePacket("dep", me.port, me.predPort1, 1, me.succPort1, me.succPort2);
	        	departurePacket dep2 = new departurePacket("dep", me.port, me.predPort2, 2, me.succPort1, me.succPort2);
	        	try {
					me.sendObject(dep1, me, me.predPort1);
		        	me.sendObject(dep2, me, me.predPort2);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	//should wait for an departure ack
	        }
		}
	}

}
