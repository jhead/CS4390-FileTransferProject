package jhead.cs4390.filetransfer.server.datagram;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import jhead.cs4390.filetransfer.server.FileReceiver;
import jhead.cs4390.filetransfer.server.ReceiverClient;

public class DatagramReceiverClient extends ReceiverClient {
  
  protected final InetSocketAddress remoteAddress;
  protected final LinkedList<ByteBuffer> packetQueue;
  
  public DatagramReceiverClient(FileReceiver server, InetSocketAddress remoteAddress) {
    super(server);
    
    this.remoteAddress = remoteAddress;
    packetQueue = new LinkedList<>();
  }
  
  @Override
  public boolean canReadData() {
    return true;
  }

  @Override
  public SocketAddress getRemoteEndpoint() {
    return remoteAddress;
  }
  
  public void enqueuePacket(ByteBuffer buffer) {
    ByteBuffer copy = ByteBuffer.allocate(buffer.capacity());
    
    buffer.rewind();
    copy.put(buffer);
    buffer.rewind();
    copy.flip();
    
    packetQueue.add(copy);
  }

  protected void parsePackets() throws EOFException, IOException {
    ByteBuffer buffer;
    DataInputStream stream;
    byte[] data;
      
    while (!packetQueue.isEmpty()) {
      buffer = packetQueue.removeFirst();
      data = buffer.array();
      stream = new DataInputStream(new ByteArrayInputStream(data));

      parsePacket(stream);
    }
  }

  @Override
  protected void sendMessage(ByteBuffer buffer) throws IOException {
    DatagramChannel channel = (DatagramChannel) server.getChannel();
    
    channel.send(buffer, remoteAddress);
  }

}
