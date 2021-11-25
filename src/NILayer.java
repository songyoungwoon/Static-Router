import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer {

	// ----- Properties -----
	private int nUpperLayerCount = 0;
	private String pLayerName = null;
	private BaseLayer p_UnderLayer = null;
	private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private Logger logging;

    private Pcap m_AdapterObject;
    private PcapIf m_pAdapter;

	// ----- Constructor -----
	public NILayer(String pName) {
		this.pLayerName = pName;
		this.logging = new Logger(this);

        this.m_pAdapter = RouterDlg.jnet.getPcapIf();
        if(this.m_pAdapter == null) {
            logging.panic("No more available adapters", null);
        }
        logging.log(this.toString() + " installed");

		int snaplen = 64 * 1024; // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000; // 10 seconds in millis
        StringBuilder errbuf = new StringBuilder();
		this.m_AdapterObject = Pcap.openLive(this.m_pAdapter.getName(), snaplen, flags, timeout, errbuf);
	}

	public boolean send(byte[] input, int length) {
		ByteBuffer buf = ByteBuffer.wrap(input);
		logging.log("Send from " + this.toString());
		if (m_AdapterObject.sendPacket(buf) != Pcap.OK) {
			logging.error(m_AdapterObject.getErr());
			return false;
		}
		return true;
	}

	public boolean receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.getUpperLayer(0));
		Thread obj = new Thread(thread);
		obj.start();
		logging.log("Receive thread(" + this.toString() + ") start");
		System.out.println(1);
	
        return true;
	}
	
	public byte[] getAdapterIP() {
		return this.m_pAdapter.getAddresses().get(0).getAddr().getData();
	}
	
	public byte[] getAdapterMAC() {
		try {
			return this.m_pAdapter.getHardwareAddress();
		} catch(IOException err) {
			err.printStackTrace();
		}
		return null;
	}

	@Override
	public void setUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void setUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
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
	public void setUpperUnderLayer(BaseLayer pUULayer) {
		this.setUpperLayer(pUULayer);
		pUULayer.setUnderLayer(this);

	}
	private String ByteToString(byte[] data) {
		return String.format("%d.%d.%d.%d", (data[0] & 0xff), (data[1] & 0xff), (data[2] & 0xff), (data[3] & 0xff));
	}

	private String macBtoS(byte[] b) {
		return String.format("%02X-%02X-%02X-%02X-%02X-%02X", b[0], b[1], b[2], b[3], b[4], b[5]);
	}
	
	public String toString() {
		return macBtoS(getAdapterMAC()) + '/' + ByteToString(getAdapterIP());
	}
}

// ----- Thread class -----
class Receive_Thread implements Runnable {
	byte[] data;
	Pcap AdapterObject;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
		AdapterObject = m_AdapterObject;
		UpperLayer = m_UpperLayer;
	}

	@Override
	public void run() {
		while (true) {
			PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					System.out.println("[NI]: Receive and pass to " + UpperLayer.getLayerName());
					data = packet.getByteArray(0, packet.size());
					UpperLayer.receive(data);
				}
			};

			AdapterObject.loop(100000, jpacketHandler, "");
		}
	}
}
