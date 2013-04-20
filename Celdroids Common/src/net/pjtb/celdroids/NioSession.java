package net.pjtb.celdroids;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NioSession implements Session {
	private static final Charset asciiEncoder = Charset.forName("US-ASCII");

	private class PacketReader implements Session.PacketReader {
		private boolean waitingForPayload;

		/* package-private */ boolean prepare() {
			try {
				if (!waitingForPayload) {
					if (channel.read(readBuf) == -1) {
						NioSession.this.close();
						return false;
					}
					if (readBuf.remaining() != 0)
						return false; //not enough bytes
					readBuf.flip();
					short packetLength = readBuf.getShort();
					readBuf.clear();
					if (packetLength > readBuf.remaining())
						throw new RuntimeException("Read buffer too small. Needed " + packetLength + " bytes, have " + readBuf.remaining());
					readBuf.limit(packetLength);
					waitingForPayload = true;
				}
				if (channel.read(readBuf) == -1) {
					NioSession.this.close();
					return false;
				}
				if (readBuf.remaining() != 0)
					return false; //not enough bytes
				readBuf.flip();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public byte[] getBytes(int n) {
			byte[] bytes = new byte[n];
			readBuf.get(bytes);
			return bytes;
		}

		@Override
		public byte getByte() {
			return readBuf.get();
		}

		@Override
		public short getShort() {
			return readBuf.getShort();
		}

		@Override
		public int getInt() {
			return readBuf.getInt();
		}

		@Override
		public long getLong() {
			return readBuf.getLong();
		}

		@Override
		public char getAsciiChar() {
			return (char) getByte();
		}

		@Override
		public float getFloat() {
			return readBuf.getFloat();
		}

		@Override
		public double getDouble() {
			return readBuf.getDouble();
		}

		@Override
		public String getAsciiString(int n) {
			//uses O(2n) memory, but takes half as long than alternatives
			byte[] bytes = getBytes(n);
			char[] ret = new char[n];
			for (int x = 0; x < n; x++)
				ret[x] = (char) bytes[x];
			return String.valueOf(ret);
		}

		@Override
		public String getLengthPrefixedAsciiString() {
			return getAsciiString(readBuf.getShort());
		}

		@Override
		public void close() {
			readBuf.clear();
			readBuf.limit(2);
			waitingForPayload = false;
			packetLock.unlock();
		}
	}

	private class PacketWriter implements Session.PacketWriter {
		private final List<ByteBuffer> queuedWrites;
		private ByteBuffer writeBuf;
		/**
		 * Ensures that queuedWrites does not contain duplicate ByteBuffers.
		 */
		private boolean fastWriteBufInQueue;
		/**
		 * Ensures that packets are sent in order. While using fastWriteBuf
		 * is preferred if it is not full, we cannot use it if
		 *  1. queuedWrites is not empty, and
		 *  2. fastWriteBuf exists in queuedWrites, and
		 *  3. fastWriteBuf is not the last element in queuedWrites.
		 */
		private boolean canUseFastWriteBuf;

		/* package-private */ PacketWriter() {
			queuedWrites = new ArrayList<ByteBuffer>();
			canUseFastWriteBuf = true;
		}

		/* package-private */ void prepare(short n) {
			if (fastWriteBuf.remaining() < n + 2 && canUseFastWriteBuf)
				writeBuf = fastWriteBuf;
			else
				writeBuf = ByteBuffer.allocate(n + 2);
			writeBuf.putShort(n);
		}

		@Override
		public void putBytes(byte... b) {
			writeBuf.put(b);
		}

		@Override
		public void putByte(byte b) {
			writeBuf.put(b);
		}

		@Override
		public void putShort(short s) {
			writeBuf.putShort(s);
		}

		@Override
		public void putInt(int i) {
			writeBuf.putInt(i);
		}

		@Override
		public void putLong(long l) {
			writeBuf.putLong(l);
		}

		@Override
		public void putAsciiChar(char c) {
			putByte((byte) c);
		}

		@Override
		public void putFloat(float f) {
			writeBuf.putFloat(f);
		}

		@Override
		public void putDouble(double d) {
			writeBuf.putDouble(d);
		}

		@Override
		public void putAsciiString(int n, String s) {
			byte[] bytes = new byte[n];
			for (int i = 0; i < bytes.length && i < s.length(); i++)
				bytes[i] = (byte) s.charAt(i);
			putBytes(bytes);
		}

		@Override
		public void putLengthPrefixedAsciiString(String s) {
			putShort((short) s.length());
			putAsciiString(s.length(), s);
		}

		/* package-private */ boolean flush() throws IOException {
			boolean success = true;
			for (Iterator<ByteBuffer> iter = queuedWrites.iterator(); success && iter.hasNext(); ) {
				ByteBuffer buf = iter.next();
				buf.flip();
				channel.write(buf);
				buf.compact();
				if (buf.remaining() != 0) {
					success = false;
				} else {
					iter.remove();
					if (buf == fastWriteBuf) {
						fastWriteBufInQueue = false;
						canUseFastWriteBuf = true;
					}
				}
			}
			if (success) {
				SelectionKey key = channel.keyFor(selector);
				key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
			}
			return success;
		}

		/* package-private */ void sendOrQueue(ByteBuffer buf) throws IOException {
			boolean flushed = true;
			boolean bufFlushed = (buf == fastWriteBuf && fastWriteBufInQueue);
			if (!queuedWrites.isEmpty())
				flushed = flush();
			if (flushed && !bufFlushed) {
				buf.flip();
				channel.write(buf);
				buf.compact();
				flushed = (buf.remaining() == 0);
			}
			if (!flushed) {
				if (buf == fastWriteBuf) {
					if (!fastWriteBufInQueue) {
						if (queuedWrites.isEmpty()) {
							SelectionKey key = channel.keyFor(selector);
							key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
						}
						queuedWrites.add(buf);
						fastWriteBufInQueue = true;
					}
				} else {
					if (queuedWrites.isEmpty()) {
						SelectionKey key = channel.keyFor(selector);
						key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
					}
					queuedWrites.add(buf);
					if (fastWriteBufInQueue)
						canUseFastWriteBuf = false;
				}
			}
		}

		@Override
		public void close() {
			try {
				sendOrQueue(writeBuf);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				packetLock.unlock();
			}
		}
	}

	private class BufferedPacketWriter implements Session.PacketWriter {
		private byte[] buffer;
		private int index;

		/* package-private */ BufferedPacketWriter() {
			buffer = new byte[0x40];
		}

		/* package-private */ void prepare() {
			index = 2;
		}

		private void grow(int min) {
			int increase = Math.max(buffer.length / 2, min);
			byte[] copy = new byte[buffer.length + increase];
			System.arraycopy(buffer, 0, copy, 0, index);
			buffer = copy;
		}

		@Override
		public void putBytes(byte... b) {
			if (index + b.length >= buffer.length + 1)
				grow(b.length);

			System.arraycopy(b, 0, buffer, index, b.length);
			index += b.length;
		}

		@Override
		public void putByte(byte b) {
			if (index == buffer.length)
				grow(1);

			buffer[index++] = b;
		}

		@Override
		public void putShort(short s) {
			putBytes(
				(byte) ((s & 0xFF)),
				(byte) ((s >>> 8) & 0xFF)
			);
		}

		@Override
		public void putInt(int i) {
			putBytes(
				(byte) ((i & 0xFF)),
				(byte) ((i >>> 8) & 0xFF),
				(byte) ((i >>> 16) & 0xFF),
				(byte) ((i >>> 24) & 0xFF)
			);
		}

		@Override
		public void putLong(long l) {
			putBytes(
				(byte) ((l & 0xFF)),
				(byte) ((l >>> 8) & 0xFF),
				(byte) ((l >>> 16) & 0xFF),
				(byte) ((l >>> 24) & 0xFF),
				(byte) ((l >>> 32) & 0xFF),
				(byte) ((l >>> 40) & 0xFF),
				(byte) ((l >>> 48) & 0xFF),
				(byte) ((l >>> 56) & 0xFF)
			);
		}

		@Override
		public void putAsciiChar(char c) {
			putByte((byte) c);
		}

		@Override
		public void putFloat(float f) {
			putInt(Float.floatToRawIntBits(f));
		}

		@Override
		public void putDouble(double d) {
			putLong(Double.doubleToRawLongBits(d));
		}

		@Override
		public void putAsciiString(int n, String s) {
			byte[] space = new byte[n];
			if (s != null) {
				byte[] ascii = s.getBytes(asciiEncoder);
				System.arraycopy(ascii, 0, space, 0, Math.min(s.length(), n));
			}
			putBytes(space);
		}

		@Override
		public void putLengthPrefixedAsciiString(String s) {
			putShort((short) s.length());
			putBytes(s.getBytes(asciiEncoder));
		}

		@Override
		public void close() {
			try {
				ByteBuffer buf;
				int packetSize = index;
				index = 0;
				putShort((short) (packetSize - 2));
				if (packetSize == buffer.length)
					buf = ByteBuffer.wrap(buffer);
				else
					buf = ByteBuffer.wrap(buffer, 0, packetSize);
				buf.limit(packetSize);
				buf.position(packetSize);
				writer.sendOrQueue(buf);

				if (buffer.length > Constants.BUFFER_SIZE)
					buffer = new byte[0x40]; //don't keep large buffer allocated after a very long packet
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				packetLock.unlock();
			}
		}
	}

	private Selector selector;
	private SocketChannel channel;
	private Lock packetLock;
	private Iterator<SelectionKey> unprocessedKeys;
	private ByteBuffer readBuf, fastWriteBuf;
	private PacketReader reader;
	private PacketWriter writer;
	private BufferedPacketWriter unknownSizeWriter;

	private NioSession(Selector selector, SocketChannel channel) {
		this.selector = selector;
		this.channel = channel;
		packetLock = new ReentrantLock();
		unprocessedKeys = Collections.<SelectionKey>emptySet().iterator();
		readBuf = ByteBuffer.allocateDirect(Constants.BUFFER_SIZE);
		fastWriteBuf = ByteBuffer.allocateDirect(Constants.BUFFER_SIZE);
		reader = new PacketReader();
		writer = new PacketWriter();
		unknownSizeWriter = new BufferedPacketWriter();

		readBuf.order(ByteOrder.BIG_ENDIAN);
		readBuf.limit(2);
	}

	private boolean processSelectionKey() {
		SelectionKey key = unprocessedKeys.next();
		unprocessedKeys.remove();
		assert key.channel() == channel;
		if (key.isValid() && key.isWritable()) {
			try {
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (key.isValid() && key.isReadable())
			return reader.prepare();
		return false;
	}

	@Override
	public PacketReader read() {
		boolean keepLocked = false;
		packetLock.lock();
		try {
			if (unprocessedKeys.hasNext()) {
				if (processSelectionKey()) {
					keepLocked = true;
					return reader;
				}
				return null;
			}

			int actions = selector.selectNow();
			if (actions == 0)
				//no packets received since last call here
				return null;

			unprocessedKeys = selector.selectedKeys().iterator();
			if (processSelectionKey()) {
				keepLocked = true;
				return reader;
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (!keepLocked)
				packetLock.unlock();
		}
	}

	@Override
	public PacketWriter write(short n) {
		packetLock.lock();
		writer.prepare(n);
		return writer;
	}

	@Override
	public BufferedPacketWriter write() {
		packetLock.lock();
		unknownSizeWriter.prepare();
		return unknownSizeWriter;
	}

	public void close() {
		
	}

	public static NioSession createClient(SocketAddress addr, int timeout) {
		SocketChannel channel = null;
		Selector selector = null;
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(addr);

			selector = Selector.open();
			SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
			int actions = selector.select(timeout);
			if (actions == 0) {
				//connect timed out
				channel.close();
				channel = null;
				selector.close();
				selector = null;
				return null;
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
				SelectionKey selected = iter.next();
				iter.remove();
				if (selected == key && selected.isValid() && selected.isConnectable())
					if (channel.isConnectionPending())
						channel.finishConnect();
			}
			key.interestOps(SelectionKey.OP_READ);
			return new NioSession(selector, channel);
		} catch (IOException e) {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException ex) {
					
				}
			}
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException ex) {
					
				}
			}
			e.printStackTrace();
			return null;
		}
	}

	public static NioSession createServer(SocketAddress addr, int timeout) {
		ServerSocketChannel channel = null;
		Selector selector = null;
		SocketChannel clientChannel = null;
		try {
			channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(addr);

			selector = Selector.open();
			SelectionKey key = channel.register(selector, SelectionKey.OP_ACCEPT);
			int actions = selector.select(timeout);
			if (actions == 0) {
				//accept timed out
				channel.close();
				channel = null;
				selector.close();
				selector = null;
				return null;
			}
			Set<SelectionKey> keys = selector.selectedKeys();
			for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
				SelectionKey selected = iter.next();
				iter.remove();
				if (selected == key && selected.isValid() && selected.isAcceptable())
					clientChannel = channel.accept();
			}
			key.cancel();
			clientChannel.register(selector, SelectionKey.OP_READ);
			return new NioSession(selector, clientChannel);
		} catch (IOException e) {
			if (clientChannel != null) {
				try {
					clientChannel.close();
				} catch (IOException ex) {
					
				}
			}
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException ex) {
					
				}
			}
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException ex) {
					
				}
			}
			e.printStackTrace();
			return null;
		}
	}
}
