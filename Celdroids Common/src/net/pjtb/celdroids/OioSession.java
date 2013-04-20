package net.pjtb.celdroids;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OioSession implements Session {
	private Socket socket;
	private Lock packetReadLock;
	private byte[] readBuf;

	private OioSession(final Socket socket) {
		this.socket = socket;
		packetReadLock = new ReentrantLock();
		readBuf = new byte[Constants.BUFFER_SIZE];

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!socket.isClosed()) {
					
				}
			}
		}, "socket-read");
	}

	@Override
	public PacketReader read() {
		return null;
	}

	@Override
	public PacketWriter write(short n) {
		return null;
	}

	@Override
	public PacketWriter write() {
		return null;
	}

	@Override
	public void close() {
		
	}

	public static OioSession createServer(SocketAddress addr, int timeout) {
		try {
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(addr);
			serverSocket.setSoTimeout(timeout);
			Socket socket;
			try {
				socket = serverSocket.accept();
			} catch (SocketTimeoutException ex) {
				return null;
			}
			return new OioSession(socket);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static OioSession createClient(SocketAddress addr, int timeout) {
		Socket socket = new Socket();
		try {
			socket.bind(null);
			try {
				socket.connect(addr, timeout);
			} catch (SocketTimeoutException ex) {
				return null;
			}
			return new OioSession(socket);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
