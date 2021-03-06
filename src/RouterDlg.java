import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

import org.jnetpcap.PcapIf;

public class RouterDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	public File file = null;

	public static LayerManager m_LayerMgr = new LayerManager();
	public static JNetManager jnet;

	private Logger logging = new Logger(this);

	private JTextField routerTableWrite;
	private JTextField arpCacheWrite;
	
	private HashMap<String, String> TotalARPTable = new HashMap<>();

	Container contentPane;
	
	JCheckBox flagUp;
	JCheckBox flagGateway;
	JCheckBox flagHost;

	JTextArea routerTableItemArea;
	JTextArea destinationArea;
	JTextArea netmaskArea;
	JTextArea gatewayArea;
	JTextArea routerTableArea;
	JTextArea arpCacheArea;

	JLabel labelDestination;
	JLabel labelNetmask;
	JLabel labelGateway;
	JLabel labelFlag;
	JLabel labelInterface;
	JLabel labelMetircs;

	
	JButton Router_Table_Entry_Add_Button;
//	JButton Router_Add_Button;
	JButton Router_Delete_Button;
	JButton Setting_Button;
//	JButton Arp_Cache_Item_Delete_Button;
	JButton Arp_Cache_All_Delete_Button;
	

	static JComboBox<String> NICComboBox_1;
//	static JComboBox<String> NICComboBox_2;
	static JComboBox<String> routerInterfaceComboBox;
	static JComboBox<String> routerMetricsComboBox;

	int adapterNumber = 0;

	String Text;
	
	private static byte[] macStoBB(String strMacAddr) {
		String[] strArr = strMacAddr.split("-");
		if(strArr.length != 6) { return null; }
		byte[] byteMacAddr = new byte[6];
		for(int i = 0; i < 6; i++) {
			byteMacAddr[i] = (byte)Integer.parseInt(strArr[i], 16);
		}
		return byteMacAddr;
	}

	private static byte[] StringToBytee(String data) {
		String[] strArr = data.split("[.]");
		if(strArr.length != 4) { return null; }
		byte[] byteIpAddr = new byte[4];
		for(int i = 0; i < 4; i++) {
			byteIpAddr[i] = (byte)Integer.parseInt(strArr[i]);
		}
		return byteIpAddr;
	}
	
	public static void main(String[] args) {
		
		// Get all adapters
		jnet = new JNetManager();
		// Adding layers	
		m_LayerMgr.addLayer(new NILayer("NI"));
		m_LayerMgr.addLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.addLayer(new ARPLayer("ARP"));
		m_LayerMgr.addLayer(new IPLayer("IP"));
	
		m_LayerMgr.addLayer(new NILayer("NI2"));
		m_LayerMgr.addLayer(new EthernetLayer("Ethernet2"));
		m_LayerMgr.addLayer(new ARPLayer("ARP2"));		
		m_LayerMgr.addLayer(new IPLayer("IP2"));
		
		m_LayerMgr.addLayer(new RoutingTable("RT"));
		m_LayerMgr.addLayer(new RouterDlg("GUI"));


		// Connecting Layers
		m_LayerMgr.connectLayers("NI ( *Ethernet ( *IP ( *RT ( *GUI ) ) ) )");
		m_LayerMgr.connectLayers("Ethernet ( *ARP ( +IP ) )");

		m_LayerMgr.connectLayers("NI2 ( *Ethernet2 ( *IP2 ( *RT ( *GUI ) ) ) )");
		m_LayerMgr.connectLayers("Ethernet2 ( *ARP2 ( +IP2 ) )");
		
		byte[] mac1 = ((NILayer)m_LayerMgr.getLayer("NI")).getAdapterMAC();
		((EthernetLayer)m_LayerMgr.getLayer("Ethernet")).setEnetSrcAddress(mac1);
		byte[] mac2 = ((NILayer)m_LayerMgr.getLayer("NI2")).getAdapterMAC();
		((EthernetLayer)m_LayerMgr.getLayer("Ethernet2")).setEnetSrcAddress(mac2);
				
		m_LayerMgr.getLayer("NI").receive();
		m_LayerMgr.getLayer("NI2").receive();
	}

	// print ARP Table arp cache area
	public void printARPTable() {
		TotalARPTable.clear();
		
		HashMap<String, String> arp1 = ((ARPLayer) m_LayerMgr.getLayer("ARP")).getARPTable();
		HashMap<String, String> arp2 = ((ARPLayer) m_LayerMgr.getLayer("ARP2")).getARPTable();
		
		for (String i : arp1.keySet()) {
			TotalARPTable.put(i, arp1.get(i));
		}
		for (String i : arp2.keySet()) {
			TotalARPTable.put(i, arp2.get(i));			
		}
		arpCacheArea.setText("IP\t\tMAC\t\tStatus\n");
		for (String i : TotalARPTable.keySet()) {
			String status = TotalARPTable.get(i) == "??????" ? "\tIncomplete" : "Complete";
			if(i.length() < 13)
				arpCacheArea.append(i + "\t\t" + TotalARPTable.get(i) + "\t" + status + "\n");
			else
				arpCacheArea.append(i + "\t" + TotalARPTable.get(i) + "\t" + status + "\n");
		}
	}
	
	// print routing table area
	public void printRouterTable(ArrayList<_Routing_Structures> RouterTable) {
		routerTableArea.setText("Dst_ip_addr\t\tSubnet_mask\t\tGateway\t\tFlag\tInterface\n");
		for (_Routing_Structures i :RouterTable) {
				routerTableArea.append(i.Dst_ip_addr + "\t\t" + i.Subnet_mask + "\t\t" + i.Gateway + "\t\t" + i.Flag + "\t" + i.Interface + "\n");
		}
	}

	class SetAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			//-----router cache--------------------------------------------------------------------------------------
			// router all delete button
			if (e.getSource() == Router_Delete_Button) {
				((RoutingTable) m_LayerMgr.getLayer("RT")).deleteRoutingTableEntry();
				routerTableWrite.setText("");
			}
			

			// -----arp cache--------------------------------------------------------------------------------------
			// arp cache all delete button
			if (e.getSource() == Arp_Cache_All_Delete_Button) {
				((ARPLayer) m_LayerMgr.getLayer("ARP")).deleteARPTable(null, 0);
				arpCacheWrite.setText("");
			}
			
			
			// -----router table entry
			// setting button
			if(e.getSource() == Router_Table_Entry_Add_Button) {
				String destination = destinationArea.getText();
				String netmask = netmaskArea.getText();
				String gateway = gatewayArea.getText();
				
				String flag = "";
				if(flagUp.isSelected()) {
					flag = "U";
					flagUp.setSelected(false);
				}
				if(flagGateway.isSelected()){
					flag += "G";
					flagGateway.setSelected(false);
				}
				if(flagHost.isSelected()) {
					flag += "H";
					flagHost.setSelected(false);
				}
				
				String interface_ = routerInterfaceComboBox.getSelectedItem().toString();
				
				((RoutingTable) m_LayerMgr.getLayer("RT")).addRoutingTableEntry(destination, netmask, gateway, flag, interface_);
				
				destinationArea.setText("");
				netmaskArea.setText("");
				gatewayArea.setText("");
				routerInterfaceComboBox.setSelectedIndex(0);
				
			}
		}
	}

	public RouterDlg(String pName) {
		pLayerName = pName;

		setTitle("ARP Router Protocol");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 150, 1150, 680);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// -----router table-----------------------------------------------------------------------------------
		// router table total panel
		JPanel routerPanel = new JPanel(); // router table total panel
		routerPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Router Table",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		routerPanel.setBounds(10, 10, 700, 300);
		contentPane.add(routerPanel);
		routerPanel.setLayout(null);

		// router table write panel
		JPanel routerEditorPanel = new JPanel();
		routerEditorPanel.setBounds(10, 20, 680, 210);
		routerPanel.add(routerEditorPanel);
		routerEditorPanel.setLayout(null);

		// router table area
		routerTableArea = new JTextArea();
		routerTableArea.append("Dst_ip_addr\t\tSubnet_mask\t\tGateway\t\tFlag\tInterface\n");
		routerTableArea.setEditable(false);
		routerTableArea.setBounds(0, 0, 680, 210);
		routerEditorPanel.add(routerTableArea);// router table edit
		
		// router table delete button
		Router_Delete_Button = new JButton("Delete");
		Router_Delete_Button.setBounds(200, 240, 300, 45);
		Router_Delete_Button.addActionListener(new SetAddressListener());
		routerPanel.add(Router_Delete_Button);// router Delete button

		
		// -----arp cache-----------------------------------------------------------------------------------
		// arp cache total panel
		JPanel arpPanel = new JPanel(); // arp cache total panel
		arpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		arpPanel.setBounds(10, 320, 700, 300);
		contentPane.add(arpPanel);
		arpPanel.setLayout(null);

		// arp cache write panel
		JPanel arpCacheEditorPanel = new JPanel();
		arpCacheEditorPanel.setBounds(10, 20, 680, 210);
		arpPanel.add(arpCacheEditorPanel);
		arpCacheEditorPanel.setLayout(null);

		// arp cache area
		arpCacheArea = new JTextArea();
		arpCacheArea.append("IP\t\tMAC\t\tS\n");
		arpCacheArea.setEditable(false);
		arpCacheArea.setBounds(0, 0, 680, 210);
		arpCacheEditorPanel.add(arpCacheArea);// arp Cache edit

		// arp cache all delete button
		Arp_Cache_All_Delete_Button = new JButton("Delete");
		Arp_Cache_All_Delete_Button.setBounds(200, 240, 300, 45);
		Arp_Cache_All_Delete_Button.addActionListener(new SetAddressListener());
		arpPanel.add(Arp_Cache_All_Delete_Button);// arp cache all Delete button
		

		// -----routerEntry---------------------------------------------------------------------------------
		// routerEntry panel
		JPanel routerEntryPanel = new JPanel();
		routerEntryPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Router Table Entry",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		routerEntryPanel.setBounds(720, 10, 400, 600);
		contentPane.add(routerEntryPanel);
		routerEntryPanel.setLayout(null);
		
		// destination
		labelDestination = new JLabel("Destination");
		labelDestination.setBounds(50, 50, 90, 20);
		routerEntryPanel.add(labelDestination);
		
		JPanel destinationPanel = new JPanel();
		destinationPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationPanel.setBounds(140, 50, 200, 20);
		routerEntryPanel.add(destinationPanel);
		destinationPanel.setLayout(null);

		destinationArea = new JTextArea();
		destinationArea.setBounds(2, 2, 200, 20);
		destinationPanel.add(destinationArea);
		
		// netmask
		labelNetmask = new JLabel("Netmask");
		labelNetmask.setBounds(50, 100, 90, 20);
		routerEntryPanel.add(labelNetmask);
		
		JPanel netmaskPanel = new JPanel();
		netmaskPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		netmaskPanel.setBounds(140, 100, 200, 20);
		routerEntryPanel.add(netmaskPanel);
		netmaskPanel.setLayout(null);

		netmaskArea = new JTextArea();
		netmaskArea.setBounds(2, 2, 200, 20);
		netmaskPanel.add(netmaskArea);
		
		// gateway
		labelGateway = new JLabel("Gateway");
		labelGateway.setBounds(50, 150, 90, 20);
		routerEntryPanel.add(labelGateway);
		
		JPanel gatewayPanel = new JPanel();
		gatewayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		gatewayPanel.setBounds(140, 150, 200, 20);
		routerEntryPanel.add(gatewayPanel);
		gatewayPanel.setLayout(null);

		gatewayArea = new JTextArea();
		gatewayArea.setBounds(2, 2, 200, 20);
		gatewayPanel.add(gatewayArea);
		
		// flag
		labelFlag = new JLabel("Flag");
		labelFlag.setBounds(50, 200, 90, 20);
		routerEntryPanel.add(labelFlag);
		
		JPanel flagPanel = new JPanel();
		flagPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		flagPanel.setBounds(140, 200, 200, 20);
		routerEntryPanel.add(flagPanel);
		flagPanel.setLayout(null);

		flagUp = new JCheckBox("UP", false);
		flagUp.setBounds(0, 0, 55, 20);
		flagPanel.add(flagUp);
		
		flagGateway = new JCheckBox("Gateway", false);
		flagGateway.setBounds(55, 0, 85, 20);
		flagPanel.add(flagGateway);	
		
		flagHost = new JCheckBox("Host", false);
		flagHost.setBounds(140, 0, 60, 20);
		flagPanel.add(flagHost);
		
		// interface
		labelInterface = new JLabel("Interface");
		labelInterface.setBounds(50, 250, 90, 20);
		routerEntryPanel.add(labelInterface);
		
		JPanel interfacePanel = new JPanel();
		interfacePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		interfacePanel.setBounds(140, 250, 200, 20);
		routerEntryPanel.add(interfacePanel);
		interfacePanel.setLayout(null);

		String[] port_name = {"Port_1", "Port_2"};
		routerInterfaceComboBox = new JComboBox(port_name);
		routerInterfaceComboBox.setBounds(0, 0, 200, 20);
		interfacePanel.add(routerInterfaceComboBox);

		// Add button
		Router_Table_Entry_Add_Button = new JButton("Add");// Add
		Router_Table_Entry_Add_Button.setBounds(100, 520, 200, 45);
		Router_Table_Entry_Add_Button.addActionListener(new SetAddressListener());
		routerEntryPanel.add(Router_Table_Entry_Add_Button);
		
		setVisible(true);

	}

	private boolean isAddressSet() {
		return Setting_Button.getText() == "Reset";
	}

	private String macBtoS(byte[] b) {
		return String.format("%02X-%02X-%02X-%02X-%02X-%02X", b[0], b[1], b[2], b[3], b[4], b[5]);
	}

	private byte[] macStoB(String strMacAddr) {
		String[] strArr = strMacAddr.split("-");
		if(strArr.length != 6) { return null; }
		byte[] byteMacAddr = new byte[6];
		for(int i = 0; i < 6; i++) {
			int num = Integer.parseInt(strArr[i], 16);
			if(0 > num || 255 < num) { return null; }
			byteMacAddr[i] = (byte)num;
		}
		return byteMacAddr;
	}

	private byte[] ipStoB(String strIpAddr) {
		String[] strArr = strIpAddr.split("[.]");
		if(strArr.length != 4) { return null; }
		byte[] byteIpAddr = new byte[4];
		for(int i = 0; i < 4; i++) {
			int num = Integer.parseInt(strArr[i]);
			if(0 > num || 255 < num) { return null; }
			byteIpAddr[i] = (byte)num;
		}
		return byteIpAddr;
	}

	// ----- ByteToString -----
	public static String ByteToString(byte[] data) {
		String result = new String(data);
		return result;
	}

	// ----- StringToByte -----
	public static byte[] StringToByte(String data) {
		return data.getBytes();
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
		// nUpperLayerCount++;
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
}
