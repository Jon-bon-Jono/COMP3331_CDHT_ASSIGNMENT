import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class fileTransferHandler implements Runnable{
	public cdht me;
	public fileRequestPacket obj;
	
	public fileTransferHandler(cdht _me, fileRequestPacket _obj) {
		me = _me;
		obj = _obj;
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
		for(int i = 0; i < wholeFile.length; i += me.MSS) {
			byte[] currentChunk = null;
			//if currentChunk is the last chunk of the file
			if((i+me.MSS) > wholeFile.length) {
				currentChunk = Arrays.copyOfRange(wholeFile, i, wholeFile.length);
			}else {
				currentChunk = Arrays.copyOfRange(wholeFile, i, i+me.MSS);
			}	
			frp = new fileResponsePacket("fres", me.port, obj.sourcePort, currentChunk);
			
			//if currentChunk is the last chunk, set lastPacket in frp to be true
			if((i+me.MSS) > wholeFile.length) {
				frp.setLast();
			}
			try {
				me.sendObject(frp, me, obj.sourcePort);
			} catch (IOException e) {
				e.printStackTrace();
			}	
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
