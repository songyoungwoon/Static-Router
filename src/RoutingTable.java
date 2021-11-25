import java.util.ArrayList;
import java.util.Arrays;

// ----- Routing Structures -----
class _Routing_Structures {
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
    
	public boolean addRoutingTableEntry(String Dst_ip_addr, String Subnet_mask, String Gateway, String Flag, String Interface) {
		routingTable.add(new _Routing_Structures(Dst_ip_addr, Subnet_mask, Gateway, Flag, Interface));
		((RouterDlg) RouterDlg.m_LayerMgr.getLayer("GUI")).printRouterTable(routingTable);
		return true;
    }

	public void deleteRoutingTableEntry() {
        routingTable.clear();
    }

    public boolean rout(byte[] input) {
		// 1.address
		byte[] dstIpAddr = Arrays.copyOfRange(input, 16, 20);
		byte[] directTransferIp = null;
		// 2.matchedRout
		_Routing_Structures matchedRout = getMatchedRout(dstIpAddr);
		// 3.Flag
		if(matchedRout.Flag.equals("U")){}
		else if(matchedRout.Flag.equals("UG")) {
			directTransferIp = StringToByte(matchedRout.Gateway);
		}
		else if(matchedRout.Flag.equals("UH")) {
			directTransferIp = dstIpAddr;
		}
		// 4.send
		String[] temp = matchedRout.Interface.split("_");
		int portNum = Integer.parseInt(temp[1]);
		return ((IPLayer) this.getUnderLayer(portNum - 1)).send(input, input.length, directTransferIp);
		
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
		return p_aUnderLayer.get(nindex);
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
