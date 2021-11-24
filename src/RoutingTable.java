import java.util.ArrayList;
import java.util.Arrays;

public class RoutingTable implements BaseLayer {
	// ----- Properties -----
	private int nUnderLayerCount = 0;
	private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	// ----- Routing Table -----
    public ArrayList<_Routing_Structures> routingTable = new ArrayList<>();
    
    // ----- Constructor -----
    public RoutingTable(String pName) {
		pLayerName = pName;
	}
    
    // ----- Routing Structures -----
    private class _Routing_Structures {
    	String Dst_ip_addr = null;
    	String Subnet_mask = null;
    	String Gateway = null;
    	String Flag = null;
    	String Interface = null;
    	
		public _Routing_Structures(String Dst_ip_addr, String Subnet_mask, String Gateway, String Flag, String Interface) {
            this.Dst_ip_addr = Dst_ip_addr;
            this.Subnet_mask = Subnet_mask;
            this.Gateway = Gateway;
            this.Flag = Flag;
            this.Interface = Interface;
        }
    }
    
	// ----- getPortNum -----
	public String getPortNum(byte[] srcIpAddr) {
		for (_Routing_Structures routingTableEntry : routingTable) {
			// ----- dstIpAddr & rout.Subnet_mask ----- 
			byte [] SubnetMask = StringToByte(routingTableEntry.Subnet_mask);
		    srcIpAddr = CalDstAndSub(srcIpAddr, SubnetMask);
			// check rout.Destination Address
			if (routingTableEntry.Dst_ip_addr.equals(ByteToString(srcIpAddr))) {
				return routingTableEntry.Interface;
			}
		}
		return null;
	}
    
	public boolean addRoutingTableEntry(String Dst_ip_addr, String Subnet_mask, String Gateway, String Flag, String Interface) {
        return routingTable.add(new _Routing_Structures(Dst_ip_addr, Subnet_mask, Gateway, Flag, Interface));
    }

	public void deleteRoutingTableEntry() {
        routingTable.clear();
    }

    public boolean rout(byte[] input) {
    	/*
		 input 패킷 뜯어서 목적지 ip 체크 -> ping packet 구조 알아야됨
		 routing table 확인
		  해당 network port, gateway ip 확인 ( 직접 연결 됐으면 ㄴ )
		 ip에 해당하는 arp table 뒤적
		 arp 없으면 arp request 후 reply 될 때까지 ㄱㄷ
		 dst mac addr와 같이 send -> dst addr은 ether에서 header로 적을듯
		 */

		// 1.address
		byte[] srcIpAddr = null; //empty for now
		byte[] dstIpAddr = Arrays.copyOfRange(input, 30, 34);
		byte[] directTransferIp = null;

		// *.if dstIP_Addr is me, do nothing

		// 2.matchedRout
		_Routing_Structures matchedRout = getMatchedRout(dstIpAddr);

		// 3.Flag
		// portNum not determined
		if(matchedRout.Flag.equals("U")) {
			// i don't read a book
		}
		else if(matchedRout.Flag.equals("UG")) {
			directTransferIp = StringToByte(matchedRout.Gateway);
		}
		else if(matchedRout.Flag.equals("UH")) {
			directTransferIp = dstIpAddr;
		}

		// 5.send
		String[] temp = matchedRout.Interface.split("_");
		int portNum = Integer.parseInt(temp[1]);
		
		return ((IPLayer)this.getUnderLayer(portNum - 1)).send(input, input.length, directTransferIp);
		
	}
	
	public _Routing_Structures getMatchedRout(byte[] dstIpAddr) {
		int index = 0;
		for (_Routing_Structures routingTableEntry : routingTable) {
			// ----- dstIpAddr & rout.Subnet_mask ----- 
			byte[] SubnetMask = StringToByte(routingTableEntry.Subnet_mask);
			byte[] temp = CalDstAndSub(dstIpAddr, SubnetMask);
			// check rout.Destination Address
			if (routingTableEntry.Dst_ip_addr.equals(ByteToString(temp)))
				return routingTable.get(index);
			index++;
		}
		return null;
	}
	
	// ----- bit And operation -----
	private byte[] CalDstAndSub(byte[] dstIpAddr, byte[] SubnetMask) {
		byte[] temp = new byte[dstIpAddr.length];
		for(int i = 0; i < dstIpAddr.length; i++) {
			temp[i] = (byte)(dstIpAddr[i] & SubnetMask[i]);
		}			
		
		return temp;
		
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
	
	// ----- getUnderLayer for Port -----
	public BaseLayer getUnderLayer(int nindex) {
		if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
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
		this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
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
