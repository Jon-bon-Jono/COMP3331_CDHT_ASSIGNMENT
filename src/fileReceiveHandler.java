import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class fileReceiveHandler implements Runnable{
	public fileResponsePacket currPacket;
	public boolean isExpecting;
	public int expectedSeqNum;
	public cdht me;
	public boolean isFinished;
	public byte[] fileData;
	
	public fileReceiveHandler(cdht _me, fileResponsePacket firstPacket) {
		me = _me;
		currPacket = firstPacket;
		isExpecting = true;
		expectedSeqNum = 0;
		isFinished = false;
		fileData = null;
	}
	
	public static byte[] append(byte[] a1, byte[] a2) {
	    byte [] ret = new byte[a1.length + a2.length];
	    System.arraycopy(a1, 0, ret, 0, a1.length);
	    System.arraycopy(a2, 0, ret, a1.length, a2.length);
	    return ret;
	}
	
	public void writeBytesToFile() {
		OutputStream out;
		try {       
	        out = new FileOutputStream(me.ID+"_received.pdf");
	        out.write(fileData, 0, fileData.length);
	        //out.position = 0;
	        out.close();
	        System.out.println("The file is received.");
	    }catch (Exception e) {
	        System.out.println(e);
	    }
	}
	
	@Override
	public void run() {
		while(true) {
			//check if it is expecting currPacket
			if(currPacket.seqNum == expectedSeqNum) {
				//send ACK
				ackPacket ack = new ackPacket("ack", me.port, currPacket.sourcePort, me.expectedSeqNum);
				try {
					me.sendObject(ack, me, currPacket.sourcePort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//append file data
				if(currPacket.seqNum == 0) {
					fileData = currPacket.fileChunk;
				}else {
					fileData = append(fileData, currPacket.fileChunk);
				}
				
				//if last packet is received, write data to file
				if(currPacket.isLast() == true) {
					writeBytesToFile();
					break;
				}
				
				expectedSeqNum = expectedSeqNum + me.MSS;
				
			}
		}
	}

}
