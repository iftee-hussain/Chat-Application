package com.socket;

import java.util.TimerTask;

public class BackgroundService extends TimerTask {
	SocketServer server;

	public BackgroundService(SocketServer s) {
		server = s;
	}

	@Override
	public void run() {
		String result = "";
		try {
			result = server.db.getTemporaryFriendShips();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			if (!result.equals("")) {
				// System.out.println("Thread checking: ");
				String pairs[] = result.split(Message.FIRST_LEVEL_SEPERATOR);
				for (String us : pairs) {
					String users[] = us.split(Message.SECOND_LEVEL_SEPERATOR);

					if (!server.db.isOnline(users[0])
							&& !server.db.isOnline(users[1])) {

						server.db.clearTemporaryFriendShip(users[0], users[1]);
						//System.out.println("Cleared");
					}
					// System.out.print(users[0]+"<->"+users[1]+",");
				}
				// System.out.println(" are temporary.");
			} else {
				// System.out.println("Thread checking: "+"no temporary pair");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			;
		}

	}
}
