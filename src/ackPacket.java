//type: "ack"
//or type: "depack" (acknowledges graceful departure)
public class ackPacket extends peerToPeerPacket implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	public int ackNum;
	
	public ackPacket(String _type, int _sourcePort, int _destPort, int _ackNum) {
		super(_type, _sourcePort, _destPort);
		ackNum = _ackNum;
	}
}
