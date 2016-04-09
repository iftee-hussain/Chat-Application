package com.socket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogManager {

	private String dir = System.getProperty("user.home") + "/duttatex/log";
	private String interactionLog = System.getProperty("user.home")
			+ "/duttatex/log/userInteractionLog.txt";
	private String undefineLog = System.getProperty("user.home")
			+ "/duttatex/log/undefineLog.txt";
	private String errorLog = System.getProperty("user.home")
			+ "/duttatex/log/errorLog.txt";
	private File interactionLogFile;
	private File errorLogFile;
	private File undefineLogFile;

	public LogManager() {
		interactionLogFile = new File(interactionLog);
		errorLogFile = new File(errorLog);
		undefineLogFile = new File(undefineLog);
		File directory = new File(dir);
		if (!directory.exists()) {
			if (directory.mkdir()) {

			} else {
				directory.mkdirs();

			}
		}
		try {
			interactionLogFile.createNewFile();
			errorLogFile.createNewFile();
			undefineLogFile.createNewFile();
			FileOutputStream o = new FileOutputStream(interactionLogFile);
			o.write("".getBytes());
			o.flush();
			o.close();
			o = new FileOutputStream(errorLogFile);
			o.write("".getBytes());
			o.flush();
			o.close();
			o = new FileOutputStream(undefineLogFile);
			o.write("".getBytes());
			o.flush();
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (FileWriter fileWritterInteraction = new FileWriter(
				interactionLogFile, true);
				BufferedWriter bufferWritterInteraction = new BufferedWriter(
						fileWritterInteraction);

				FileWriter fileWriterErrorLog = new FileWriter(errorLogFile,
						true);
				BufferedWriter bufferWriterErrorLog = new BufferedWriter(
						fileWriterErrorLog);

				FileWriter fileWritUndefine = new FileWriter(undefineLogFile,
						true);
				BufferedWriter bufferWritterUndefine = new BufferedWriter(
						fileWritUndefine);) {
			String st = String.format("%-100s%-100s%-20s%s", "Username",
					"Interaction", "Date", System.lineSeparator());
			String st1 = String
					.format("--------------------------------------------------------------------------------%s",
							System.lineSeparator());

			bufferWritterInteraction.append(st);
			bufferWritterInteraction.append(st1);

			String st2 = String.format("%-200s%-100s%s", "Error Details",
					"Date", System.lineSeparator());
			bufferWriterErrorLog.append(st2);
			bufferWriterErrorLog.append(st1);

			String str3 = String.format("Error Detaisl \t\t\t\t Date%s",
					System.lineSeparator());
			bufferWritterUndefine.append(str3);
			bufferWritterUndefine.append(st1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addGeneralLog(String username, String details) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("hh:mm a dd, MMM");
		try (FileWriter fileWriter = new FileWriter(interactionLogFile, true);
				BufferedWriter writer = new BufferedWriter(fileWriter);) {
			String st = String.format("%-100s%-100s%-20s%s", username, details,
					df.format(cal.getTime()), System.lineSeparator());
			fileWriter.append(st);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addErrorLog(String errorDetails) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("hh:mm a dd, MMM");
		try (FileWriter fileWriter = new FileWriter(interactionLogFile, true);
				BufferedWriter writer = new BufferedWriter(fileWriter);) {
			String st = String.format("%-200s%-100s%s", errorDetails,
					df.format(cal.getTime()), System.lineSeparator());
			fileWriter.append(st);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addUndefineLog(String log) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("hh:mm a dd, MMM");
		try (FileWriter fileWriter = new FileWriter(interactionLogFile, true);
				BufferedWriter writer = new BufferedWriter(fileWriter);) {
			String st = String.format("%s\t\t\t%-20s%s", log,
					df.format(cal.getTime()), System.lineSeparator());
			fileWriter.append(st);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
