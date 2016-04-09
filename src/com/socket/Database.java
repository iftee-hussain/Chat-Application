package com.socket;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Database {
	public Database() throws Exception {
		String sql = "UPDATE `logininfo` SET `ONLINE`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setBoolean(1, false);
			stmt.executeUpdate();
		} catch (SQLException e) {
			try {
				throw new Exception("Server Error! Please try again Leter. "
						+ e.getMessage());
			} catch (Exception e1) {
				throw new Exception(e1.getMessage());
			}
		}
	}

	public boolean userExists(String username) throws Exception {
		String sql = "SELECT * FROM `logininfo`  WHERE `username`=? ";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public UserDetails login(String username, String password) throws Exception {

		String sql = "SELECT `NAME`, `USERTYPE`, `IP`,  `EMAIL`, `PHONE`, `POSTAL_ADDRESS`, `COUNTRY`,"
				+ " `ADMIN_PANEL_ACCESS`,  `ACTIVE` FROM `logininfo`, `userinfo` WHERE"
				+ "`logininfo`.`USERNAME`=? AND `logininfo`.`PASSWORD`=? AND"
				+ "`logininfo`.`USERNAME`=`userinfo`.`USERNAME`";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			stmt.setString(2, getSecureHash(password));
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				UserDetails iu = new UserDetails(username, res.getString(1),
						res.getInt(2), res.getString(3), res.getString(4),
						res.getString(5), res.getString(6), res.getString(7),
						res.getBoolean(8), res.getBoolean(9));
				return iu;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public UserDetails getUserDetail(String username) throws Exception {

		String sql = "SELECT `NAME`, `USERTYPE`, `IP`,  `EMAIL`, `PHONE`, `POSTAL_ADDRESS`, `COUNTRY`,"
				+ " `ADMIN_PANEL_ACCESS`,  `ACTIVE` FROM `logininfo`, `userinfo` WHERE"
				+ "`logininfo`.`USERNAME`=? AND"
				+ "`logininfo`.`USERNAME`=`userinfo`.`USERNAME`";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				UserDetails iu = new UserDetails(username, res.getString(1),
						res.getInt(2), res.getString(3), res.getString(4),
						res.getString(5), res.getString(6), res.getString(7),
						res.getBoolean(8), res.getBoolean(9));
				return iu;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}

	}

	public String getNotificationList(String username) {
		String sql;

		sql = "SELECT username_friend, status FROM notification WHERE `username`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			ResultSet res = stmt.executeQuery();
			String str = "";
			while (res.next()) {
				str += res.getString(1) + Message.THIRD_LEVEL_SEPERATOR
						+ res.getBoolean(2) + Message.SECOND_LEVEL_SEPERATOR;
			}
			return str;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return "";
	}

	public String getUserList() throws Exception {
		String sql;

		sql = "SELECT `logininfo`.`USERNAME`, `NAME`, `COUNTRY`, `ONLINE`,`USERTYPE` FROM `logininfo`, `userinfo` WHERE"
				+ "`logininfo`.`USERNAME`=`userinfo`.`USERNAME` ORDER BY  `logininfo`.`USERNAME`";

		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			ResultSet res = stmt.executeQuery();
			String str = "";
			while (res.next()) {
				str += res.getString(1) + Message.THIRD_LEVEL_SEPERATOR
						+ res.getString(2) + Message.THIRD_LEVEL_SEPERATOR
						+ res.getString(3) + Message.THIRD_LEVEL_SEPERATOR
						+ res.getBoolean(4) + Message.THIRD_LEVEL_SEPERATOR
						+ res.getInt(5) + Message.SECOND_LEVEL_SEPERATOR;
			}
			return str;
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public void setOnline(String userName, boolean flag) throws Exception {
		String sql = "UPDATE `logininfo` SET `ONLINE`=? WHERE `USERNAME`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setBoolean(1, flag);
			stmt.setString(2, userName);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public void addUser(UserDetails userDetails, String pass) throws Exception {
		String sql = "INSERT INTO  `logininfo`(username, password,ip,active,ADMIN_PANEL_ACCESS) VALUES(?, ?,?,?,?)";

		String sql1 = "INSERT INTO `userinfo`(`USERNAME`, `USERTYPE`, `NAME`, `EMAIL`, `COUNTRY`, `PHONE`, `POSTAL_ADDRESS`)"
				+ "VALUES (?,?,?,?,?,?,?)";
		String sql2 = "INSERT INTO `useraccess`(`username`, `sendfriendreq`, "
				+ "`approvefriendreq`, `approveremreq`, `sendmsg`, `rcvmsg`, "
				+ "`calltransfer`,`filesharing`,`groupchat`) "
				+ "VALUES (?,1,1,1,1,1,1,1,1)";
		String sql3 = "INSERT INTO `useraccess`(`username`, `sendfriendreq`, "
				+ "`approvefriendreq`, `approveremreq`, `sendmsg`, `rcvmsg`, "
				+ "`calltransfer`,`filesharing`,`groupchat`) "
				+ "VALUES (?,0,0,0,0,0,0,0,0)";
		String sql4 = "SELECT username FROM `logininfo`  WHERE `username`!=? ";
		String sql5 = "INSERT INTO `chatper`(username,chatwith,permission)  values(?,?,?) ";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);

				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);

				java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt3 = conn.prepareStatement(sql3,
						Statement.RETURN_GENERATED_KEYS);

				java.sql.PreparedStatement stmt4 = conn.prepareStatement(sql4,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt5 = conn.prepareStatement(sql5,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, userDetails.userName);
			stmt.setString(2, pass);
			stmt.setString(3, userDetails.ip);
			stmt.setBoolean(4, userDetails.active);
			stmt.setBoolean(5, userDetails.adminPanelAccess);
			stmt.executeUpdate();

			stmt1.setString(1, userDetails.userName);
			stmt1.setInt(2, userDetails.userType);
			stmt1.setString(3, userDetails.fullName);
			stmt1.setString(4, userDetails.email);
			stmt1.setString(5, userDetails.country);
			stmt1.setString(6, userDetails.phone);
			stmt1.setString(7, userDetails.postalAddress);
			stmt1.executeUpdate();

			if (userDetails.userType == 1) {
				stmt2.setString(1, userDetails.userName);
				stmt2.executeUpdate();

				stmt4.setString(1, userDetails.userName);
				ResultSet rs = stmt4.executeQuery();
				while (rs.next()) {
					stmt5.setString(1, userDetails.userName);
					stmt5.setString(2, rs.getString(1));
					stmt5.setBoolean(3, Boolean.parseBoolean("True"));
					stmt5.executeUpdate();

					stmt5.setString(1, rs.getString(1));
					stmt5.setString(2, userDetails.userName);
					stmt5.setBoolean(3, Boolean.parseBoolean("True"));
					stmt5.executeUpdate();

				}

			} else {

				stmt3.setString(1, userDetails.userName);
				stmt3.executeUpdate();
				stmt4.setString(1, userDetails.userName);
				ResultSet rs = stmt4.executeQuery();
				while (rs.next()) {
					stmt5.setString(1, userDetails.userName);
					stmt5.setString(2, rs.getString(1));
					stmt5.setBoolean(3, Boolean.parseBoolean("False"));
					stmt5.executeUpdate();

					stmt5.setString(1, rs.getString(1));
					stmt5.setString(2, userDetails.userName);
					stmt5.setBoolean(3, Boolean.parseBoolean("False"));
					stmt5.executeUpdate();

				}
			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}

	}

	public String getHistory(String sender, String recipient) throws Exception {
		String sql = "SELECT sender, recipient, message, date_time FROM "
				+ "`history` WHERE (sender=? and recipient=?)OR(sender=? and recipient = ?) ORDER BY date_time asc";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			stmt.setString(2, recipient);
			stmt.setString(3, recipient);
			stmt.setString(4, sender);
			ResultSet rs = stmt.executeQuery();
			String str = "";
			while (rs.next()) {
				str = str + rs.getString(1) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(2) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(3) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getTimestamp(4) + Message.THIRD_LEVEL_SEPERATOR

						+ Message.SECOND_LEVEL_SEPERATOR;

			}
			return str;

		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}
	public String getHistory(String sender) throws Exception {
		String sql = "SELECT sender, recipient, message, date_time FROM "
				+ "`history` WHERE sender=? ORDER BY date_time asc";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			ResultSet rs = stmt.executeQuery();
			String str = "";
			while (rs.next()) {
				str = str + rs.getString(1) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(2) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(3) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getTimestamp(4) + Message.THIRD_LEVEL_SEPERATOR

						+ Message.SECOND_LEVEL_SEPERATOR;

			}
			return str;

		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public void saveHistory(String sender, String recipient, String message)
			throws Exception {
		String sql = "INSERT INTO  `history`(	sender, recipient, message) VALUES(?, ?, ?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			stmt.setString(2, recipient);
			stmt.setString(3, message);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
	}

	public static String getSecureHash(String passwordToHash) {

		String generatedPassword = null;
		try {
			String salt = "duttatex";
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes());
			byte[] bytes = md.digest(passwordToHash.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	public String getSpecificUsers(int type) {
		String users = "";
		String sql = "SELECT username from userinfo where usertype =?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setInt(1, type);
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				users = users + res.getString(1)
						+ Message.SECOND_LEVEL_SEPERATOR;

			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return users;

	}

	public String getSpecificUserPermission(String content) {
		String rs = "";
		String sql = "SELECT active, admin_panel_access,`sendfriendreq`, "
				+ "`approvefriendreq`, `approveremreq`, `sendmsg`, `rcvmsg`, `calltransfer`,`filesharing`,"
				+ "`groupchat` FROM `useraccess` ,logininfo "
				+ "where logininfo.username = useraccess.username and logininfo.username=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, content);
			ResultSet res = stmt.executeQuery();

			if (res.next()) {

				for (int i = 1; i <= 10; i++) {
					rs += res.getBoolean(i) + Message.SECOND_LEVEL_SEPERATOR;
				}
			}

			String s = getChatPermission(content);
			rs = rs + Message.THIRD_LEVEL_SEPERATOR + s;

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return rs;
	}

	public String getChatPermission(String sender) {
		String sql = "SELECT chatwith, permission from chatper where username =?";
		String chatwiths = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				chatwiths = chatwiths + res.getString(1)
						+ Message.SECOND_LEVEL_SEPERATOR + res.getBoolean(2)
						+ Message.SECOND_LEVEL_SEPERATOR;

			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return chatwiths;
		// TODO Auto-generated method stub

	}

	public void setNotification(String recipient, String sender) {
		String sql = "SELECT username, username_friend FROM notification WHERE username =? AND username_friend=?";
		String sql1 = "UPDATE `notification` SET `status`= 1 WHERE `username`=? AND `username_friend`=?";
		String sql2 = "INSERT INTO `notification`(`username`, `username_friend`, `status`) VALUES (?,?, 1)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			stmt.setString(2, recipient);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				stmt1.setString(1, sender);
				stmt1.setString(2, recipient);
				stmt1.executeUpdate();
			} else {
				stmt2.setString(1, sender);
				stmt2.setString(2, recipient);
				stmt2.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

	}

	public void clearNotification(String sender, String recipient) {
		String sql = "UPDATE `notification` SET `status`= 0 WHERE `username`=? AND `username_friend`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, sender);
			stmt.setString(2, recipient);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

	}

	public String updatePermissions(String content) throws Exception {
		String dpers[] = content.split(Message.THIRD_LEVEL_SEPERATOR);

		String lpres[] = dpers[0].split(Message.SECOND_LEVEL_SEPERATOR);

		String rpres[] = dpers[1].split(Message.SECOND_LEVEL_SEPERATOR);
		//System.out.println("rpress : " + rpres[0] + " " + rpres[1]);

		String sql1 = "UPDATE useraccess SET sendfriendreq=?,"
				+ "approvefriendreq=?,approveremreq=?,"
				+ "calltransfer=?,sendmsg=?,rcvmsg=? ,filesharing=?, groupchat=? WHERE username=?";
		String sql2 = "UPDATE logininfo SET active=? , admin_panel_access=? WHERE  username=?";
		String sql3 = "UPDATE chatper SET  permission=? WHERE username=? and chatwith=? ";

		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt3 = conn.prepareStatement(sql3,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt1.setBoolean(1, Boolean.parseBoolean(lpres[3]));
			stmt1.setBoolean(2, Boolean.parseBoolean(lpres[4]));
			stmt1.setBoolean(3, Boolean.parseBoolean(lpres[5]));
			stmt1.setBoolean(4, Boolean.parseBoolean(lpres[6]));
			stmt1.setBoolean(5, Boolean.parseBoolean(lpres[7]));
			stmt1.setBoolean(6, Boolean.parseBoolean(lpres[8]));
			stmt1.setBoolean(7, Boolean.parseBoolean(lpres[9]));
			stmt1.setBoolean(8, Boolean.parseBoolean(lpres[10]));
			stmt1.setString(9, lpres[0]);
			stmt1.executeUpdate();

			stmt2.setBoolean(1, Boolean.parseBoolean(lpres[1]));
			stmt2.setBoolean(2, Boolean.parseBoolean(lpres[2]));
			stmt2.setString(3, lpres[0]);
			stmt2.executeUpdate();

			for (int i = 0; i < rpres.length; i = i + 2) {

				stmt3.setBoolean(1, Boolean.parseBoolean(rpres[i + 1]));
				stmt3.setString(2, lpres[0]);
				stmt3.setString(3, rpres[i]);
				stmt3.executeUpdate();
			}
			for (int i = 0; i < rpres.length; i = i + 2) {

				stmt3.setBoolean(1, Boolean.parseBoolean(rpres[i + 1]));
				stmt3.setString(2, rpres[i]);
				stmt3.setString(3, lpres[0]);

				stmt3.executeUpdate();
			}

		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}

		return null;

	}

	public String deleteUsers(Message msg) throws Exception {
		// ArrayList<String> delUsers = new ArrayList<>();
		String[] delUsers = msg.content.split(Message.SECOND_LEVEL_SEPERATOR);
		for (int i = 0; i < delUsers.length; i++)
			System.out.print("****" + delUsers[i] + "****");
		String sql1 = "delete from logininfo where username=?";
		String sql2 = "delete from userinfo where username=?";
		String sql3 = "delete from useraccess where username=?";
		String sql4 = "delete from notification where username=?";
		String sql5 = "delete from chatper where username=? or chatwith=?";
		String sql6 = "delete from history where sender=? or recipient=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt3 = conn.prepareStatement(sql3,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt4 = conn.prepareStatement(sql4,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt5 = conn.prepareStatement(sql5,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt6 = conn.prepareStatement(sql6,
						Statement.RETURN_GENERATED_KEYS);) {

			for (int i = 0; i < delUsers.length; i++) {
				stmt1.setString(1, delUsers[i]);
				stmt1.executeUpdate();

				stmt2.setString(1, delUsers[i]);
				stmt2.executeUpdate();

				stmt3.setString(1, delUsers[i]);
				stmt3.executeUpdate();

				stmt4.setString(1, delUsers[i]);
				stmt4.executeUpdate();

				stmt5.setString(1, delUsers[i]);
				stmt5.setString(2, delUsers[i]);
				stmt5.executeUpdate();

				stmt6.setString(1, delUsers[i]);
				stmt6.setString(2, delUsers[i]);
				stmt6.executeUpdate();
			}

		} catch (SQLException e) {
			throw new Exception("Deletion Error");
		}
		return "Successfully deleted " + delUsers.length + " users";
	}

	public String blockUsers(Message msg) throws Exception {
		String[] blockUsers = msg.content.split(Message.SECOND_LEVEL_SEPERATOR);
		for (int i = 0; i < blockUsers.length; i++)
			System.out.print("****" + blockUsers[i] + "****");
		String sql1 = "update chatper set permission = 0 where username=? and chatwith=? ";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);) {

			if (blockUsers.length <= 0)
				throw new Exception("Nothing Blocked....");
			for (int i = 0; i < blockUsers.length; i++) {
				stmt.setString(1, msg.sender);
				stmt.setString(2, blockUsers[0]);
				stmt.executeUpdate();

				stmt.setString(1, blockUsers[0]);
				stmt.setString(2, msg.sender);
				stmt.executeUpdate();

			}

		} catch (SQLException e) {
			throw new Exception("Can't Block. Please try again later...");
		}

		return "Succeessfully blocked....";
	}

	public String editUsers(Message msg) throws Exception {
		String[] msgs = msg.content.split(Message.SECOND_LEVEL_SEPERATOR);

		String sql1 = "update logininfo set username=? where username=?";
		String sql2 = "update userinfo set username=?,  usertype=? , name=? where username=?";
		String sql3 = "update useraccess set username=?  where username=?";
		String sql4 = "update chatper set username=?  where username=?";
		String sql5 = "update chatper set chatwith=?  where chatwith=?";
		String sql6 = "update notification set username=?  where username=?";
		String sql7 = "update history set sender=?  where sender=?";
		String sql8 = "update history set recipient=?  where recipient=?";
		String sql9 = "update notification set username_friend=?  where username_friend=?";

		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt3 = conn.prepareStatement(sql3,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt4 = conn.prepareStatement(sql4,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt5 = conn.prepareStatement(sql5,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt6 = conn.prepareStatement(sql6,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt7 = conn.prepareStatement(sql7,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt8 = conn.prepareStatement(sql8,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt9 = conn.prepareStatement(sql9,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2,
						Statement.RETURN_GENERATED_KEYS);

		) {
			System.out.println(msgs.length);
			for (int i = 0; i < msgs.length; i = i + 4) {
				// System.out.print("1111"
				// +" "+msgs[i+1]+" "+msgs[i+2]+" "+msgs[i+3]+" "+msgs[i+4]
				// +" "+msgs[i+4]);
				stmt1.setString(1, msgs[i + 1]);
				stmt1.setString(2, msgs[i]);
				stmt1.executeUpdate();

				stmt3.setString(1, msgs[i + 1]);
				stmt3.setString(2, msgs[i]);
				stmt3.executeUpdate();

				stmt4.setString(1, msgs[i + 1]);
				stmt4.setString(2, msgs[i]);
				stmt4.executeUpdate();

				stmt5.setString(1, msgs[i + 1]);
				stmt5.setString(2, msgs[i]);
				stmt5.executeUpdate();

				stmt6.setString(1, msgs[i + 1]);
				stmt6.setString(2, msgs[i]);
				stmt6.executeUpdate();

				stmt7.setString(1, msgs[i + 1]);
				stmt7.setString(2, msgs[i]);
				stmt7.executeUpdate();

				stmt8.setString(1, msgs[i + 1]);
				stmt8.setString(2, msgs[i]);
				stmt8.executeUpdate();

				stmt9.setString(1, msgs[i + 1]);
				stmt9.setString(2, msgs[i]);
				stmt9.executeUpdate();

				stmt2.setString(1, msgs[i + 1]);
				stmt2.setInt(2, Integer.parseInt(msgs[i + 3]));
				stmt2.setString(3, msgs[i + 2]);
				stmt2.setString(4, msgs[i]);
				stmt2.executeUpdate();

			}

		} catch (SQLException e) {
			e.printStackTrace();
			// throw new Exception("Can't Update. Please try again later...");
		}

		return "Succeessfully updated....";

	}

	public void createSession(String user1, String user2) {
		String sql = "INSERT INTO  `session`(user1, user2, date) VALUES(?, ?, ?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(1, first);
			stmt.setString(2, second);
			stmt.setTimestamp(3, getCurrentTimeStamp());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void updateSession(String user1, String user2) {
		String sql = "UPDATE `session` SET `date`= ? WHERE `user1`=? AND `user2`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(2, first);
			stmt.setString(3, second);
			stmt.setTimestamp(1, getCurrentTimeStamp());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public Timestamp getSessionTime(String user1, String user2) {
		String sql = "SELECT `date` FROM `session` WHERE `user1`=? AND `user2`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(1, first);
			stmt.setString(2, second);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return res.getTimestamp(1);
			}
		} catch (SQLException e) {
			new Exception(e.getMessage());
		}
		return null;

	}

	private static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}

	public boolean check() {
		String sql = "SELECT * from others";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			ResultSet res = stmt.executeQuery();
			res.last();
			//System.out.println(res.getRow());
			res.first();
			if (res.next()) {
				return true;
			}
		} catch (SQLException e) {
			new Exception(e.getMessage());
		}
		return false;
	}

	public String update_password(Message msg) {
		String sql = "update logininfo set password=? where username=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, msg.content);
			stmt.setString(2, msg.sender);
			stmt.executeUpdate();
			return "Password Successfully Updated";

		} catch (SQLException e) {
			return "Password Can't be Changed";
		}

	}

	public void update_friend_req(Message msg) {
		String sql = "Insert into friendreq (who,whom) values (?,?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, msg.sender);
			stmt.setString(2, msg.content);
			stmt.executeUpdate();

		} catch (SQLException e) {

		}

	}

	public String getFriendRequests() throws Exception {
		String sql = "SELECT * FROM `friendreq` ";
		String r = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				r = r + res.getString("who") + Message.THIRD_LEVEL_SEPERATOR
						+ res.getString("whom")
						+ Message.SECOND_LEVEL_SEPERATOR;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}
		return r;

	}

	public boolean isOnline(String username) {
		String sql = "SELECT online FROM `logininfo`  WHERE `username`=? ";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return res.getBoolean(1);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}

	// >>>>>>>>>>>>>>Call Transfer<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public String getTemporaryFriendShips() {
		String sql = "SELECT * from temporary_friendship";
		String str = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				str += res.getString(1) + Message.SECOND_LEVEL_SEPERATOR
						+ res.getString(2) + Message.FIRST_LEVEL_SEPERATOR;
			}
		} catch (SQLException e) {
			new Exception(e.getMessage());
		}
		return str;
	}

	public boolean checkChatPermission(String username, String chatwith) {
		String sql = "SELECT permission FROM `chatper`  WHERE `username`=? AND chatwith=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			String user1, user2;
			if (username.compareTo(chatwith) == 1) {
				user1 = username;
				user2 = chatwith;
			} else {
				user1 = chatwith;
				user2 = username;
			}
			stmt.setString(1, user1);
			stmt.setString(2, user2);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return res.getBoolean(1);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}

	public void setTemporaryFriendShip(String user1, String user2)
			throws Exception {
		String sql = "INSERT INTO  `temporary_friendship`(user1, user2) VALUES(?, ?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(1, first);
			stmt.setString(2, second);
			if (!checkChatPermission(first, second)
					&& !checkTemporaryFriendShip(user1, user2)) {
				stmt.executeUpdate();
				setChatPermission(first, second, true);
			}
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public void clearTemporaryFriendShip(String user1, String user2)
			throws Exception {
		String sql = "DELETE FROM `temporary_friendship` WHERE `user1`=? AND `user2`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(1, first);
			stmt.setString(2, second);
			stmt.executeUpdate();
			setChatPermission(first, second, false);

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public boolean checkTemporaryFriendShip(String user1, String user2)
			throws Exception {
		String sql = "SELECT * FROM `temporary_friendship` WHERE `user1`=? AND `user2`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			String first, second;
			if (user1.compareTo(user2) == 1) {
				first = user1;
				second = user2;
			} else {
				first = user2;
				second = user1;
			}
			stmt.setString(1, first);
			stmt.setString(2, second);
			ResultSet res = stmt.executeQuery();
			if (res.next())
				return true;

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
		return false;
	}

	public void setChatPermission(String user1, String user2, boolean permission)
			throws Exception {
		String sql = "UPDATE `chatper` SET `permission`=? WHERE `username`=? AND `chatwith`=?";
		String sql1 = "UPDATE `chatper` SET `permission`=? WHERE `username`=? AND `chatwith`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setBoolean(1, permission);
			stmt.setString(2, user1);
			stmt.setString(3, user2);

			stmt1.setBoolean(1, permission);
			stmt1.setString(2, user2);
			stmt1.setString(3, user1);

			stmt.executeUpdate();
			stmt1.executeUpdate();

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public String getFilteredText() throws Exception {
		String sql = "SELECT `filteredtext` FROM `others`";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				result = res.getString(1);
			}

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
		return result;
	}

	public String setFilteredText(String txt) throws Exception {
		String sql = "UPDATE `others` SET `filteredtext`=?";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setString(1, txt);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
		return result;
	}

	// <<<<<<<<<<<<<<<<<<<<<<Group>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
	public void createGroup(String txt) throws Exception {
		String sql = "INSERT INTO `group_list`(`groupname`) VALUES (?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setString(1, txt);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public void deleteGroup(String content) throws Exception {
		String sql = "DELETE FROM `group_list` WHERE `groupname`=?";
		String sqlDeleteFormPermission = "DELETE FROM `group_permission` WHERE `groupname`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(
						sqlDeleteFormPermission,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setString(1, content);
			stmt1.setString(1, content);
			stmt1.executeUpdate();
			if (stmt.executeUpdate() != 1) {
				throw new Exception("Group not found.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Internal Server Error");
		}

	}

	public String getGroupNames(Message msg) throws Exception {
		String sqlForAdmin = "SELECT `groupname` FROM `group_list` WHERE 1";
		String sql = "SELECT `groupname` FROM `group_permission` WHERE `username`=?";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(
						sqlForAdmin, Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			ResultSet rs;
			if (msg.recipient.equals("1")) {
				rs = stmt1.executeQuery();
			} else {
				stmt.setString(1, msg.sender);
				rs = stmt.executeQuery();
			}
			while (rs.next()) {
				result += rs.getString(1) + Message.SECOND_LEVEL_SEPERATOR;
			}
			//System.out.println(result);
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Internal Server Error");
		}
	}

	public String getGroupHistory(String username, String groupName) {
		String sql = "SELECT  `username`, `message`, `date_time` FROM `group_history`"
				+ " WHERE `groupname`=?";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, groupName);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				result += rs.getString(1) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(2) + Message.THIRD_LEVEL_SEPERATOR
						+ rs.getString(3) + Message.SECOND_LEVEL_SEPERATOR;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return result;
	}

	public String getMemberListFromGroup(Message msg) throws Exception {
		String sql = "SELECT  `username` FROM `group_permission` WHERE `groupname`=?";
		String sql1 = "SELECT `username` FROM `useraccess` WHERE `groupchat`=1";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stm1 = conn.prepareStatement(sql1,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, msg.content);
			ResultSet rs = stmt.executeQuery();
			ResultSet rs1 = stm1.executeQuery();
			while (rs.next()) {
				result += rs.getString(1) + Message.THIRD_LEVEL_SEPERATOR;
			}
			result += Message.SECOND_LEVEL_SEPERATOR;
			while (rs1.next()) {
				result += rs1.getString(1) + Message.THIRD_LEVEL_SEPERATOR;
			}
			result += Message.SECOND_LEVEL_SEPERATOR;
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Internal Server Error");
		}
	}

	public ArrayList<String> getMemberList(String content) {

		String sql = "SELECT  `username` FROM `group_permission` WHERE `groupname`=?";
		ArrayList<String> arr = new ArrayList<>();
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, content);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return arr;
	}

	public void addMemberToGroup(Message msg) throws Exception {
		String sql = "INSERT INTO `group_permission`(`groupname`, `username`) VALUES (?,?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setString(1, msg.content);
			stmt.setString(2, msg.recipient);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public void setGroupNotification(String username, String group, boolean flag) {
		String check = "SELECT `notification` FROM `group_notification` WHERE `username`=? AND `groupname` = ?";
		String update = "UPDATE `group_notification` SET `notification`= ? WHERE `username`=? AND `groupname`=?";
		String insert = "INSERT INTO `group_notification`(`username`, `groupname`, `notification`) VALUES (?,?,?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmtCheck = conn.prepareStatement(
						check, Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmtUpdate = conn.prepareStatement(
						update, Statement.RETURN_GENERATED_KEYS);
				java.sql.PreparedStatement stmtInsert = conn.prepareStatement(
						insert, Statement.RETURN_GENERATED_KEYS);) {

			stmtCheck.setString(1, username);
			stmtCheck.setString(2, group);
			ResultSet rs = stmtCheck.executeQuery();
			if (rs.next()) {
				stmtUpdate.setBoolean(1, flag);
				stmtUpdate.setString(2, username);
				stmtUpdate.setString(3, group);
				stmtUpdate.executeUpdate();
			} else {
				stmtInsert.setString(1, username);
				stmtInsert.setString(2, group);
				stmtInsert.setBoolean(3, flag);
				stmtInsert.executeUpdate();
			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public String getGroupNotification(String username) {
		String sql = "SELECT  `groupname` FROM `group_notification` WHERE `username`=? AND `notification`=1";
		String result = "";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmtCheck = conn.prepareStatement(
						sql, Statement.RETURN_GENERATED_KEYS);) {

			stmtCheck.setString(1, username);
			ResultSet rs = stmtCheck.executeQuery();
			while (rs.next()) {
				result += rs.getString(1) + Message.SECOND_LEVEL_SEPERATOR;
			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return result;
	}

	public void deleteMemberFromGroup(String groupName, String username)
			throws Exception {
		String sql = "DELETE FROM `group_permission` WHERE `groupname`=? AND `username`=?";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {

			stmt.setString(1, groupName);
			stmt.setString(2, username);
			if (stmt.executeUpdate() != 1) {
				throw new Exception("Group not found.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Internal Server Error");
		}

	}

	public void saveGroupHistory(String sender, String groupName, String content)
			throws Exception {
		String sql = "INSERT INTO `group_history`(`groupname`, `username`, `message`) VALUES (?,?, ?)";
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, groupName);
			stmt.setString(2, sender);
			stmt.setString(3, content);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new Exception("Server Error! Please try again Leter. "
					+ e.getMessage());
		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	public String updateFriendReq(String content) throws Exception {
		String sql = "UPDATE `chatper` SET `permission`= true where username=? and chatwith=?";
		String[] test = content.split(Message.THIRD_LEVEL_SEPERATOR);

		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);

		) {

			for (int i = 0; i < test.length; i++) {
				String[] test2 = test[i].split(Message.SECOND_LEVEL_SEPERATOR);
				for (int j = 0; j < test2.length; j = j + 2) {
					stmt1.setString(1, test2[j]);
					stmt1.setString(2, test2[j + 1]);
					stmt1.executeUpdate();

					stmt1.setString(1, test2[j + 1]);
					stmt1.setString(2, test2[j]);
					stmt1.executeUpdate();
				}

			}
			deleteFriendReq(content);
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}

		return "";
	}

	public void deleteFriendReq(String content) throws Exception {
		String sql = "delete from `friendreq` where who=? and whom=?";
		String[] test = content.split(Message.THIRD_LEVEL_SEPERATOR);

		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);

		) {

			for (int i = 0; i < test.length; i++) {
				String[] test2 = test[i].split(Message.SECOND_LEVEL_SEPERATOR);
				for (int j = 0; j < test2.length; j = j + 2) {
					stmt1.setString(1, test2[j]);
					stmt1.setString(2, test2[j + 1]);
					stmt1.executeUpdate();

				}

			}
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}

	}

	public String updateMyProfile(Message msg) throws Exception {
		String content = msg.content;
		String sql = "UPDATE `userinfo` SET `NAME`=?,`EMAIL`=?,"
				+ "`COUNTRY`=?,`PHONE`=?,`POSTAL_ADDRESS`=? `ip`=? WHERE `USERNAME`=?";
		UserDetails ud = new UserDetails(content);
		try (Connection conn = DBUtil.getConnection();
				java.sql.PreparedStatement stmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);

		) {
			stmt.setString(1, ud.fullName);
			stmt.setString(2, ud.email);
			stmt.setString(3, ud.country);
			stmt.setString(4, ud.phone);
			stmt.setString(5, ud.postalAddress);
			stmt.setString(6, ud.ip);
			stmt.setString(7, msg.sender);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		return "Successfully updated profile....";
	}

}
