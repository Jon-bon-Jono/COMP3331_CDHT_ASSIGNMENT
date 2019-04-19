//type: "fres"
public class fileResponsePacket extends peerToPeerPacket implements java.io.Serializable {

	public byte[] fileChunk;
	private boolean lastPacket = false;
	
	public fileResponsePacket(String _type, int _sourcePort, int _destPort, byte[] _fileChunk) {
		super(_type, _sourcePort, _destPort);
		fileChunk = _fileChunk;
	}
	
	public boolean isLast() {
		return lastPacket;
	}
	public void setLast() {
		lastPacket = true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
