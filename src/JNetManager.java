import java.io.File;
import org.jnetpcap.PcapIf;
import org.jnetpcap.Pcap;
import java.util.ArrayList;


public class JNetManager {
	private ArrayList<PcapIf> m_pAdapterList;
    private int nextPcapIf;

    // ----- Load Lib -----
	static {
		try {
			String jnetpcapPath = new File("jnetpcap.dll").getAbsolutePath();
			System.load(jnetpcapPath);
			
			System.out.println("[JNetPcap] Library loded: " + jnetpcapPath);
		} catch (UnsatisfiedLinkError e) {
			System.err.println("[JNetPcap] Library failed to load: " + e);
			System.exit(0);
		}

	}

    // ----- Constructor -----
    public JnetManager() {
        // Find all possible adapters
	    StringBuilder errbuf = new StringBuilder();
        ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
        if(Pcap.findAllDevs(allDevs, errbuf) != Pcap.OK) {
			System.err.println("[JNetPcap] Failed to load adapters");
			System.exit(0);
        }

        // Filter IPv4 adaters from all adapters
		this.m_pAdapterList = new ArrayList<PcapIf>();
        for(int i = 0; i < allDevs.size(); i++) {
            if(allDevs.get(i).getAddresses()getAddr().get(0).getFamily() == 2) {
                this.m_pAdapterList.add(allDevs.get(i));
            }
        }

        if(allDevs.size() == 0 || this.m_pAdapterList.size() == 0) {
			System.err.println("[JNetPcap] No adapters found");
			System.exit(0);
        }

        System.out.println("[JNetPcap] " + this.m_pAdapterList.size() + "Adapters found");
        this.nextPcapIf = 0;
    }

    public PcapIf getPcapIf() {
        if(this.nextPcapIf < this.m_pAdapterList.size()) {
            return this.m_pAdapterList.get(this.nextPcapIf++);
        }
        return null;
    }
}
