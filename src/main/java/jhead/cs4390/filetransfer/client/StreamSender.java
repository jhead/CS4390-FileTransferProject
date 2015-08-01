package jhead.cs4390.filetransfer.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class StreamSender extends FileSender {
  
  protected SocketAddress remoteSocketAddress;
  protected SocketChannel channel;
  protected Socket remoteSocket;
  
  public StreamSender(String host, int port) throws UnknownHostException {
    super(host, port);
    
    remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
  }
  
  @Override
  protected void initSocket() throws IOException {
    channel = SocketChannel.open(remoteSocketAddress);
    remoteSocket = channel.socket();
    
    System.out.println("Transport Protocol: TCP");
  }

  @Override
  protected void sendBeginTransferPacket(int transferID, File file) throws IOException {   
    ByteBuffer packet = FileSender.getBeginTransferPacket(transferID, file);
    
    channel.write(packet);
  }

  @Override
  protected void sendTransferCompletePacket(int transferID, File file) throws IOException {
    ByteBuffer packet = FileSender.getTransferCompletePacket(transferID, file);
    
    channel.write(packet);
  }

  @Override
  protected void sendFileDataPacket(ByteBuffer buffer) throws IOException {
    channel.write(buffer);
  }
  
   @Override
  protected void readAcknowledgement(int transferID) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    
    while (true) {
      channel.read(buffer);
      
      buffer.flip();

      buffer.getInt(); // Magic

      byte opcode = buffer.get(); // Opcode

      if (opcode != 0x03) continue;

      int ackTransferID = buffer.getInt(); // Transfer ID

      if (ackTransferID != transferID) continue;
      
      break;
    }
    
    System.out.println("Transfer " + transferID + " acknowledged by receiver!");
  }

}
 