package me.zee.SSHTest;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import javax.imageio.ImageIO;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Main {
	public static void main(String[] args) {
		String random = generateString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@^&*()-_", 32);
		//Easier to generate a huge random string than a small one and check if it already exists
		try {
			Robot robot = new Robot();
			String fileName = "temp", format = "png";
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle captureRect = new Rectangle(screenSize.width, screenSize.height);
			BufferedImage screenCap = robot.createScreenCapture(captureRect);
			ImageIO.write(screenCap, format, new File(fileName + "." + format));
			
			JSch jsch = new JSch();
			Session session = jsch.getSession(args[1], args[0], 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(args[2]);
			session.connect();
			
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.cd("/var/www/cm/s/");
			File f = new File("temp.png");
			FileInputStream fIS = new FileInputStream(f);
			sftpChannel.put(fIS, random + "." + format);
			
			fIS.close();
			sftpChannel.exit();
			session.disconnect();
			
			StringSelection selection = new StringSelection("http://craftmountain.net/s/" + random + "." + format);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(selection, selection);
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exc = sw.toString();
			
			e.printStackTrace();
			StringSelection selection = new StringSelection(exc);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		} finally {
			try {
				Files.deleteIfExists(Paths.get("temp.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String generateString(String characters, int length) {
	    Random gen = new Random();
		char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(gen.nextInt(characters.length()));
	    }
	    return new String(text);
	}
}