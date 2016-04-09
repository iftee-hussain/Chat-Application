package com.socket;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SocketServer implements Runnable {

	public ServerThread clients[];
	public ServerSocket server = null;
	public Thread thread = null;
	public int clientCount = 0, port = 13000, port_file_server = 13001;
	public ServerFrame ui;
	public Database db;
	private FileServer fileServer;
	private static final int UPLOADER = 5; // UPLOADER = CONNECT WITH UPLOADER
	private static final int DOWNLOADER = 6; // /UPLOADER = CONNECT WITH
	public LogManager logManager;

	public SocketServer(ServerFrame frame) throws Exception {
		logManager = new LogManager();
		clients = new ServerThread[1000];
		ui = frame;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new CheckUser(), 10000, 10000);
		db = new Database();
		try {
			server = new ServerSocket(port);
			port = server.getLocalPort();
			ui.jTextArea1.append("Server startet. IP : "
					+ InetAddress.getLocalHost() + ", Port : "
					+ server.getLocalPort());
			logManager.addUndefineLog("Server startet. IP : "
					+ InetAddress.getLocalHost() + ", Port : "
					+ server.getLocalPort());
			fileServer = new FileServer(port_file_server);
			new Thread(fileServer).start();
			startMainThread();
		} catch (IOException ioe) {

			ui.jTextArea1.append("Can not bind to port : " + port
					+ "\nRetrying");
			logManager.addErrorLog("Can not bind to port : " + port);
		}

	}

	public void run() {
		while (thread != null) {

			try {
				ui.jTextArea1.append("\nWaiting for a client ...");
				addThread(server.accept());
			} catch (Exception ioe) {
				ui.jTextArea1.append("\nServer accept error: \n");
				logManager.addErrorLog("Server accept error:");
			}
		}
	}

	public void startMainThread() {

		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}

	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	public void stopServer() throws Exception {

		for (ServerThread t : clients) {
			if (t != null) {
				t.close();
			}
		}

		fileServer.stopServer();
		server.close();
		stop();
		logManager.addUndefineLog("Server Stopp.");
		ui.jTextArea1.append("\nServer Stopp.\n");
	}

	private int findClient(int ID) {
		for (int i = 0; i < clientCount; i++) {
			if (clients[i].getID() == ID) {
				return i;
			}
		}
		return -1;
	}

	public synchronized void handle(int ID, Message msg) {

		if (msg.content.equals(".bye")) {
			try {
				db.setOnline(msg.sender, false);
				AnnounceWithoutSender(Constant.USER_LIST, msg.sender, "SERVER",
						db.getUserList());
				clients[findClient(ID)]
						.logout(clients[findClient(ID)].username);
			} catch (Exception e) {
				e.printStackTrace();
				logManager.addErrorLog("Error while removing id.");
				ui.jTextArea1.append("Error while removing id.");
			}
		} else {
			if (msg.type == Constant.LOGIN) {
				if (findUserThread(msg.sender) == null
						|| !findUserThread(msg.sender).isLogin()) {
					try {
						UserDetails ud = db.login(msg.sender, msg.content);
						if (ud != null) {
							clients[findClient(ID)].login(msg.sender);
							clients[findClient(ID)].username = msg.sender;
							clients[findClient(ID)].userDetails = ud;
							clients[findClient(ID)].setOnline(true);
							clients[findClient(ID)].send(new Message(
									Constant.LOGIN, ud.toString(), "TRUE",
									msg.sender, msg.callBackId));
							Announce(Constant.USER_LIST, "SERVER",
									db.getUserList());

						} else {
							clients[findClient(ID)].send(new Message(
									Constant.LOGIN, "SERVER",
									"Username and Password doesn't match.",
									msg.sender, msg.callBackId));
							logManager
									.addErrorLog("Username and Password doesn't match.");

						}
					} catch (Exception e) {
						if (findUserThread(msg.sender) == null) {
							findUserThread(msg.sender)
									.send(new Message(
											Constant.SERVER_ERROR,
											"SERVER",
											"Server error: Error while connection database",
											msg.sender, msg.callBackId));
							logManager
									.addErrorLog("Server error: Error while connection database");
						}

					}
				} else {
					clients[findClient(ID)].send(new Message(Constant.LOGIN,
							"SERVER", "User already login.", msg.sender,
							msg.callBackId));
				}
			} else if (!clients[findClient(ID)].isLogin) {
				return;
			} else if (msg.type == Constant.STATUS_CHECK) {
				clients[findClient(ID)].send(new Message(Constant.STATUS_CHECK,
						"Server", "OK", msg.sender, msg.callBackId));
			} else if (msg.type == Constant.CHAT_PERMISSION) {
				clients[findClient(ID)].send(new Message(
						Constant.CHAT_PERMISSION, "Server", db
								.getChatPermission(msg.content), msg.sender,
						msg.callBackId));
			} else if (msg.type == Constant.UPDATE_FRIEND_REQUEST) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.UPDATE_FRIEND_REQUEST, "Server", db
									.updateFriendReq(msg.content), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());
					e.printStackTrace();
				}
			} else if (msg.type == Constant.SPECIFIC_USER_PERMISSION) {
				clients[findClient(ID)].send(new Message(
						Constant.SPECIFIC_USER_PERMISSION, "Server", db
								.getSpecificUserPermission(msg.content),
						msg.sender, msg.callBackId));

			}
			// //////<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<GROUP>.....................
			else if (msg.type == Constant.GROUP_HISTORY) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.GROUP_HISTORY, msg.content, db
									.getGroupHistory(msg.sender, msg.content),
							msg.sender, msg.callBackId));

				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());
					e.printStackTrace();
				}
			} else if (msg.type == Constant.GROUP_MESSAGE) {

				try {
					db.saveGroupHistory(msg.sender, msg.recipient, msg.content); // //Sender
																					// grouName
																					// conte
					ArrayList<String> memberList = db
							.getMemberList(msg.recipient);
					for (String string : memberList) {
						if (!string.equals(msg.sender))
							db.setGroupNotification(string, msg.recipient, true);
						if (!string.equals(msg.sender)
								&& findUserThread(string) != null) {
							findUserThread(string).send(
									new Message(Constant.GROUP_MESSAGE,
											msg.sender, msg.content,
											msg.recipient, msg.callBackId));
						}

					}
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());
					e.printStackTrace();
				}

				// Baki Kaj Hobe
			} else if (msg.type == Constant.SET_GROUP_NOTIFICATION) {
				db.setGroupNotification(msg.sender, msg.content,
						Boolean.parseBoolean(msg.recipient));
			} else if (msg.type == Constant.GET_GROUP_NOTIFICATION) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.GET_GROUP_NOTIFICATION, "true", db
									.getGroupNotification(msg.sender),
							msg.sender, msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());
				}
			} else if (msg.type == Constant.CREATE_GROUP) {
				try {
					db.createGroup(msg.content);
					clients[findClient(ID)].send(new Message(
							Constant.CREATE_GROUP, "Server", "true",
							msg.sender, msg.callBackId));
				} catch (Exception e) {
					clients[findClient(ID)].send(new Message(
							Constant.CREATE_GROUP, "Server", "false",
							msg.sender, msg.callBackId));
				}
			} else if (msg.type == Constant.DELETE_GROUP) {
				try {
					db.deleteGroup(msg.content);
					clients[findClient(ID)].send(new Message(
							Constant.DELETE_GROUP, "Server", "true",
							msg.sender, msg.callBackId));
				} catch (Exception e) {
					clients[findClient(ID)].send(new Message(
							Constant.DELETE_GROUP, "Server", e.getMessage(),
							msg.sender, msg.callBackId));
					logManager.addErrorLog(e.getMessage());
				}
			} else if (msg.type == Constant.GET_GROUPS_NAMES) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.GET_GROUPS_NAMES, "true", db
									.getGroupNames(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					clients[findClient(ID)].send(new Message(
							Constant.GET_GROUPS_NAMES, "false", e.getMessage(),
							msg.sender, msg.callBackId));
					logManager.addErrorLog(e.getMessage());
				}
			} else if (msg.type == Constant.GET_MEMBERS_FROM_GROUP) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.GET_MEMBERS_FROM_GROUP, "true", db
									.getMemberListFromGroup(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					clients[findClient(ID)].send(new Message(
							Constant.GET_MEMBERS_FROM_GROUP, "false", e
									.getMessage(), msg.sender, msg.callBackId));
					logManager.addErrorLog(e.getMessage());

				}
			} else if (msg.type == Constant.ADD_MEMBER_TO_GROUP) {
				try {
					db.addMemberToGroup(msg);
					clients[findClient(ID)].send(new Message(
							Constant.ADD_MEMBER_TO_GROUP, msg.recipient,
							"true", msg.sender, msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)].send(new Message(
							Constant.ADD_MEMBER_TO_GROUP, "Server", "false",
							msg.sender, msg.callBackId));
				}
			} else if (msg.type == Constant.REMOVE_MEMBER_FROM_GROUP) {
				try {
					String l[] = msg.recipient
							.split(Message.SECOND_LEVEL_SEPERATOR);
					for (String string : l) {
						db.deleteMemberFromGroup(msg.content, string);
					}
					clients[findClient(ID)].send(new Message(
							Constant.REMOVE_MEMBER_FROM_GROUP, "Server",
							"true", msg.sender, msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)].send(new Message(
							Constant.REMOVE_MEMBER_FROM_GROUP, "Server", e
									.getMessage(), msg.sender, msg.callBackId));
				}
				// ////////////////////////////////////////////////////////////////////////
			} else if (msg.type == Constant.DELETE_USERS) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.DELETE_USERS, "true", db.deleteUsers(msg),
							msg.sender, msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)].send(new Message(
							Constant.DELETE_USERS, "false", e.getMessage(),
							msg.sender, msg.callBackId));
				}

			} else if (msg.type == Constant.CALL_TRANSFER) {
				try {
					db.setTemporaryFriendShip(msg.content, msg.recipient);
					sendToUser(Constant.SPECIFIC_USER_PERMISSION, "SERVER",
							db.getSpecificUserPermission(msg.content),
							msg.content);
					sendToUser(Constant.SPECIFIC_USER_PERMISSION, "SERVER",
							db.getSpecificUserPermission(msg.recipient),
							msg.recipient);

				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (msg.type == Constant.BLOCK) {
				// System.out.println("Block Request Came.........");
				try {
					clients[findClient(ID)].send(new Message(Constant.BLOCK,
							"true", db.blockUsers(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)]
							.send(new Message(Constant.BLOCK, "false", e
									.getMessage(), msg.sender, msg.callBackId));
				}

			}

			else if (msg.type == Constant.EDIT) {
				try {
					clients[findClient(ID)].send(new Message(Constant.EDIT,
							"true", db.editUsers(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)]
							.send(new Message(Constant.EDIT, "false", e
									.getMessage(), msg.sender, msg.callBackId));
					e.printStackTrace();
				}

			}

			else if (msg.type == Constant.USER_LIST) {
				SendUserList(msg.sender, msg.callBackId);
			} else if (msg.type == Constant.MESSAGE_GROUP) {
				sendToGroup(msg.type, msg.sender, msg.content, msg.recipient);
			} else if (msg.type == Constant.MESSAGE) {
				if (msg.recipient.equals("All")) {
					Announce(Constant.MESSAGE, msg.sender, msg.content);
				} else {
					if (findUserThread(msg.recipient) != null) {
						findUserThread(msg.recipient).send(
								new Message(msg.type, msg.sender, msg.content,
										msg.recipient, msg.callBackId));
						findUserThread(msg.recipient).send(
								new Message(Constant.NOTIFICATION_LIST,
										msg.sender, "", msg.recipient, -1));
						db.setNotification(msg.sender, msg.recipient);
					} else
						db.setNotification(msg.sender, msg.recipient);
					try {

						db.saveHistory(msg.sender, msg.recipient, msg.content);
					} catch (Exception e) {
						logManager
								.addErrorLog("Error while save history in database.");
						ui.jTextArea1
								.append("Error while save history in database.");
					}
				}
			} else if (msg.type == Constant.NOTIFICATION_LIST) {

				if (msg.content.equalsIgnoreCase("clear")) {
					db.clearNotification(msg.sender, msg.recipient);
				} else {

					clients[findClient(ID)].send(new Message(
							Constant.NOTIFICATION_LIST, "Server", db
									.getNotificationList(msg.sender),
							msg.sender, msg.callBackId));
				}

			} else if (msg.type == Constant.SPECIFIC_USERS) {

				clients[findClient(ID)].send(new Message(
						Constant.SPECIFIC_USERS, "Server",
						db.getSpecificUsers(Integer.parseInt(msg.content)),
						msg.sender, msg.callBackId));

			} else if (msg.type == Constant.UPDATE_PERMISSION) {
				try {
					db.updatePermissions(msg.content);
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			else if (msg.type == Constant.CHANGE_PASS) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.CHANGE_PASS, "true", db
									.update_password(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					e.printStackTrace();
				}
			} else if (msg.type == Constant.FRIEND_REQ) {
				try {
					db.update_friend_req(msg);
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					e.printStackTrace();
				}
			} else if (msg.type == Constant.GET_FRIEND_REQ) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.GET_FRIEND_REQ, "true", db
									.getFriendRequests(), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					clients[findClient(ID)].send(new Message(
							Constant.GET_FRIEND_REQ, "false",
							"Can't get friend requests at this time",
							msg.sender, msg.callBackId));
				}
			} else if (msg.type == Constant.UPDATE_MY_PROFILE) {
				try {
					clients[findClient(ID)].send(new Message(
							Constant.UPDATE_MY_PROFILE, "true", db
									.updateMyProfile(msg), msg.sender,
							msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					clients[findClient(ID)]
							.send(new Message(
									Constant.UPDATE_MY_PROFILE,
									"false",
									"Can't update profile this time. Please Try again later...",
									msg.sender, msg.callBackId));
				}
			}

			else if (msg.type == Constant.FILTERED_TEXT) {

				if (msg.recipient.equalsIgnoreCase("GET")) {
					try {
						clients[findClient(ID)].send(new Message(
								Constant.FILTERED_TEXT, "GET", db
										.getFilteredText(), "SUCCESS",
								msg.callBackId));
					} catch (Exception e) {
						logManager.addErrorLog(e.getMessage());

						clients[findClient(ID)].send(new Message(
								Constant.FILTERED_TEXT, "GET", "ERROR",
								"ERROR", msg.callBackId));
					}
				} else if (msg.recipient.equalsIgnoreCase("SET")) {
					try {
						db.setFilteredText(msg.content);
						clients[findClient(ID)].send(new Message(
								Constant.FILTERED_TEXT, "SET", "SET",
								"SUCCESS", msg.callBackId));
					} catch (Exception e) {
						logManager.addErrorLog(e.getMessage());

						clients[findClient(ID)].send(new Message(
								Constant.FILTERED_TEXT, "SET", "ERROR",
								"ERROR", msg.callBackId));
					}
				}

			}

			else if (msg.type == Constant.HISTORY) {
				try {
					clients[findClient(ID)].send(new Message(Constant.HISTORY,
							msg.sender, db
									.getHistory(msg.sender, msg.recipient),
							msg.recipient, msg.callBackId));
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					e.printStackTrace();
				}
			} else if (msg.type == Constant.HELLO) {
				clients[findClient(ID)].send(new Message(Constant.HELLO,
						"SERVER", "OK", msg.sender, msg.callBackId));
			}

			else if (msg.type == Constant.SINE_UP) {

				try {
					UserDetails obj = new UserDetails(msg.content);
					if (!db.userExists(obj.userName)) {

						db.addUser(obj, msg.recipient);
						clients[findClient(ID)].username = msg.sender;
						clients[findClient(ID)].send(new Message(
								Constant.SINE_UP, "SERVER", "TRUE", msg.sender,
								msg.callBackId));
						Announce(Constant.USER_LIST, "SERVER", db.getUserList());

					} else {
						clients[findClient(ID)].send(new Message(
								Constant.SINE_UP, "SERVER", "FALSE",
								msg.sender, msg.callBackId));
					}
				} catch (Exception e) {
					logManager.addErrorLog(e.getMessage());

					e.printStackTrace();
					if (findUserThread(msg.sender) == null) {
						findUserThread(msg.sender)
								.send(new Message(
										Constant.SERVER_ERROR,
										"Server error: Error while connection database",
										e.getMessage(), msg.sender,
										msg.callBackId));
					}
				}

			} else if (msg.type == Constant.UPLOAD_REQUEST) {
				findUserThread(msg.recipient).send(
						new Message(Constant.UPLOAD_REQUEST, msg.sender,
								msg.content, msg.recipient, msg.callBackId));

			} else if (msg.type == Constant.UPLOAD_RESPONSE) {
				if (msg.content.equals("true")) {

					fileServer.findUserThread(msg.recipient).actAs = UPLOADER;
					fileServer.findUserThread(msg.sender).actAs = DOWNLOADER;
					fileServer.findUserThread(msg.sender).ID_Pair = fileServer
							.findUserThread(msg.recipient).ID;
					fileServer.findUserThread(msg.recipient).ID_Pair = fileServer
							.findUserThread(msg.sender).ID;

					findUserThread(msg.recipient)
							.send(new Message(Constant.UPLOAD_RESPONSE,
									msg.sender, msg.content, msg.recipient,
									msg.callBackId));
				} else {
					findUserThread(msg.recipient)
							.send(new Message(Constant.UPLOAD_RESPONSE,
									msg.sender, msg.content, msg.recipient,
									msg.callBackId));
					fileServer.findUserThread(msg.recipient).close();
				}
			}
		}
	}

	public void Announce(int type, String sender, String content) {
		Message msg = new Message(type, sender, content, "All", -1);
		for (int i = 0; i < clientCount; i++) {
			clients[i].send(msg);
		}
	}

	public void sendToGroup(int type, String sender, String content,
			String recipient) {
		String[] recip = recipient.split(Message.SECOND_LEVEL_SEPERATOR);
		for (String userName : recip) {
			if (findUserThread(userName) != null)
				findUserThread(userName).send(
						new Message(Constant.MESSAGE, sender, content,
								userName, -1));
		}
	}

	public void AnnounceWithoutSender(int type, String user, String sender,
			String content) {
		Message msg = new Message(type, sender, content, "All", -1);
		for (int i = 0; i < clientCount; i++) {
			if (!clients[i].username.equals(user))
				clients[i].send(msg);
		}

	}

	public void SendUserList(String toWhom, int callBack) {
		try {
			findUserThread(toWhom).send(
					new Message(Constant.USER_LIST, "SERVER", db.getUserList(),
							toWhom, callBack));
		} catch (Exception e) {
			logManager.addErrorLog(e.getMessage());

			findUserThread(toWhom).send(
					new Message(Constant.SERVER_ERROR, "SERVER",
							"Server Error: Error while connection database",
							toWhom, -1));
			ui.jTextArea1.append(e.getMessage());

		}
	}

	public void sendToUser(int type, String sender, String content,
			String receiver) {
		findUserThread(receiver).send(
				new Message(type, sender, content, receiver, -1));

	}

	private synchronized void remove(int ID) throws Exception {
		int pos = findClient(ID);
		if (pos >= 0) {
			ServerThread toTerminate = clients[pos];
			toTerminate.logout(toTerminate.username);
			db.setOnline(toTerminate.username, false);
			AnnounceWithoutSender(Constant.USER_LIST, toTerminate.username,
					"SERVER", db.getUserList());
			ui.jTextArea1.append("\nRemove:  " + ID + " at thread: " + pos);
			if (pos < clientCount - 1) {
				for (int i = pos + 1; i < clientCount; i++) {
					clients[i - 1] = clients[i];
				}
			}
			clientCount--;
			toTerminate.close();
			toTerminate = null;

		}
	}

	public ServerThread findUserThread(String usr) {
		for (int i = 0; i < clientCount; i++) {
			if (clients[i].username.equals(usr)) {
				return clients[i];
			}
		}
		return null;
	}

	private void addThread(Socket socket) {
		if (clientCount < clients.length) {
			clients[clientCount] = new ServerThread(this, socket);
			try {
				clients[clientCount].open();
				clients[clientCount].start();
				clientCount++;
			} catch (IOException ioe) {
				logManager.addErrorLog("\nError opening thread: " + ioe);
				ui.jTextArea1.append("\nError opening thread: " + ioe);
			}
		} else {
			ui.jTextArea1.append("\nClient refused: maximum " + clients.length
					+ " reached.");
			logManager.addErrorLog("\nClient refused: maximum "
					+ clients.length + " reached.");
		}
	}

	class CheckUser extends TimerTask {

		@Override
		public void run() {
			try {
				for (int i = 0; i < clientCount; i++) {
					if (clients[i] != null) {
						clients[i].send(new Message(Constant.IS_CLIENT_ACTIVE,
								"SERVER", "CHECK", clients[i].username, -1));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class ServerThread extends Thread {

		public SocketServer server = null;
		public Socket socket = null;
		public int ID = -1;
		public String username = "";
		public UserDetails userDetails;
		public ObjectInputStream streamIn = null;
		public ObjectOutputStream streamOut = null;
		private InputStreamReader is;
		private OutputStreamWriter os;
		public ServerFrame ui;
		private boolean isRunning;
		private boolean isLogin;

		public ServerThread(SocketServer _server, Socket _socket) {
			super();
			server = _server;
			socket = _socket;
			ID = socket.getPort();
			ui = _server.ui;
			ui.jTextArea1.append("\nClient added in thread: " + clientCount);
			logManager.addUndefineLog("Client added in thread: " + clientCount);
		}

		public void setOnline(boolean flag) throws Exception {
			db.setOnline(username, flag);
		}

		public synchronized void send(Message msg) {

			try {
				streamOut.writeUTF(msg.toString());
				streamOut.flush();
			} catch (IOException ex) {

				try {
					setOnline(false);
					remove(ID);
				} catch (Exception e) {
					ui.jTextArea1.append(e.getStackTrace() + "");
				}

			}

		}

		public int getID() {
			return ID;
		}

		@Override
		public void run() {
			ui.jTextArea1.append("\nServer Thread " + ID + " running.");
			logManager.addUndefineLog("Server Thread " + ID + " running.");
			isRunning = true;
			try {
				while (isRunning) {
					Message msg = new Message(streamIn.readUTF());
					server.handle(ID, msg);
				}
			} catch (EOFException eof) {
				try {
					server.remove(ID);
				} catch (Exception e) {
					System.err.println("Run: first catch");
					e.printStackTrace();
				}
			} catch (Exception e) {
				try {
					server.remove(ID);
				} catch (Exception e1) {
					System.err.println("Run: Second catch");
					e1.printStackTrace();
				}
				System.err.println("Run: third catch: " + "Connection Reset.");

			} finally {
				try {
					if (!socket.isClosed()) {
						streamOut.close();
						streamIn.close();
						socket.close();
					}
				} catch (IOException e) {
					System.err.println("Run: fourth catch");
					e.printStackTrace();
				} finally {
					interrupt();
				}
			}
		}

		public void open() throws IOException {

			streamOut = new ObjectOutputStream(socket.getOutputStream());
			streamOut.flush();
			streamIn = new ObjectInputStream(socket.getInputStream());
		}

		public void close() {
			isRunning = true;
		}

		public boolean isLogin() {
			return isLogin;
		}

		public synchronized void logout(String name) {
			isLogin = false;
			ui.jTextArea1.append("\nLogout Successfull: " + name);
			logManager.addGeneralLog(name, "Logout Successfull:");
		}

		public synchronized void login(String username) {
			isLogin = true;
			ui.jTextArea1.append("\nLogin Success: " + username);
			logManager.addGeneralLog(username, "User login Success");
		}
	}

}
