package com.xjd.socket.proxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by XJD on 12/27/14.
 */
public class Test2 {
	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = new ServerSocket(1080);

		Socket socket = serverSocket.accept();
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		int b, c;
		byte[] rbuf;
		byte[] wbuf;

		rbuf = new byte[3];
		read(in, rbuf);

		wbuf = new byte[]{0x05, 0x00};
		write(out, wbuf);

		rbuf = new byte[4];
		read(in, rbuf);

		byte atyp = rbuf[3];

		byte[] hostByte = null;
		String host = null;
		if (atyp == 0x01) { // ipv4
			rbuf = new byte[4];
			read(in, rbuf);
			hostByte = rbuf;
			host = String.format("%d.%d.%d.%d", (rbuf[0] & 0xff), rbuf[1], (rbuf[2] & 0xff), rbuf[3]);
//			System.out.printf("House Address: %d.%d.%d.%d", (rbuf[0] & 0xff), rbuf[1], (rbuf[2] & 0xff), rbuf[3]);
//			System.out.printf("House Address: %d.%d.%d.%d", rbuf);
			System.out.printf("House Address: %s", host);
			System.out.println();

		} else if (atyp == 0x03) { // host name
			rbuf = new byte[1];
			read(in, rbuf);
			System.out.println("Host Name Length: " + rbuf[0]);

			hostByte = new byte[1 + rbuf[0]];
			hostByte[0] = rbuf[0];

			rbuf = new byte[rbuf[0]];
			read(in, rbuf);
			System.arraycopy(rbuf, 0, hostByte, 1, rbuf.length);
			host = new String(rbuf);
			System.out.println("House Name: " + host);

		} else if (atyp == 0x04) { // ipv6
			rbuf = new byte[16];
			read(in, rbuf);
			// TODO

		}

		rbuf = new byte[2];
		byte[] portByte = rbuf;
		read(in, rbuf);
		int port = ((int) rbuf[0] << 4) | rbuf[1];
		System.out.println("Port: " + port);

		wbuf = new byte[]{0x05, 0x00, 0x00, 0x01};

		wbuf[1] = 0x00;

		write(out, wbuf);

		write(out, hostByte);

		wbuf = new byte[2];
		int cport = socket.getLocalPort();
		if (cport > 255) {
			wbuf[0] = (byte) ((cport & 0xff) >> 4);
		} else {
			wbuf[0] = 0x00;
		}

		wbuf[1] = (byte) (cport & 0xff);
		write(out, portByte);

		Socket pSocket = new Socket(host, port);
		TmpThread t = new TmpThread();
		t.in = pSocket.getInputStream();
		t.out = out;
		t.start();

		/*BufferedWriter pbw = new BufferedWriter(new OutputStreamWriter(pSocket.getOutputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		System.out.println("============in==============");
		while ((line = br.readLine()) != null) {
			pbw.write(line);
			pbw.flush();
			System.out.println("<<<:" + line);
		}
		System.out.println("============in==============");*/
		byte[] buf = new byte[1024];
		int i;
		OutputStream pout = pSocket.getOutputStream();
		System.out.println("============in==============");
		while ((i = in.read(buf)) != -1) {
			if (i == 0) {
				continue;
			}
			pout.write(buf, 0, i);
			pout.flush();
		}
	}

	public static class TmpThread extends Thread {
		InputStream in;
		OutputStream out;

		@Override
		public void run() {
			try {
				byte[] buf = new byte[1024];
				int i;
				System.out.println("============out==============");
				while ((i = in.read(buf)) != -1) {
					if (i == 0) {
						continue;
					}
					out.write(buf, 0, i);
					out.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void read(InputStream in, byte[] buf) throws IOException {
		for (int i = 0; i < buf.length; i++) {
			int b = in.read();
			if (b == -1) {
				throw new RuntimeException("IO End");
			}
			buf[i] = (byte) (b & 0xFF);
		}
		print(true, buf);
	}

	public static void write(OutputStream out, byte[] buf) throws IOException {
		out.write(buf);
		out.flush();
		print(false, buf);
	}

	public static void print(boolean isIn, byte[] content) {
		if (isIn) {
			System.out.print("<<<:");
		} else {
			System.out.print(">>>:");
		}
		for (int i = 0; i < content.length; i++) {
			String hex = Integer.toHexString(content[i] & 0xff);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			hex = "0x" + hex;
			System.out.print(" " + hex);
		}
		System.out.println();
	}

}
