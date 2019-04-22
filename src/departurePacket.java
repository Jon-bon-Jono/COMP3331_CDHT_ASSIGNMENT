//type: "dep"
public class departurePacket extends peerToPeerPacket implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int mySucc1;
	public int mySucc2;
	//predReceiver == 1 gets sent to predecessor 1
	//predReceiver == 2 gets sent to predecessor 2
	public int predReceiver;
	
	public departurePacket(String _type, int _sourcePort, int _destPort, int _predReceiver, int _mySucc1, int _mySucc2) {
		super(_type, _sourcePort, _destPort);
		mySucc1 = _mySucc1;
		mySucc2 = _mySucc2;
		predReceiver = _predReceiver;
	}


}
