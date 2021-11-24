import java.util.ArrayList;
import java.util.Arrays;

public class IPLayer implements BaseLayer {

	private static final int HEADER_SIZE = 20;
	private static final int MAX_SIZE = 1480;
	private static final int MAX_OFFSET = 185;

	// ----- Properties -----
	private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    private _IP m_sHeader;

	private byte[] packetAccumulator;
	private Logger logging;
	
	private int ID = 0;

	// ----- Constructor -----
    public IPLayer(String pName) {
		pLayerName = pName;
		m_sHeader = new _IP();
		logging = new Logger(this);
	}
    
    // ----- Routing Structures -----
    private class _Routing_Structures {
    	String Dst_ip_addr = null;
    	String Subnet_mask = null;
    	String Gateway = null;
    	String Flag = null;
    	String Interface = null;
    	String metric;

		public _Routing_Structures(String Dst_ip_addr, String Subnet_mask, String Gateway, String Flag, String Interface, String metric) {
            this.Dst_ip_addr = Dst_ip_addr;
            this.Subnet_mask = Subnet_mask;
            this.Gateway = Gateway;
            this.Flag = Flag;
            this.Interface = Interface;
            this.metric = metric;
        }
    }
    
	// ----- Structures -----
    private class _IP_ADDR {
        byte[] addr = new byte[4];

        public _IP_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
        }
    }
     
	private class _IP {			// byte	| description					| usage
		byte ip_verlen;			// 1	| IP version and header length	| !NOT USED!
		byte ip_tos;			// 1	| Type of service.				| !NOT USED!
		byte[] ip_len;			// 2	| Total packet length			| 
		byte[] ip_id;			// 2	| Datagram id					| !NOT USED!
		byte[] ip_flag_fragoff;	// 1	| Fragment flag	& frag offset	| 
		byte ip_ttl;			// 1	| Time to live in gateway hops	| !NOT USED!
		byte ip_proto;			// 1	| IP Protocol					| !NOT USED!
		byte[] ip_cksum;		// 2	| Header checksum				| !NOT USED!
		_IP_ADDR ip_src;		// 4	| Source IP address				| 
		_IP_ADDR ip_dst;		// 4	| Destination IP address		| 
		byte[] ip_data; 		// n	| Data of packet				| 


		public _IP() {
			ip_verlen = 0x45;				// !NOT USED! - IPv4, 5 * 32 = 160bit(20byte header)
			ip_tos = 0x00;					// !NOT USED! - 0 -> Normal packet
			ip_len = new byte[2];
			ip_id = new byte[2];
			ip_flag_fragoff = new byte[2];
			ip_ttl = 0x00;					// !NOT USED!
			ip_proto = 0x00;				// !NOT USED!
			ip_cksum = new byte[2];			// !NOT USED!
			ip_src = new _IP_ADDR();
			ip_dst = new _IP_ADDR();
			ip_data = null;
		}
	}
	
	// ----- Rout function -----
	public boolean receive(byte[] input) {
		/*
		 input 패킷 뜯어서 목적지 ip 체크 -> ping packet 구조 알아야됨
		 routing table 확인
		  해당 network port, gateway ip 확인 ( 직접 연결 됐으면 ㄴ )
		 ip에 해당하는 arp table 뒤적 
		 arp 없으면 arp request 후 reply 될 때까지 ㄱㄷ
		 dst mac addr와 같이 send -> dst addr은 ether에서 header로 적을듯 
		 */
		
		// 1.address
		byte[] srcIpAddr = null;
		byte[] dstIpAddr = Arrays.copyOfRange(input, 30, 34);
		byte[] directTransferIp = null;
		byte[] directTransferMac = null;
		
		// *.if dstIP_Addr is me, do nothing
		
		// 2.matchedRout
		ArrayList<String> matchedRoutStr = ((RoutingTable) this.getUpperLayer(0)).getMatchedRout(dstIpAddr);
		_Routing_Structures matchedRout = new _Routing_Structures(matchedRoutStr.get(0), matchedRoutStr.get(1), matchedRoutStr.get(2), matchedRoutStr.get(3), matchedRoutStr.get(4), matchedRoutStr.get(5));

		// 3.Flag
		// portNum not determined
		int portNum;
		if(matchedRout.Flag.equals("U")) {
			// i don't read a book
		}
		else if(matchedRout.Flag.equals("UG")) {
			directTransferIp = StringToByte(matchedRout.Gateway);
		}
		else if(matchedRout.Flag.equals("UH")) {
			directTransferIp = dstIpAddr;
		}
		
		// 4.ARP check : byte[] srcIpAddr = null; // ** not complete **
		directTransferMac = ((ARPLayer) RouterDlg.m_LayerMgr.getLayer("ARPLayer")).getDstMac(srcIpAddr, directTransferIp);
		
		// 5.send
		if (matchedRout.metric.equals("")) {
			return this.send(input, input.length, directTransferMac);
		}
		else if (matchedRout.metric.equals("")) {
			return ((IPLayer) this.getUpperLayer(1)).send(input, input.length, directTransferMac);
		}
		
		return false;
	}
	
	// ----- send -----
	public boolean send(byte[] input, int length, byte[] directTransferMac) {
		((EthernetLayer)this.getUnderLayer()).RouterSend(input, input.length, directTransferMac);
		return true;
	}
	
	// ----- Private Methods -----
	private byte[] intToByte2(int value) {
		byte[] temp = new byte[2];
		temp[0] |= (byte) ((value & 0xFF00) >> 8);
		temp[1] |= (byte) (value & 0xFF);

		return temp;
	}

	private int byte2ToInt(byte value1, byte value2) {
		return (int) (((value1 & 0xff) << 8) | (value2 & 0xff));
	}
	
	// ----- StringToByte -----
	private byte[] StringToByte(String data) {
		String[] strArr = data.split("[.]");
		if(strArr.length != 4) { return null; }
		byte[] byteIpAddr = new byte[4];
		for(int i = 0; i < 4; i++) {
			byteIpAddr[i] = (byte)Integer.parseInt(strArr[i]);
		}
		return byteIpAddr;
	}
	
	// ----- ByteToString -----
	private String ByteToString(byte[] data) {
		return String.format("%d.%d.%d.%d", (data[0] & 0xff), (data[1] & 0xff), (data[2] & 0xff), (data[3] & 0xff));
	}
	

    private byte[] removeIPHeader(byte[] input, int length) {
	    byte[] cpyInput = new byte[length - HEADER_SIZE];
	    System.arraycopy(input, HEADER_SIZE, cpyInput, 0, length - HEADER_SIZE);
		return cpyInput;
	}

	private boolean dstIsMe(_IP_ADDR dst) {
		for (int i = 0; i < 4; i++) {
			if (m_sHeader.ip_src.addr[i] != dst.addr[i])
				return false;
		} 	
		return true;
	}
	private boolean srcIsMe(_IP_ADDR src) {
		for(int i = 0; i < 4; i++)
			if(m_sHeader.ip_src.addr[i] != src.addr[i])
				return false;
		return true;
	}
	
	// ----- Convert methods -----
	private byte[] objToByte(_IP Header, byte[] input, int length) {//data ��  �� �� 붙여주기
	    byte[] buf = new byte[length + HEADER_SIZE];

		// Copy header
		buf[0] = Header.ip_verlen;
		buf[1] = Header.ip_tos;
		System.arraycopy(Header.ip_len, 0, buf, 2, 2);
		System.arraycopy(Header.ip_id, 0, buf, 4, 2);
		System.arraycopy(Header.ip_flag_fragoff, 0, buf, 6, 2);
		buf[8] = Header.ip_ttl;
		buf[9] = Header.ip_proto;
		System.arraycopy(Header.ip_cksum, 0, buf, 10, 2);
		System.arraycopy(Header.ip_src.addr, 0, buf, 12, 4);
		System.arraycopy(Header.ip_dst.addr, 0, buf, 16, 4);

		// Copy data
		System.arraycopy(input, 0, buf, HEADER_SIZE, length);

		return buf;
	}

	private _IP byteToObj(byte[] input, int length) {
		_IP temp = new _IP();

		// Copy header
		temp.ip_verlen = input[0];
		temp.ip_tos = input[1];
		System.arraycopy(input, 2, temp.ip_len, 0, 2);
		System.arraycopy(input, 4, temp.ip_id, 0, 2);
		System.arraycopy(input, 6, temp.ip_flag_fragoff, 0, 2);
		temp.ip_ttl = input[8];
		temp.ip_proto = input[9];
		System.arraycopy(input, 10, temp.ip_cksum, 0, 2);
		System.arraycopy(input, 12, temp.ip_src.addr, 0, 4);
		System.arraycopy(input, 16, temp.ip_dst.addr, 0, 4);

		// Copy data
		temp.ip_data = this.removeIPHeader(input, length);

		return temp;
	}

	// ----- Getters & Setters -----
	public void setIPSrcAddress(byte[] srcAddress) {
	    m_sHeader.ip_src.addr = srcAddress;
	}

	public void setIPDstAddress(byte[] dstAddress) {
	    m_sHeader.ip_dst.addr = dstAddress;
	}

	public byte[] getIPSrcAddress() {
		return m_sHeader.ip_src.addr;
	}

	public byte[] getIPDstAddress() {
		return m_sHeader.ip_dst.addr;
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
