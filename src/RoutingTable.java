import java.util.ArrayList;

public class RoutingTable {
	// ----- Routing Table -----
    private ArrayList<_Routing_Structures> RoutingTable = new ArrayList<>();
    
	
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
    
	public boolean addRoutingTableEntry(String Dst_ip_addr, String Subnet_mask, String Gateway, String Flag, String Interface) {
        return RoutingTable.add(new _Routing_Structures(Dst_ip_addr, Subnet_mask, Gateway, Flag, Interface));
    }

	public void deleteRoutingTableEntry(int index) {
        RoutingTable.remove(index);
    }
	
	public ArrayList<_Routing_Structures> getRoutingTable() {
		return RoutingTable;
	}

}
