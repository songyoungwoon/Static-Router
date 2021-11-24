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

	private Logger logging = new Logger(this);

	private JTextField routerTableWrite;
	private JTextField arpCacheWrite;
//	private JTextField ChattingWrite;
//	private JTextField proxyArpDeviceWrite;
//	private JTextField proxyArpIpWrite;
//	private JTextField proxyArpMacWrite;
//	private JTextField gratuitousArpWrite;

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
//	JTextArea ChattingArea;
//	JTextArea filePathArea;
//	JTextArea proxyArpArea;
	JTextArea srcIpAddress;
	JTextArea srcMacAddress;
	JTextArea dstIpAddress;

	JLabel labelsrcIp;
	JLabel labelsrcMac;
	JLabel labeldstIp;
	JLabel labelArpIp;
	JLabel labelDestination;
	JLabel labelNetmask;
	JLabel labelGateway;
	JLabel labelFlag;
	JLabel labelInterface;
//	JLabel labelProxyIp;
//	JLabel labelProxyMac;
//	JLabel labeldevice;
//	JLabel labelGratuitousArp;
//	JLabel labelFileName;
//	JLabel labelFileLoading;

	JButton Router_Table_Entry_Setting_Button;
	JButton Router_Add_Button;
	JButton Router_Delete_Button;
	JButton Setting_Button;
//	JButton Chat_send_Button;
	JButton Arp_Cache_Send_Button;
	JButton Arp_Cache_Item_Delete_Button;
	JButton Arp_Cache_All_Delete_Button;
//	JButton Proxy_Arp_Add_Button;
//	JButton Proxy_Arp_Delete_Button;
//	JButton Gratuitous_Arp_Button;
//	JButton Find_File_Button;
//	JButton File_send_Button;
//	private JButton fileUploadButton;
//	private JButton fileSendButton;

//	public JProgressBar progressBar;

	static JComboBox<String> NICComboBox_1;
	static JComboBox<String> NICComboBox_2;
	static JComboBox<String> routerInterfaceComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
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
		//m_LayerMgr.connectLayers("NI ( *Ethernet ( *IP ( *GUI )");
		//m_LayerMgr.connectLayers("Ethernet ( *ARP (+IP )");
		m_LayerMgr.connectLayers("NI ( *Ethernet ( *ARP ( +IP ) ( *IP ( *RT ( *GUI ) ) ) ) ) ");
		
	}

	// print ARP Table arp cache area
	public void printARPTable(HashMap<String, String> ARPTable) {
		arpCacheArea.setText("IP\t\tMAC\t\tStatus\n");
		for (String i : ARPTable.keySet()) {
			String status = ARPTable.get(i) == "??????" ? "\tIncomplete" : "Complete";

			if(i.length() < 13)
				arpCacheArea.append(i + "\t\t" + ARPTable.get(i) + "\t" + status + "\n");
			else
				arpCacheArea.append(i + "\t" + ARPTable.get(i) + "\t" + status + "\n");
		}
	}

	// print Proxy Table at proxy arp area
//	public void printProxyTable(HashMap<String, String> ProxyTable) {
//		proxyArpArea.setText("IP\t\tMAC\n");
//		for (String i : ProxyTable.keySet()) {
//			if(i.length() < 13)
//				proxyArpArea.append(i + "\t\t" + ProxyTable.get(i) + "\n");
//			else
//				proxyArpArea.append(i + "\t" + ProxyTable.get(i) + "\n");
//		}
//	}

	class SetAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			//-----router cache--------------------------------------------------------------------------------------
			// router add button
			if (e.getSource() == Router_Add_Button) {
				if (isAddressSet()) {
					String routerStr = routerTableWrite.getText();
					byte[] arpIp = ipStoB(routerStr);

					if(arpIp != null) {
					//	((TCPLayer) m_LayerMgr.getLayer("TCP")).sendARP(arpIp);	
					} else {
						JOptionPane.showMessageDialog(null, "Wrong IP address for basic ARP");
					}
				} else {
					JOptionPane.showMessageDialog(null, "Address Configuration Error");
				}
				routerTableWrite.setText("");
			}
			// router delete button
			if (e.getSource() == Router_Delete_Button) {
				
				
//				String arpIpStr = routerTableWrite.getText();
//				byte[] arpIp = ipStoB(arpIpStr);
//				if(arpIp != null) {
//					((ARPLayer) m_LayerMgr.getLayer("IP")).deleteARPTable(arpIp, 1);	
//				} else {
//					JOptionPane.showMessageDialog(null, "Wrong IP address for basic ARP");
//				}
//				routerTableWrite.setText("");
			}
			

			// -----arp cache--------------------------------------------------------------------------------------
			// arp cache send button
//			if (e.getSource() == Arp_Cache_Send_Button) {
//				if (isAddressSet()) {
//					String arpIpStr = arpCacheWrite.getText();
//					byte[] arpIp = ipStoB(arpIpStr);
//
//					if(arpIp != null) {
//					//	((TCPLayer) m_LayerMgr.getLayer("TCP")).sendARP(arpIp);	
//					} else {
//						JOptionPane.showMessageDialog(null, "Wrong IP address for basic ARP");
//					}
//				} else {
//					JOptionPane.showMessageDialog(null, "Address Configuration Error");
//				}
//				arpCacheWrite.setText("");
//			}
			// arp cache item delete button
			if (e.getSource() == Arp_Cache_Item_Delete_Button) {
				String arpIpStr = arpCacheWrite.getText();
				byte[] arpIp = ipStoB(arpIpStr);
				if(arpIp != null) {
					((ARPLayer) m_LayerMgr.getLayer("ARP")).deleteARPTable(arpIp, 1);	
				} else {
					JOptionPane.showMessageDialog(null, "Wrong IP address for basic ARP");
				}
				arpCacheWrite.setText("");
			}
			// arp cache all delete button
//			if (e.getSource() == Arp_Cache_All_Delete_Button) {
//				((ARPLayer) m_LayerMgr.getLayer("ARP")).deleteARPTable(null, 0);
//				arpCacheWrite.setText("");
//			}
			
			// -----router table entry
			//
			if(flagUp.isSelected()) {
				
			}
			
			
			
			// -----setting------------------------------------------------------------------------------------
			// setting button
			if (e.getSource() == Setting_Button) {
				if (Setting_Button.getText() == "Reset") {
					srcIpAddress.setText("");
					srcMacAddress.setText("");
					dstIpAddress.setText("");
					Setting_Button.setText("Setting");
					srcIpAddress.setEnabled(true);
					srcMacAddress.setEnabled(true);
					dstIpAddress.setEnabled(true);
				} else {
					byte[] srcIp = ipStoB(srcIpAddress.getText());
					byte[] srcMac = macStoB(srcMacAddress.getText());
					byte[] dstIp = ipStoB(dstIpAddress.getText());
					
					if(srcIp != null && srcMac != null && dstIp != null) {
						((IPLayer) m_LayerMgr.getLayer("IP")).setIPSrcAddress(srcIp);
						((IPLayer) m_LayerMgr.getLayer("IP")).setIPDstAddress(dstIp);
						((EthernetLayer) m_LayerMgr.getLayer("Ethernet")).setEnetSrcAddress(srcMac);

						((NILayer) m_LayerMgr.getLayer("NI")).setAdapterNumber(adapterNumber);

						Setting_Button.setText("Reset");
						srcIpAddress.setEnabled(false);
						srcMacAddress.setEnabled(false);
						dstIpAddress.setEnabled(false);	
					} else {
						JOptionPane.showMessageDialog(null, "Wrong Address Format");
					}
				}
			}
//			// -----proxy arp entry----
//			// proxy arp add button
//			if (e.getSource() == Proxy_Arp_Add_Button) {
//				byte[] proxyIp = ipStoB(proxyArpIpWrite.getText());
//				byte[] proxyMac = macStoB(proxyArpMacWrite.getText());
//
//				if(proxyIp != null && proxyMac != null) {
//					((ARPLayer) m_LayerMgr.getLayer("ARP")).setProxyTable(proxyIp, proxyMac);	
//				} else {
//					JOptionPane.showMessageDialog(null, "Wrong address for adding proxy entry");
//				}
//
//				proxyArpIpWrite.setText("");
//				proxyArpMacWrite.setText("");
//			}
//			// proxy arp delete button
//			if (e.getSource() == Proxy_Arp_Delete_Button) {
//				byte[] proxyIp = ipStoB(proxyArpIpWrite.getText());
//				
//				if(proxyIp != null) {
//					((ARPLayer) m_LayerMgr.getLayer("ARP")).deleteProxyTable(proxyIp);
//				} else {
//					JOptionPane.showMessageDialog(null, "Wrong address for deleting proxy entry");
//				}
//
//				proxyArpIpWrite.setText("");
//				proxyArpMacWrite.setText("");
//			}
//
//			// -----gratuitous arp-----
//			// gratuitous arp send button
//			if (e.getSource() == Gratuitous_Arp_Button) {
//				if(isAddressSet()) {
//					byte[] garpMac = macStoB(gratuitousArpWrite.getText());
//					
//					if(garpMac != null) {
//					//	((TCPLayer) m_LayerMgr.getLayer("TCP")).sendGARP(garpMac);
//					} else {
//						JOptionPane.showMessageDialog(null, "Wrong address for gratuitous ARP");
//					}
//				} else {
//					JOptionPane.showMessageDialog(null, "Address Configuration Error");
//				}
//				gratuitousArpWrite.setText("");
//			}
//
//			// -----chatting-----
//			// chatting send button
//			if (e.getSource() == Chat_send_Button) {
//				if (Setting_Button.getText() == "Reset") {
//						String input = ChattingWrite.getText();
//						ChattingArea.append("[SEND] : " + input + "\n");
//				//		((ChatAppLayer) m_LayerMgr.getLayer("ChatApp")).send(input);
//				} else {
//					JOptionPane.showMessageDialog(null, "Address Configuration Error");
//				}
//				ChattingWrite.setText("");
//			}
//
//			// -----file transfer-----
//			// file find button
//			if (e.getSource() == Find_File_Button) {
//				JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
//				fileChooser.setMultiSelectionEnabled(false);
//				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//					file = fileChooser.getSelectedFile();
//					if(file.length() > 65536) {
//						JOptionPane.showMessageDialog(null, "[error]File size is too big");
//						file = null;
//					} else {
//						filePathArea.setText(file.toString());	
//					}
//				}
//			}
//			// file send button
//			if (e.getSource() == File_send_Button) {
//				if (Setting_Button.getText() == "Reset") {
//				//	((FileAppLayer)m_LayerMgr.getLayer("FileApp")).send(file);
//					file = null;
//				} else {
//					JOptionPane.showMessageDialog(null, "Address Configuration Error");
//				}
//				filePathArea.setText("");
//			}

		}
	}

	public RouterDlg(String pName) {
		pLayerName = pName;

		setTitle("ARP Router Protocol");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 150, 1150, 880);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// -----router table-----------------------------------------------------------------------------------
		// router table total panel
		JPanel routerPanel = new JPanel(); // router table total panel
		routerPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Router Table",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		routerPanel.setBounds(10, 10, 700, 400);
		contentPane.add(routerPanel);
		routerPanel.setLayout(null);

		// router table write panel
		JPanel routerEditorPanel = new JPanel();
		routerEditorPanel.setBounds(10, 20, 680, 310);
		routerPanel.add(routerEditorPanel);
		routerEditorPanel.setLayout(null);

		// router table area
		routerTableArea = new JTextArea();
		routerTableArea.append("IP\t\tMAC\t\tS\n");
		routerTableArea.setEditable(false);
		routerTableArea.setBounds(0, 0, 680, 310);
		routerEditorPanel.add(routerTableArea);// router table edit
		
		// router table item area
		routerTableArea = new JTextArea();
		routerTableArea.setBounds(50, 350, 200, 30);
		routerEditorPanel.add(routerTableArea);// router table edit
		
//		// router table add button
//		Router_Add_Button = new JButton("Add");
//		Router_Add_Button.setBounds(100, 340, 210, 45);
//		Router_Add_Button.addActionListener(new SetAddressListener());
//		routerPanel.add(Router_Add_Button);// router Delete button
		
		// router table delete button
		Router_Delete_Button = new JButton("Delete");
		Router_Delete_Button.setBounds(390, 350, 100, 30);
		Router_Delete_Button.addActionListener(new SetAddressListener());
		routerEditorPanel.add(Router_Delete_Button);// router Delete button

//		// arp cache input panel
//		JPanel arpCacheInputPanel = new JPanel();// arp cache input write panel
//		arpCacheInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		arpCacheInputPanel.setBounds(50, 370, 220, 20);
//		arpPanel.add(arpCacheInputPanel);
//		arpCacheInputPanel.setLayout(null);
//
//		// arp cache input
//		arpCacheWrite = new JTextField();
//		arpCacheWrite.setBounds(2, 2, 220, 20);
//		arpCacheInputPanel.add(arpCacheWrite);
//		arpCacheWrite.setColumns(10);// arp cache writing area
//
//		// arp cache IP label
//		labelArpIp = new JLabel("IP : ");
//		labelArpIp.setBounds(20, 370, 30, 20);
//		arpPanel.add(labelArpIp);
//
//		// arp cache Send button
//		Arp_Cache_Send_Button = new JButton("Send");
//		Arp_Cache_Send_Button.setBounds(280, 370, 80, 20);
//		Arp_Cache_Send_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_Send_Button);// arp cache send button
//
//		// arp cache item delete button
//		Arp_Cache_Item_Delete_Button = new JButton("Item Delete");
//		Arp_Cache_Item_Delete_Button.setBounds(65, 325, 120, 30);
//		Arp_Cache_Item_Delete_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_Item_Delete_Button);// arp cache Item Delete button
//
//		// arp cache all delete button
//		Arp_Cache_All_Delete_Button = new JButton("All Delete");
//		Arp_Cache_All_Delete_Button.setBounds(215, 325, 120, 30);
//		Arp_Cache_All_Delete_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_All_Delete_Button);// arp cache All Delete button

		
		// -----arp cache-----------------------------------------------------------------------------------
		// arp cache total panel
		JPanel arpPanel = new JPanel(); // arp cache total panel
		arpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		arpPanel.setBounds(10, 420, 700, 400);
		contentPane.add(arpPanel);
		arpPanel.setLayout(null);

		// arp cache write panel
		JPanel arpCacheEditorPanel = new JPanel();
		arpCacheEditorPanel.setBounds(10, 20, 680, 310);
		arpPanel.add(arpCacheEditorPanel);
		arpCacheEditorPanel.setLayout(null);

		// arp cache area
		arpCacheArea = new JTextArea();
		arpCacheArea.append("IP\t\tMAC\t\tS\n");
		arpCacheArea.setEditable(false);
		arpCacheArea.setBounds(0, 0, 680, 310);
		arpCacheEditorPanel.add(arpCacheArea);// arp Cache edit

		// arp cache item delete button
		Arp_Cache_Item_Delete_Button = new JButton("Delete");
		Arp_Cache_Item_Delete_Button.setBounds(200, 340, 300, 45);
		Arp_Cache_Item_Delete_Button.addActionListener(new SetAddressListener());
		arpPanel.add(Arp_Cache_Item_Delete_Button);// arp cache Item Delete button
		
//		// arp cache input panel
//		JPanel arpCacheInputPanel = new JPanel();// arp cache input write panel
//		arpCacheInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		arpCacheInputPanel.setBounds(50, 370, 220, 20);
//		arpPanel.add(arpCacheInputPanel);
//		arpCacheInputPanel.setLayout(null);
//
//		// arp cache input
//		arpCacheWrite = new JTextField();
//		arpCacheWrite.setBounds(2, 2, 220, 20);
//		arpCacheInputPanel.add(arpCacheWrite);
//		arpCacheWrite.setColumns(10);// arp cache writing area
//
//		// arp cache IP label
//		labelArpIp = new JLabel("IP : ");
//		labelArpIp.setBounds(20, 370, 30, 20);
//		arpPanel.add(labelArpIp);
//
//		// arp cache Send button
//		Arp_Cache_Send_Button = new JButton("Send");
//		Arp_Cache_Send_Button.setBounds(280, 370, 80, 20);
//		Arp_Cache_Send_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_Send_Button);// arp cache send button
//
//		// arp cache item delete button
//		Arp_Cache_Item_Delete_Button = new JButton("Item Delete");
//		Arp_Cache_Item_Delete_Button.setBounds(65, 325, 120, 30);
//		Arp_Cache_Item_Delete_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_Item_Delete_Button);// arp cache Item Delete button
//
//		// arp cache all delete button
//		Arp_Cache_All_Delete_Button = new JButton("All Delete");
//		Arp_Cache_All_Delete_Button.setBounds(215, 325, 120, 30);
//		Arp_Cache_All_Delete_Button.addActionListener(new SetAddressListener());
//		arpPanel.add(Arp_Cache_All_Delete_Button);// arp cache All Delete button

		// -----routerEntry---------------------------------------------------------------------------------
		// routerEntry panel
		JPanel routerEntryPanel = new JPanel();
		routerEntryPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Router Table Entry",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		routerEntryPanel.setBounds(720, 10, 400, 400);
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
		
		String[] port = {"Port1", "Port2"};
		routerInterfaceComboBox = new JComboBox(port);
		routerInterfaceComboBox.setBounds(0, 0, 200, 20);
		interfacePanel.add(routerInterfaceComboBox);
		
		// setting button
		Router_Table_Entry_Setting_Button = new JButton("Setting");// setting
		Router_Table_Entry_Setting_Button.setBounds(100, 315, 200, 45);
		Router_Table_Entry_Setting_Button.addActionListener(new SetAddressListener());
		routerEntryPanel.add(Router_Table_Entry_Setting_Button);

		

//		labelsrcIp = new JLabel("Source IP Address");
//		labelsrcIp.setBounds(20, 120, 170, 20);
//		settingPanel.add(labelsrcIp);
//
//		JPanel sourceIpAddressPanel = new JPanel();
//		sourceIpAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		sourceIpAddressPanel.setBounds(20, 150, 360, 20);
//		settingPanel.add(sourceIpAddressPanel);
//		sourceIpAddressPanel.setLayout(null);
//
//		srcIpAddress = new JTextArea();
//		srcIpAddress.setBounds(2, 2, 360, 20);
//		sourceIpAddressPanel.add(srcIpAddress);// src address
//
//		labelsrcMac = new JLabel("Source MAC Address");
//		labelsrcMac.setBounds(20, 200, 170, 20);
//		settingPanel.add(labelsrcMac);
//
//		JPanel sourceAddressPanel = new JPanel();
//		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		sourceAddressPanel.setBounds(20, 230, 360, 20);
//		settingPanel.add(sourceAddressPanel);
//		sourceAddressPanel.setLayout(null);
//
//		srcMacAddress = new JTextArea();
//		srcMacAddress.setBounds(2, 2, 360, 20);
//		sourceAddressPanel.add(srcMacAddress);// src address
//
//		labeldstIp = new JLabel("Destination IP Address");
//		labeldstIp.setBounds(20, 280, 190, 20);
//		settingPanel.add(labeldstIp);
//
//		JPanel destinationAddressPanel = new JPanel();
//		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		destinationAddressPanel.setBounds(20, 310, 360, 20);
//		settingPanel.add(destinationAddressPanel);
//		destinationAddressPanel.setLayout(null);
//
//		dstIpAddress = new JTextArea();
//		dstIpAddress.setBounds(2, 2, 360, 20);
//		destinationAddressPanel.add(dstIpAddress);// dst address
//
//		JLabel NICLabel = new JLabel("NIC List");
//		NICLabel.setBounds(20, 30, 170, 20);
//		settingPanel.add(NICLabel);
//
//		NICComboBox_1 = new JComboBox();
//		NICComboBox_1.setBounds(20, 70, 360, 20);
//		settingPanel.add(NICComboBox_1);
//
//		NILayer tempNiLayer = (NILayer) m_LayerMgr.getLayer("NI");
//
//		for (int i = 0; i < tempNiLayer.getAdapterList().size(); i++) {
//			// NICComboBox.addItem(((NILayer)
//			// m_LayerMgr.getLayer("NI")).getAdapterObject(i).getDescription());
//			PcapIf pcapIf = tempNiLayer.getAdapterObject(i); //
//			NICComboBox_1.addItem(pcapIf.getName());
//		}
//
//		NICComboBox_1.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// adapterNumber = NICComboBox.getSelectedIndex();
//				JComboBox jcombo = (JComboBox) e.getSource();
//				adapterNumber = jcombo.getSelectedIndex();
//				try {
//					srcMacAddress.setText("");
//					String macAddr = macBtoS(((NILayer) m_LayerMgr.getLayer("NI")).getAdapterObject(adapterNumber).getHardwareAddress());
//					logging.log("Adapter selected: " + adapterNumber + ", Present MAC Addr: " + macAddr);
//					srcMacAddress.append(macAddr);
//
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		});
//
//		try {
//			srcMacAddress.append(macBtoS(
//					((NILayer) m_LayerMgr.getLayer("NI")).getAdapterObject(adapterNumber).getHardwareAddress()));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		;
//
//		Setting_Button = new JButton("Setting");// setting
//		Setting_Button.setBounds(300, 360, 80, 20);
//		Setting_Button.addActionListener(new SetAddressListener());
//		settingPanel.add(Setting_Button);
//
//		setVisible(true);
		
		
		// -----setting---------------------------------------------------------------------------------
		// setting address panel
		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(720, 420, 400, 400);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		labelsrcIp = new JLabel("Source IP Address");
		labelsrcIp.setBounds(20, 120, 170, 20);
		settingPanel.add(labelsrcIp);

		JPanel sourceIpAddressPanel = new JPanel();
		sourceIpAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceIpAddressPanel.setBounds(20, 150, 360, 20);
		settingPanel.add(sourceIpAddressPanel);
		sourceIpAddressPanel.setLayout(null);

		srcIpAddress = new JTextArea();
		srcIpAddress.setBounds(2, 2, 360, 20);
		sourceIpAddressPanel.add(srcIpAddress);// src address

		labelsrcMac = new JLabel("Source MAC Address");
		labelsrcMac.setBounds(20, 200, 170, 20);
		settingPanel.add(labelsrcMac);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(20, 230, 360, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		srcMacAddress = new JTextArea();
		srcMacAddress.setBounds(2, 2, 360, 20);
		sourceAddressPanel.add(srcMacAddress);// src address

		labeldstIp = new JLabel("Destination IP Address");
		labeldstIp.setBounds(20, 280, 190, 20);
		settingPanel.add(labeldstIp);

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(20, 310, 360, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		dstIpAddress = new JTextArea();
		dstIpAddress.setBounds(2, 2, 360, 20);
		destinationAddressPanel.add(dstIpAddress);// dst address

		JLabel NICLabel = new JLabel("NIC List");
		NICLabel.setBounds(20, 30, 170, 20);
		settingPanel.add(NICLabel);

		NICComboBox_1 = new JComboBox();
		NICComboBox_1.setBounds(20, 70, 360, 20);
		settingPanel.add(NICComboBox_1);

		NILayer tempNiLayer = (NILayer) m_LayerMgr.getLayer("NI");

		for (int i = 0; i < tempNiLayer.getAdapterList().size(); i++) {
			// NICComboBox.addItem(((NILayer)
			// m_LayerMgr.getLayer("NI")).getAdapterObject(i).getDescription());
			PcapIf pcapIf = tempNiLayer.getAdapterObject(i); //
			NICComboBox_1.addItem(pcapIf.getName());
		}

		NICComboBox_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// adapterNumber = NICComboBox.getSelectedIndex();
				JComboBox jcombo = (JComboBox) e.getSource();
				adapterNumber = jcombo.getSelectedIndex();
				try {
					srcMacAddress.setText("");
					String macAddr = macBtoS(((NILayer) m_LayerMgr.getLayer("NI")).getAdapterObject(adapterNumber).getHardwareAddress());
					logging.log("Adapter selected: " + adapterNumber + ", Present MAC Addr: " + macAddr);
					srcMacAddress.append(macAddr);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		try {
			srcMacAddress.append(macBtoS(
					((NILayer) m_LayerMgr.getLayer("NI")).getAdapterObject(adapterNumber).getHardwareAddress()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		;

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(300, 360, 80, 20);
		Setting_Button.addActionListener(new SetAddressListener());
		settingPanel.add(Setting_Button);

		setVisible(true);
				
				
//		// -----proxy arp entry--------------------------------------------------------------------------------------
//		JPanel proxyArpPanel = new JPanel();
//		proxyArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry",
//				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
//		proxyArpPanel.setBounds(405, 5, 400, 400);
//		contentPane.add(proxyArpPanel);
//		proxyArpPanel.setLayout(null);
//
//		JPanel proxyArpEditorPanel = new JPanel();
//		proxyArpEditorPanel.setBounds(10, 15, 380, 200);
//		proxyArpPanel.add(proxyArpEditorPanel);
//		proxyArpEditorPanel.setLayout(null);
//
//		proxyArpArea = new JTextArea();
//		proxyArpArea.append("IP\t\tMAC");
//		proxyArpArea.setEditable(false);
//		proxyArpArea.setBounds(0, 0, 380, 200);
//		proxyArpEditorPanel.add(proxyArpArea);// proxy arp edit
//
////		labeldevice = new JLabel("Device");
////		labeldevice.setBounds(50, 250, 80, 20);
////		proxyArpPanel.add(labeldevice);
////
////		JPanel proxyArpDeviceInputPanel = new JPanel();// proxy arp Device input write panel
////		proxyArpDeviceInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
////		proxyArpDeviceInputPanel.setBounds(150, 250, 160, 20);
////		proxyArpPanel.add(proxyArpDeviceInputPanel);
////		proxyArpDeviceInputPanel.setLayout(null);
////
////		proxyArpDeviceWrite = new JTextField();
////		proxyArpDeviceWrite.setBounds(2, 2, 160, 20);
////		proxyArpDeviceInputPanel.add(proxyArpDeviceWrite);
////		proxyArpDeviceWrite.setColumns(10);// writing area
//
//		labelProxyIp = new JLabel("IP : ");
//		labelProxyIp.setBounds(50, 250, 80, 20);
//		proxyArpPanel.add(labelProxyIp);
//
//		JPanel proxyArpIpInputPanel = new JPanel();// proxy arp input write panel
//		proxyArpIpInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		proxyArpIpInputPanel.setBounds(150, 250, 160, 20);
//		proxyArpPanel.add(proxyArpIpInputPanel);
//		proxyArpIpInputPanel.setLayout(null);
//
//		proxyArpIpWrite = new JTextField();
//		proxyArpIpWrite.setBounds(2, 2, 160, 20);
//		proxyArpIpInputPanel.add(proxyArpIpWrite);
//		proxyArpIpWrite.setColumns(10);// writing area
//
//		labelProxyMac = new JLabel("MAC : ");
//		labelProxyMac.setBounds(50, 290, 80, 20);
//		proxyArpPanel.add(labelProxyMac);
//
//		JPanel proxyArpMacInputPanel = new JPanel();// proxy arp input write panel
//		proxyArpMacInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		proxyArpMacInputPanel.setBounds(150, 290, 160, 20);
//		proxyArpPanel.add(proxyArpMacInputPanel);
//		proxyArpMacInputPanel.setLayout(null);
//
//		proxyArpMacWrite = new JTextField();
//		proxyArpMacWrite.setBounds(2, 2, 160, 20);
//		proxyArpMacInputPanel.add(proxyArpMacWrite);
//		proxyArpMacWrite.setColumns(10);// writing area
//
//		Proxy_Arp_Add_Button = new JButton("Add");
//		Proxy_Arp_Add_Button.setBounds(25, 350, 160, 35);
//		Proxy_Arp_Add_Button.addActionListener(new SetAddressListener());
//		proxyArpPanel.add(Proxy_Arp_Add_Button);// proxy arp add button
//
//		Proxy_Arp_Delete_Button = new JButton("Delete");
//		Proxy_Arp_Delete_Button.setBounds(225, 350, 160, 35);
//		Proxy_Arp_Delete_Button.addActionListener(new SetAddressListener());
//		proxyArpPanel.add(Proxy_Arp_Delete_Button);// proxy arp Delete button

//		// -----gratuitous arp------------------------------------------------------------------------------------
//		JPanel garpPanel = new JPanel();
//		garpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP",
//				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
//		garpPanel.setBounds(805, 5, 400, 400);
//		contentPane.add(garpPanel);
//		garpPanel.setLayout(null);
//
//		labelGratuitousArp = new JLabel("MAC : ");
//		labelGratuitousArp.setBounds(20, 100, 80, 20);
//		garpPanel.add(labelGratuitousArp);
//
//		JPanel GratuitousArpInputPanel = new JPanel();// gratuitous arp input write panel
//		GratuitousArpInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		GratuitousArpInputPanel.setBounds(20, 150, 350, 25);
//		garpPanel.add(GratuitousArpInputPanel);
//		GratuitousArpInputPanel.setLayout(null);
//
//		gratuitousArpWrite = new JTextField();
//		gratuitousArpWrite.setBounds(2, 2, 350, 25);
//		GratuitousArpInputPanel.add(gratuitousArpWrite);
//		gratuitousArpWrite.setColumns(10);// writing area
//
//		Gratuitous_Arp_Button = new JButton("Send");
//		Gratuitous_Arp_Button.setBounds(140, 300, 100, 40);
//		Gratuitous_Arp_Button.addActionListener(new SetAddressListener());
//		garpPanel.add(Gratuitous_Arp_Button);// gratuitous arp add button

//		// IP, MAC address setting
//		JPanel addressIpMacPanel = new JPanel();
//		addressIpMacPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP", TitledBorder.LEADING,
//				TitledBorder.TOP, null, new Color(0, 0, 0)));
//		addressIpMacPanel.setBounds(805, 195, 400, 210);
//		contentPane.add(addressIpMacPanel);
//		addressIpMacPanel.setLayout(null);
//
//
//		IPCComboBox = new JComboBox();
//		IPCComboBox.setBounds(70, 30, 310, 20);
//		addressIpMacPanel.add(IPCComboBox);
//
//
//		labelAddressMac = new JLabel("MAC");
//		labelAddressMac.setBounds(20, 70, 50, 20);
//		addressIpMacPanel.add(labelAddressMac);
//
//		JPanel AddressMacInputPanel = new JPanel();// address Mac input write panel
//		AddressMacInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		AddressMacInputPanel.setBounds(70, 70, 310, 20);
//		addressIpMacPanel.add(AddressMacInputPanel);
//		AddressMacInputPanel.setLayout(null);
//
//		addressMacWrite = new JTextField();
//		addressMacWrite.setBounds(2, 2, 310, 20);
//		AddressMacInputPanel.add(addressMacWrite);
//		addressMacWrite.setColumns(10);// writing area
//
//		labelAddressIp = new JLabel("IP");
//		labelAddressIp.setBounds(20, 120, 50, 20);
//		addressIpMacPanel.add(labelAddressIp);
//
//		JPanel AddressIpInputPanel = new JPanel();// address IP input write panel
//		AddressIpInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		AddressIpInputPanel.setBounds(70, 120, 310, 20);
//		addressIpMacPanel.add(AddressIpInputPanel);
//		AddressIpInputPanel.setLayout(null);
//
//		addressIpWrite = new JTextField();
//		addressIpWrite.setBounds(2, 2, 310, 20);
//		AddressIpInputPanel.add(addressIpWrite);
//		addressIpWrite.setColumns(10);// writing area
//
//		Address_Setting_Button = new JButton("Setting");
//		Address_Setting_Button.setBounds(280, 160, 100, 30);
//		Address_Setting_Button.addActionListener(new SetAddressListener());
//		addressIpMacPanel.add(Address_Setting_Button);// Address setting button

//		// -----chatting------------------------------------------------------------------------------------------
//		JPanel chattingPanel = new JPanel();// chatting panel
//		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
//				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
//		chattingPanel.setBounds(5, 410, 400, 400);
//		contentPane.add(chattingPanel);
//		chattingPanel.setLayout(null);
//
//		JPanel chattingEditorPanel = new JPanel();// chatting write panel
//		chattingEditorPanel.setBounds(10, 15, 380, 350);
//		chattingPanel.add(chattingEditorPanel);
//		chattingEditorPanel.setLayout(null);
//
//		ChattingArea = new JTextArea();
//		ChattingArea.setEditable(false);
//		ChattingArea.setBounds(0, 0, 380, 350);
//		chattingEditorPanel.add(ChattingArea);// chatting edit
//
//		JPanel chattingInputPanel = new JPanel();// chatting write panel
//		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		chattingInputPanel.setBounds(10, 370, 290, 20);
//		chattingPanel.add(chattingInputPanel);
//		chattingInputPanel.setLayout(null);
//
//		ChattingWrite = new JTextField();
//		ChattingWrite.setBounds(2, 2, 290, 20);// 249
//		chattingInputPanel.add(ChattingWrite);
//		ChattingWrite.setColumns(10);// writing area
//
//		Chat_send_Button = new JButton("Send");
//		Chat_send_Button.setBounds(310, 370, 80, 20);
//		Chat_send_Button.addActionListener(new SetAddressListener());
//		chattingPanel.add(Chat_send_Button);// chatting send button
//
//		// -----file-----
//		JPanel filePanel = new JPanel(); // file
//		filePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "file", TitledBorder.LEADING,
//				TitledBorder.TOP, null, new Color(0, 0, 0)));
//		filePanel.setBounds(405, 410, 400, 400);
//		contentPane.add(filePanel);
//		filePanel.setLayout(null);
//
//		labelFileName = new JLabel("File : ");
//		labelFileName.setBounds(20, 50, 60, 20);
//		filePanel.add(labelFileName);
//
//		JPanel filePathPanel = new JPanel();
//		filePathPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		filePathPanel.setBounds(20, 80, 360, 20);
//		filePanel.add(filePathPanel);
//		filePathPanel.setLayout(null);
//
//		filePathArea = new JTextArea();
//		filePathArea.setBounds(2, 2, 380, 20);
//		filePathPanel.add(filePathArea);// file Path edit
//		filePathArea.setColumns(10);// writing area
//
//		Find_File_Button = new JButton("Find");
//		Find_File_Button.setBounds(310, 110, 70, 20);
//		Find_File_Button.addActionListener(new SetAddressListener());
//		filePanel.add(Find_File_Button);
//
//		labelFileLoading = new JLabel("Loading : ");
//		labelFileLoading.setBounds(20, 180, 60, 20);
//		filePanel.add(labelFileLoading);
//
//		progressBar = new JProgressBar();
//		progressBar.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		progressBar.setBounds(20, 210, 360, 20);
//		progressBar.setValue(0);
//		progressBar.setStringPainted(true);
//		filePanel.add(progressBar);
//		progressBar.setLayout(null);
//
//		File_send_Button = new JButton("Send");
//		File_send_Button.setBounds(310, 240, 70, 20);
//		File_send_Button.addActionListener(new SetAddressListener());
//		filePanel.add(File_send_Button);

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

//	public boolean receive(byte[] input) {
//		if (input != null) {
//			byte[] data = input;
//			Text = new String(data);
//			ChattingArea.append("[RECV] : " + Text + "\n");
//			return false;
//		}
//		return false;
//	}

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
