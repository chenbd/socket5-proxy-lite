package com.xjd.socket.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by XJD on 12/28/14.
 */
public class TransferThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(TransferThread.class);

	InputStream in;
	OutputStream out;

	public TransferThread(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public void run() {
		try {
			byte[] buf = new byte[1024 * 2];
			int c;

			while ((c = in.read(buf)) != -1) {
				if (c > 0) {
					out.write(buf, 0, c);
					out.flush();
				}
			}
		} catch (IOException e) {
			log.error("transfer data from source to target exception.", e);
		}
	}
}
