package jhead.cs4390.filetransfer.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramSender extends FileSender {
  
  protected DatagramChannel channel;

  public DatagramSender(String host, int port) throws UnknownHostException {
    super(host, port);
  }
  
  @Override
  protected void initSocket() throws IOException {
    channel = DatagramChannel.open();
    
    System.out.println("Transport Protocol: UDP");
  }
  
  private void sendDatagram(ByteBuffer buffer) throws IOException {
    channel.send(buffer, remoteEndpoint);
  }

  @Override
  protected void sendBeginTransferPacket(int transferID, File file) throws IOException {
    ByteBuffer buffer = FileSender.getBeginTransferPacket(transferID, file);
    
    sendDatagram(buffer);
  }

  @Override
  protected void sendTransferCompletePacket(int transferID, File file) throws IOException {
    ByteBuffer buffer = FileSender.getTransferCompletePacket(transferID, file);
    
    sendDatagram(buffer);
  }

  @Override
  protected void sendFileDataPacket(ByteBuffer buffer) throws IOException {
    sendDatagram(buffer);
  }

  @Override
  protected void readAcknowledgement(int transferID) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    
    while (true) {
      InetSocketAddress remote = (InetSocketAddress) channel.receive(buffer);

      if (!remote.equals(this.remoteEndpoint)) {
        continue;
      }
      
      buffer.flip();
      
      buffer.getInt(); // Magic

      byte opcode = buffer.get(); // Opcode

      if (opcode != 0x03) continue;

      int ackTransferID = buffer.getInt(); // Transfer ID

      if (ackTransferID != transferID) continue;
      
      break;
    }
    
    System.out.println("Transfer " + transferID + " acknowledged by receiver!");
    
    System.exit(0);
  }

}
