package jhead.cs4390.filetransfer.server.datagram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import jhead.cs4390.filetransfer.server.FileReceiver;

public class DatagramReceiver extends FileReceiver {
  
  public DatagramReceiver(int port) {
    super(port, DatagramChannel.class);
  }
  
  @Override
  protected void onListen() {
    System.out.println("UDP server bound to " + localAddress.toString());
  }
  
  @Override
  public void processRequests() throws IOException {
    DatagramChannel channel = (DatagramChannel) serverChannel;
    
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.receive(readBuffer);
    DatagramReceiverClient client;
    
    if (!clients.containsKey(remoteAddress)) {
      client = new DatagramReceiverClient(this, remoteAddress);
      clients.put(remoteAddress, client);
    } else {
      client = (DatagramReceiverClient) clients.get(remoteAddress);
    }
    
    client.enqueuePacket(readBuffer);
    client.parsePackets();
  }  

}
