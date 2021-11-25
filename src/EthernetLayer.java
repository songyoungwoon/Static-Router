
import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {

	private static final int HEADER_SIZE = 14;
	private static final int DATA_TYPE = 0x0800;
	private static final int ARP_TYPE = 0x0806;
	private static final byte[] BROADCAST = new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};

	// ----- Properties -----
	private int nUpperLayerCount = 0;
	private String pLayerName = null;
	private BaseLayer p_UnderLayer = null;
	private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private _ETHERNET_Frame m_sHeader;

	private Logger logging;

	// ----- Constructor -----
	public EthernetLayer(String pName) {
		pLayerName = pName;
		m_sHeader = new _ETHERNET_Frame();
		logging = new Logger(this);
	}

	// ----- Structure -----
	private class _ETHERNET_ADDR {
		byte[] addr = new byte[6];

		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	private class _ETHERNET_Frame {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_Frame() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	// ----- Methods -----
	
	// ----- RouterSend -----
	// ----- TODO : send Routing data -----
	// check portNum, attach header (DST, SRC MacAddr)
	public boolean send(byte[] input, int length, byte[] dstIP) {
		setEnetSrcAddress(((NILayer)this.getUnderLayer()).getAdapterMAC());
		byte[] dstMac = ((ARPLayer) this.getUpperLayer(1)).getDstMac(dstIP);
		if(dstMac == null) { return false; }
		setEnetDstAddress(dstMac);
		setEnetType(DATA_TYPE);
		logging.log("Send data");
		byte[] bytes = objToByte(m_sHeader, input, length, false);
		return ((NILayer) this.getUnderLayer()).send(bytes, length + HEADER_SIZE);
	}
	
	// Sending
	public boolean sendARP(byte[] input, int length) {
		setEnetSrcAddress(((NILayer)this.getUnderLayer()).getAdapterMAC());
		setEnetDstAddress(BROADCAST);
		setEnetType(ARP_TYPE);
		logging.log("Send ARP");
		byte[] bytes = objToByte(m_sHeader, input, length, false);
		return ((NILayer)this.getUnderLayer()).send(bytes, length + HEADER_SIZE);
	}

	// Receiving
	private byte[] removeEthernetHeader(byte[] input, int length) {
		byte[] cpyInput = new byte[length - HEADER_SIZE];
		System.arraycopy(input, HEADER_SIZE, cpyInput, 0, length - HEADER_SIZE);
		input = cpyInput;
		return input;
	}

	private String macBtoS(byte[] b) {
		return String.format("%02X-%02X-%02X-%02X-%02X-%02X", b[0], b[1], b[2], b[3], b[4], b[5]);
	}
	
	// ----- TODO : check ARP OR ROUT -----
	// check ethernet type in ethernet header
	// 0x0800 : rout, 0x0806 : arp
	public synchronized boolean receive(byte[] input) {
		_ETHERNET_Frame received = this.byteToObj(input, input.length);
		logging.log("received from " + macBtoS(received.enet_srcaddr.addr) + " to " + macBtoS(received.enet_dstaddr.addr));
		int frameType = byte2ToInt(received.enet_type[0], received.enet_type[1]);
		
		if(frameType == ARP_TYPE) {
			logging.log("Receive ARP frame");
			return ((ARPLayer) this.getUpperLayer(1)).receive(received.enet_data);
		}

		if(srcIsMe(received.enet_srcaddr)) {
			logging.log("Frame rejected: Sent by this host");
			return false;
		}
		
		if(isBroadcast(received.enet_dstaddr)) {

			
			logging.log("Frame rejected: Unknown broadcast");
			return false;
		}

		if(!dstIsMe(received.enet_dstaddr)) {
			System.out.println(macBtoS(m_sHeader.enet_srcaddr.addr));
			System.out.println(macBtoS(received.enet_dstaddr.addr));
			logging.log("Frame rejected: Not sent to this host");
			return false;
		}
	
		if (frameType == DATA_TYPE) {
			logging.log("Receive data frame");
			return ((IPLayer) this.getUpperLayer(0)).receive(received.enet_data);
		}
		return false;		
	}

	// ----- Convert methods -----
	public byte[] objToByte(_ETHERNET_Frame Header, byte[] input, int length, boolean broadTrue) {
		byte[] buf = new byte[length + HEADER_SIZE];
		for (int i = 0; i < 6; i++) {
			if (broadTrue == true) {
				buf[i] = (byte) 0xff;
			} else {
				buf[i] = Header.enet_dstaddr.addr[i];
			}
			buf[i + 6] = Header.enet_srcaddr.addr[i];
		}
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++)
			buf[HEADER_SIZE + i] = input[i];

		return buf;
	}

	private _ETHERNET_Frame byteToObj(byte[] input, int length) {
		_ETHERNET_Frame temp = new _ETHERNET_Frame();
		for (int i = 0; i < 6; i++) {
			temp.enet_dstaddr.addr[i] = input[i];
			temp.enet_srcaddr.addr[i] = input[6 + i];
		}
		System.arraycopy(input, 12, temp.enet_type, 0, 2);
		temp.enet_data = this.removeEthernetHeader(input, length);

		return temp;
	}

	private byte[] intToByte2(int value) {
		byte[] temp = new byte[2];
		temp[0] |= (byte) ((value & 0xFF00) >> 8);
		temp[1] |= (byte) (value & 0xFF);

		return temp;
	}

	private int byte2ToInt(byte value1, byte value2) {
		return (int) (((value1 & 0xff) << 8) | (value2 & 0xff));
	}

	// ----- Boolean methods -----
	private boolean dstIsMe(_ETHERNET_ADDR dst) {
		for (int i = 0; i < 6; i++)
			if (m_sHeader.enet_srcaddr.addr[i] != dst.addr[i])
				return false;
		return true;
	}

	private boolean srcIsMe(_ETHERNET_ADDR src) {
		for (int i = 0; i < 6; i++)
			if (m_sHeader.enet_srcaddr.addr[i] != src.addr[i])
				return false;
		return true;
	}

	private boolean isBroadcast(_ETHERNET_ADDR dst) {
		for (int i = 0; i < 6; i++)
			if (dst.addr[i] != (byte) 0xff)
				return false;
		return true;
	}

	// ----- Getters & Setters -----
	private void setEnetType(int type) {
		m_sHeader.enet_type = intToByte2(type);
	}

	public void setEnetSrcAddress(byte[] srcAddress) {
		m_sHeader.enet_srcaddr.addr = srcAddress;
	}

	public void setEnetDstAddress(byte[] dstAddress) {
		m_sHeader.enet_dstaddr.addr = dstAddress;
	}

	public byte[] getEnetSrcAddress() {
		return m_sHeader.enet_srcaddr.addr;
	}

	public byte[] getEnetDstAddress() {
		return m_sHeader.enet_dstaddr.addr;
	}

	@Override
	public String getLayerName() {
		return pLayerName;
	}

	@Override
	public BaseLayer getUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer getUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void setUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void setUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}

	@Override
	public void setUpperUnderLayer(BaseLayer pUULayer) {
		this.setUpperLayer(pUULayer);
		pUULayer.setUnderLayer(this);
	}
}