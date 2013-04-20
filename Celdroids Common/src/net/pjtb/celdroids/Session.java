package net.pjtb.celdroids;

//reuse PacketReaders, don't want to allocate a new object for every packet
//read when we have tight resource constraints
public interface Session {
	public interface PacketReader {
		public byte[] getBytes(int n);

		public byte getByte();

		public short getShort();

		public int getInt();

		public long getLong();

		public char getAsciiChar();

		public float getFloat();

		public double getDouble();

		public String getAsciiString(int n);

		public String getLengthPrefixedAsciiString();

		public void close();
	}

	public interface PacketWriter {
		public void putBytes(byte... b);

		public void putByte(byte b);

		public void putShort(short s);

		public void putInt(int i);

		public void putLong(long l);

		public void putAsciiChar(char c);

		public void putFloat(float f);

		public void putDouble(double d);

		public void putAsciiString(int n, String s);

		public void putLengthPrefixedAsciiString(String s);

		public void close();
	}

	public PacketReader read();

	public PacketWriter write(short n);

	public void close();

	public PacketWriter write();
}
