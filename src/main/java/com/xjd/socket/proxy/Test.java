package com.xjd.socket.proxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by XJD on 12/27/14.
 */
public class Test {
	public static void main(String[] args) throws IOException {
		System.out.println(Integer.toHexString(16));

		if (true) {
			return ;
		}

		ServerSocket serverSocket = new ServerSocket(1080);


		Socket socket = serverSocket.accept();

		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		byte[] buf = new byte[1024 * 2];
		int i, c = 0;
		String line = null;

		System.out.println("===============start================");

		/*while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}*/
		/*while((i = inputStream.read(buf)) != -1) {

		}*/

		while ((i = inputStream.read()) != -1) {
			System.out.println(i);
			c++;
			if (c == 3) {
				break;
			}
		}

		outputStream.write(5);
		outputStream.write(0);

		c = 0;
		while ((i = inputStream.read()) != -1) {
			System.out.println(i);
			c++;
			/*if (c == 3) {
				break;
			}*/
		}

		System.out.println("===============end================");
		socket.close();

	}
}
