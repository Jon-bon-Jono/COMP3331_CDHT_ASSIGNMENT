/*types include 
 * ping request: "ping?"
 * ping response: "ping"
 * file request "freq?"
 */
public class peerToPeerPacket implements java.io.Serializable {
	public String type;
	//these become most useful for file requests
	public int sourcePort;
	public int destPort;
	
	public peerToPeerPacket(String _type, int _sourcePort, int _destPort) {
		type = _type;
		sourcePort = _sourcePort;
		destPort = _destPort;
	}
}
