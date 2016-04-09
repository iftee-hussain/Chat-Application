package com.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer implements Runnable {

	public ServerSocket server;
	public Socket socket;
	public int port;
	public String saveTo = "";
	private int clientCount = 0;
	public Thread thread = null;
	public ServerThread clients[];
	private static final int START = 1;
	private static final int OK = 2;
	private static final int COMPLETE = 3;
	private static final int ERROR = 4;
	private static final int UPLOADER = 5; // UPLOADER = CONNECT WITH UPLOADER
	private static final int DOWNLOADER = 6; // /UPLOADER = CONNECT WITH
												// DOWNLOADER
	private static final int HEADER = 10;
	private byte[] buffer = new byte[1024];

	public FileServer(int port) {

		clients = new ServerThread[100];
		try {
			server = new ServerSocket(port);
			port = server.getLocalPort();
			start();
			System.out.println("File Server start at port: " + port);
		} catch (IOException ex) {
			System.err.println("Exception : " + ex.getMessage());
		}
	}

	@Override
	public void run() {
		while (thread != null) {

			try {
				addThread(server.accept());

			} catch (Exception ioe) {
				System.err.println("\n" + ioe.getMessage());

			}
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

	private synchronized void addThread(Socket socket) {
		if (clientCount < clients.length) {
			clients[clientCount] = new ServerThread(this, socket);
			try {
				clients[clientCount].open();
				clients[clientCount].start();
				clientCount++;
			} catch (IOException ioe) {
			}
		} else {
		}
	}

	public void start() {

		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}

	}

	private void stop() {

		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	public void stopServer() {
		for (ServerThread t : clients) {
			if (t != null) {
				remove(t.ID);
			}
		}
		stop();
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public int findClient(int ID) {
		for (int i = 0; i < clientCount; i++) {
			if (clients[i].getID() == ID) {
				return i;
			}
		}
		return -1;
	}

	public synchronized void handle(int ID, int status) throws IOException {
		if (status == HEADER) {
			clients[findClient(ID)].username = clients[findClient(ID)].objectInputStream
					.readUTF();
			return;
		}

		ServerThread user = clients[findClient(ID)];

		ServerThread pairedUser = null;
		if (clients[findClient(ID)].ID_Pair != -1
				&& findClient(clients[findClient(ID)].ID_Pair) != -1)
			pairedUser = clients[findClient(clients[findClient(ID)].ID_Pair)];
		else
			new IOException("Paired user not found");

		if (pairedUser != null) {
			if (!((user.actAs == UPLOADER && pairedUser.actAs == DOWNLOADER) || (user.actAs == DOWNLOADER && pairedUser.actAs == UPLOADER))) {
				new IOException("User and paired user not correctly paired.");
			}
			if (user.actAs == UPLOADER) {
				if (status == START) {
					pairedUser.send(START);
				} else if (status == OK) {
					int lenght = user.inputStream.read(buffer);
					pairedUser.send(OK, buffer, lenght);
				} else if (status == COMPLETE) {
					pairedUser.send(status);
					pairedUser.stop = true;
					remove(findClient(clients[findClient(ID)].ID_Pair));
					user.stop = true;
					remove(ID);
				} else if (status == ERROR) {
					pairedUser.send(status);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					pairedUser.stop = true;
					remove(findClient(clients[findClient(ID)].ID_Pair));
					user.stop = true;
					remove(ID);
				} else {
					pairedUser.send(status);
				}
			} else if (user.actAs == DOWNLOADER) {
				pairedUser.send(status);
			} else {
				System.err
						.println("Error!!! User not an uploader nor downloader");
			}
		}
	}

	@SuppressWarnings("deprecation")
	public synchronized void remove(int ID) {
		int pos = findClient(ID);
		if (pos >= 0) {

			ServerThread toTerminate = clients[pos];

			if (pos < clientCount - 1) {
				for (int i = pos + 1; i < clientCount; i++) {
					clients[i - 1] = clients[i];
				}
			}
			clientCount--;
			toTerminate.close();
			toTerminate.stop();
			// System.out.println("Port: " + ID
			// + "is free now, Current client list size: " + clientCount);
		}
	}

	public void removePair(int ID1, int ID2) {
		remove(ID2);
		remove(ID1);
	}

	class ServerThread extends Thread {
		private FileServer server = null;
		private Socket socket = null;
		public int ID = -1;
		public int ID_Pair = -1;
		public int actAs = -1; // 0 for upload; 1 fro download; -1 for initial;
		public String username = "";
		InputStream inputStream = null;
		private OutputStream outputStream = null;
		public boolean stop = false;
		ObjectInputStream objectInputStream;

		public ServerThread(FileServer _server, Socket _socket) {
			super();
			server = _server;
			socket = _socket;
			ID = socket.getPort();
			// System.out.println("Client number: " + clientCount + " on port "
			// + ID);
		}

		public int getID() {
			return ID;
		}

		public void send(int status) throws IOException {
			outputStream.write(status);
		}

		public void send(int status, byte[] b, int len) throws IOException {
			outputStream.write(status);
			outputStream.flush();
			outputStream.write(b, 0, len);
			outputStream.flush();
		}

		public void run() {
			System.out.println("\nServer Thread " + ID + " running.");
			try {
				while (!stop) {

					int st = inputStream.read();
					if (st == ERROR) {
						clients[findClient(clients[findClient(ID)].ID_Pair)]
								.send(ERROR);
					}
					server.handle(ID, st);
				}

			} catch (Exception ioe) {
				try {
					clients[findClient(clients[findClient(ID)].ID_Pair)]
							.send(ERROR);
				} catch (IOException e) {
				}
				removePair(ID, ID_Pair);
			} finally {
				removePair(ID, ID_Pair);
			}
		}

		public void open() throws IOException {
			objectInputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = socket.getOutputStream();
			outputStream.flush();
			inputStream = socket.getInputStream();
		}

		public void close() {
			stop = true;
			try {
				if (socket != null)
					socket.close();
				if (inputStream != null)
					inputStream.close();
				if (outputStream != null)
					outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}