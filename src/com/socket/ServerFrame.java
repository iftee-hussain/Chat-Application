package com.socket;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ServerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SocketServer server;
	Timer timer;

	public ServerFrame() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				ServerFrame.class.getResource("/com/socket/icon.png")));
		initComponents();
		setResizable(false);
		getContentPane().setLayout(null);
		setTitle("Dutta Chat Server");
		jTextArea1.setEditable(false);
		getContentPane().add(jScrollPane1);
		getContentPane().add(btnStartServer);

		btnStopServer = new JButton();

		btnStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (server != null) {
					try {
						timer.cancel();
						server.stopServer();
						btnStartServer.setEnabled(true);
						btnStopServer.setEnabled(false);

					} catch (Exception e) {
						System.err.println("Error while stopping server. "
								+ e.getMessage());
					}
				}
			}
		});
		btnStopServer.setText("Stop Server");
		btnStopServer.setEnabled(true);
		btnStopServer.setBounds(320, 276, 270, 34);
		// getContentPane().add(btnStopServer);
		btnStopServer.setEnabled(false);
		timer = new Timer();

	}

	public boolean isWin32() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	private void initComponents() {

		btnStartServer = new javax.swing.JButton();
		btnStartServer.setBounds(5, 276, 270, 34);
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Activities Log",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(64, 64,
						64)));
		jScrollPane1.setBounds(5, 0, 591, 270);
		jTextArea1 = new javax.swing.JTextArea();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		btnStartServer.setText("Start Server");
		btnStartServer.setEnabled(true);
		btnStartServer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {

					if (server != null) {
						server.stopServer();
						server = null;
					}
					server = new SocketServer(ServerFrame.this);
					new Thread(server).start();
					btnStartServer.setEnabled(false);
					btnStopServer.setEnabled(true);
					timer = new Timer();
					timer.scheduleAtFixedRate(new BackgroundService(server), 0,
							100000);
				} catch (Exception e) {

					jTextArea1.append("MySQL database server not found\n "
							+ e.getMessage());
				}
			}
		});

		jTextArea1.setColumns(20);
		jTextArea1.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
		jTextArea1.setRows(5);
		jScrollPane1.setViewportView(jTextArea1);
		getContentPane().setPreferredSize(new Dimension(600, 330));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getPreferredSize().width) / 2,
				(dim.height - getPreferredSize().height) / 2);

		pack();
	}

	public void RetryStart(int port) {

	}

	public void startServer() {
		try {

			if (server != null) {
				server.stopServer();
				server = null;
			}
			server = new SocketServer(ServerFrame.this);
			timer = new Timer();
			timer.scheduleAtFixedRate(new BackgroundService(server), 0, 100000);
		} catch (Exception e) {
			jTextArea1.append("MySQL database server not found\n ");
		}
	}

	public static void main(String args[]) {

		if (args.length != 0) {
			new ServerFrame().startServer();
		} else {
			try {
				for (LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (Exception ex) {
				System.err.println("Look & Feel Exception");
			}

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					new ServerFrame().setVisible(true);
				}
			});

		}
	}

	private javax.swing.JButton btnStartServer;
	private javax.swing.JScrollPane jScrollPane1;
	public javax.swing.JTextArea jTextArea1;
	private JButton btnStopServer;
}
