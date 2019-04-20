import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class fileTransferHandler implements Runnable{
	public cdht me;
	public fileRequestPacket obj;
	public boolean awaitingAck = false;
	public int awaitingSeqNum;
	
	public fileTransferHandler(cdht _me, fileRequestPacket _obj) {
		me = _me;
		obj = _obj;
	}
	
	public void stopWaiting() {
		awaitingAck = false;
	}
	
	public void startWaiting() {
		awaitingAck = true;
	}
	
	public boolean getWaitingStatus() {
		return awaitingAck;
	}
	
	public int getAwaitingSeqNum() {
		return awaitingSeqNum;
	}
	
	public void setAwaitingSeqNum(int s) {
		awaitingSeqNum = s;
	}
	
	//function to abstracting the feature for waiting for an ACK
	//waits for an ACK, will resend fileResponsePacket if no ACK received within 1 second
	public void waitForAck(int seqNum, fileResponsePacket frp) {
		startWaiting();
		setAwaitingSeqNum(seqNum);
		long lastPacketTime = System.currentTimeMillis();
		while(getWaitingStatus() == true) {
			if((System.currentTimeMillis() - lastPacketTime) >= 1000) {
				System.out.println("I HAVE TIMED OUT WAITING FOR CLIENT "+obj.sourcePort+" TO SEND ACK: "+seqNum);
				System.out.println("Resending packet: "+seqNum);
				//resend packet and reset timer
				try {
					me.sendObject(frp, me, obj.sourcePort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				lastPacketTime = System.currentTimeMillis();
			}
		}
	}
	
	
	public byte[] fileToByteArray(String fileName) throws IOException {
		FileInputStream fin = new FileInputStream(fileName);
		byte[] buffer = new byte[8192];
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    int bytesRead;
	    while ((bytesRead = fin.read(buffer)) != -1)
	    {
	        baos.write(buffer, 0, bytesRead);
	    }
	    return baos.toByteArray();
	}
	
	@Override
	public void run() {
		//construct fileResponse object
		fileResponsePacket frp = null;
		byte[] wholeFile = null;
		try {
			wholeFile = fileToByteArray(obj.fileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("File "+obj.fileName+" is here.");
		System.out.println("A response message, destined for peer "+obj.sourcePort+", has been sent.");
		System.out.println("We now start sending the file .........");
		//i increments by MSS
		//i also represents the sequence number of each packet (byte index)
		for(int i = 0; i < wholeFile.length; i += me.MSS) {
			byte[] currentChunk = null;
			//if currentChunk is the last chunk of the file
			if((i+me.MSS) > wholeFile.length) {
				currentChunk = Arrays.copyOfRange(wholeFile, i, wholeFile.length);
			}else {
				currentChunk = Arrays.copyOfRange(wholeFile, i, i+me.MSS);
			}	
			frp = new fileResponsePacket("fres", me.port, obj.sourcePort, currentChunk, i);
			
			//if currentChunk is the last chunk, set lastPacket in frp to be true
			if((i+me.MSS) > wholeFile.length) {
				frp.setLast();
				//(loop should terminate since the invariant wont be satisfied)
			}
			try {
				me.sendObject(frp, me, obj.sourcePort);
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
			//waits for an ACK, will resend fileResponsePacket frp if no ACK received within 1 second
			waitForAck(i, frp);
			
		}
	}

}
