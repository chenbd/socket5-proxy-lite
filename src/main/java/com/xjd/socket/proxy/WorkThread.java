package com.xjd.socket.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by XJD on 12/28/14.
 */
public class WorkThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(WorkThread.class);

	private Socket sourceSocket;
	private Socket targetSocket;

	public WorkThread(Socket sourceSocket) {
		this.sourceSocket = sourceSocket;
	}

	@Override
	public void run() {
		try {
			targetSocket = shakeHand(sourceSocket);

			TransferThread s2t = new TransferThread(sourceSocket.getInputStream(), targetSocket.getOutputStream());
			TransferThread t2s = new TransferThread(targetSocket.getInputStream(), sourceSocket.getOutputStream());

			s2t.start();
			t2s.start();

			s2t.join();
			t2s.join();

		} catch (IOException e) {
			log.error("", e);
		} catch (InterruptedException e) {
			log.error("", e);
		} finally {
			closeSilently(sourceSocket);
			closeSilently(targetSocket);
			log.info("Close proxy for: {}:{}", sourceSocket.getInetAddress().getHostAddress(), sourceSocket.getPort());
		}
	}

	protected static void closeSilently(Socket socket) {
		if (socket == null) {
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {
			log.warn("", e);
		}
	}

	protected static Socket shakeHand(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		byte[] rbuf;
		byte[] wbuf;

		// first
		rbuf = new byte[3];
		read(in, rbuf);

		wbuf = new byte[]{0x05, 0x00};
		write(out, wbuf);

		// second
		// request
		rbuf = new byte[4];
		read(in, rbuf);

		byte atyp = rbuf[3];
		String host = null;
		int port = 0;

		if (atyp == 0x01) { // ipv4
			rbuf = new byte[4];
			read(in, rbuf);
			host = String.format("%d.%d.%d.%d", rbuf[0] & 0xFF, rbuf[1] & 0xFF, rbuf[2] & 0xFF, rbuf[3] & 0xFF);

		} else if (atyp == 0x04) { // ipv6
			rbuf = new byte[16];
			read(in, rbuf);
			StringBuilder sb = new StringBuilder(40);
			for (int i = 0; i < 16; i += 2) {
				sb.append(":" + byte2HexString(rbuf[i]) + byte2HexString(rbuf[i + 1]));
			}
			sb.deleteCharAt(0);
			host = sb.toString();

		} else if (atyp == 0x03) { // host name
			rbuf = new byte[1];
			read(in, rbuf);

			rbuf = new byte[rbuf[0]];
			read(in, rbuf);

			host = new String(rbuf);

		} else {
			throw new IOException("Wrong ATYP: " + byte2HexString(atyp));
		}
		log.info("host: {}", host);

		rbuf = new byte[2];
		read(in, rbuf);
		port = Integer.parseInt("" + (rbuf[0] & 0xFF) + (rbuf[1] & 0xFF));
		log.info("port: {}", port);

		// response
		wbuf = new byte[]{0x05, 0x00, 0x00, 0x01};
		Socket target = null;
		try {
			target = new Socket(host, port);
		} catch (IOException e) {
			wbuf[1] = 0x03;
			log.warn("Cannot connect to target: " + host + ":" + port, e);
		}
		write(out, wbuf);

		write(out, socket.getLocalAddress().getAddress());

		int lport = socket.getLocalPort();
		wbuf = new byte[2];
		wbuf[0] = (byte) ((lport >> 4) & 0xFF);
		wbuf[1] = (byte) (lport & 0xFF);
		write(out, wbuf);

		return target;

	}

	public static void read(InputStream in, byte[] buf) throws IOException {
		for (int i = 0; i < buf.length; i++) {
			int b = in.read();
			if (b == -1) {
				throw new IOException("Unexpected IO End");
			}
			buf[i] = (byte) (b & 0xFF);
		}
		log(true, buf);
	}

	public static void write(OutputStream out, byte[] buf) throws IOException {
		out.write(buf);
		out.flush();
		log(false, buf);
	}

	public static void log(boolean isIn, byte[] content) {
		if (log.isInfoEnabled()) {
			String txt = "";
			if (isIn) {
				txt += "<<<:";
			} else {
				txt += ">>>:";
			}
			for (int i = 0; i < content.length; i++) {
				txt += " " + byte2HexString(content[i]);
			}
			log.info(txt);
		}
	}

	public static String byte2HexString(byte b) {
		String txt = Integer.toHexString(b & 0xFF);
		if (txt.length() < 2) {
			txt = "0" + txt;
		}
		return "0x" + txt;
	}
}
