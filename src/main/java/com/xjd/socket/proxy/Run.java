package com.xjd.socket.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by XJD on 12/28/14.
 */
public class Run {
	public static Logger log = LoggerFactory.getLogger(Run.class);

	public static void main(String[] args) {
		int port = 1080;

		if (args != null && args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		try {
			ServerSocket ss = new ServerSocket(port);
			log.info("Socket5 proxy start at: 127.0.0.1:" + port);

			while (true) {
				Socket so = ss.accept();

				new WorkThread(so).start();
				log.info("Get proxy request from: " + so.getInetAddress().getHostAddress() + ":" + so.getPort());
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
