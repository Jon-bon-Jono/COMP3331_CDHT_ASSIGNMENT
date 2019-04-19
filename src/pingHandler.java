import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicBoolean;

public class pingHandler implements Runnable {
	public cdht me;
	public int pingDest;
	public volatile boolean awaitingPing = false;
	DatagramPacket pingRequestPacket;

	public pingHandler(cdht _me, int _pingDest) throws IOException {
		me = _me;
		pingDest = _pingDest;
		//create datagram packet with ping object in it
		peerToPeerPacket ping = new peerToPeerPacket("ping?", me.port, pingDest);
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		oo = new ObjectOutputStream(bStream);
		oo.writeObject(ping);
		oo.close();				
		byte[] serializedPacket = bStream.toByteArray();
		pingRequestPacket = new DatagramPacket(serializedPacket, serializedPacket.length, me.host, pingDest);
	}
	public void stopWaiting() {
		awaitingPing = false;
	}
	
	//send either a ping request or ping response to the specified destination
	public void sendPing() throws IOException {
		me.socket.send(pingRequestPacket);
	}
	
	//sends a single ping
	@Override
	public void run() {
		//sends a ping request and waits 8seconds for a response
		//if no response received in time calls for a return of the DHT
		//if response received in time exits thread
		long lastPingTime = System.currentTimeMillis();
		pingLoop:
		while(true) {
			if((float)(System.currentTimeMillis() - lastPingTime)/1000F >= 10) {
				//send ping?
				try {
					sendPing();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("		Sent ping? to "+pingDest);
				awaitingPing = true;
				long pingSentTime = System.currentTimeMillis();
				//wait for a response
				while(awaitingPing == true) {
					if((System.currentTimeMillis() - pingSentTime) >= 5000) {
						System.out.println("CLIENT "+(pingDest-5000)+" HAS LEFT THE BUILDING");
						break pingLoop;
					}
				}
				System.out.println("Client "+(pingDest-5000)+" is still alive:::::waited "+((float)(System.currentTimeMillis() - pingSentTime)/1000F)+" seconds");
				lastPingTime = System.currentTimeMillis();
			}
		}System.out.println("Thread "+pingDest+" is over");
	}
}
