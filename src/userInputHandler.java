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
	        	
	        }
		}
	}

}
