import java.util.ArrayList;

public class RoutingTable implements BaseLayer {
	// ----- Properties -----
	private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	// ----- Routing Table -----
    public ArrayList<_Routing_Structures> RoutingTable = new ArrayList<>();
    
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
		for (_Routing_Structures routingTableEntry : RoutingTable) {
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
        return RoutingTable.add(new _Routing_Structures(Dst_ip_addr, Subnet_mask, Gateway, Flag, Interface));
    }

	public void deleteRoutingTableEntry(int index) {
        RoutingTable.remove(index);
    }
	
	public ArrayList<String> getMatchedRout(byte[] dstIpAddr) {
		int routingTableIndex = 0;
		byte[] temp = dstIpAddr.clone();
		for (_Routing_Structures routingTableEntry : RoutingTable) {
			// ----- dstIpAddr & rout.Subnet_mask ----- 
			byte[] SubnetMask = StringToByte(routingTableEntry.Subnet_mask);
		    dstIpAddr = CalDstAndSub(dstIpAddr, SubnetMask);
			// check rout.Destination Address
			if (routingTableEntry.Dst_ip_addr.equals(ByteToString(dstIpAddr))) {
				break;
			}
			else {
				dstIpAddr = temp.clone();
				routingTableIndex++;
			}
		}
		ArrayList<String> matchedRoutStr = new ArrayList<>();
		matchedRoutStr.add(RoutingTable.get(routingTableIndex).Dst_ip_addr);
		matchedRoutStr.add(RoutingTable.get(routingTableIndex).Subnet_mask);
		matchedRoutStr.add(RoutingTable.get(routingTableIndex).Gateway);
		matchedRoutStr.add(RoutingTable.get(routingTableIndex).Flag);
		matchedRoutStr.add(RoutingTable.get(routingTableIndex).Interface);
		
		return matchedRoutStr;
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
