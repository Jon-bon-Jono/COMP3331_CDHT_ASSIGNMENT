//type: freq?
public class fileRequestPacket extends peerToPeerPacket implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public String fileName;
	//indicates to the recipient of this packet whether they have the file
	private boolean isResponder = false;
	
	public fileRequestPacket(String _type, int _sourcePort, int _destPort, String _fileName) {
		super(_type, _sourcePort, _destPort);
		fileName = _fileName;
	}
	
	public boolean amIResponder() {
		return isResponder;
	}
	
	public void setResponder() {
		isResponder = true;
	}

}
