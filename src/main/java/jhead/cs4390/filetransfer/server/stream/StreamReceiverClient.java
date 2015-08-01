package jhead.cs4390.filetransfer.server.stream;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import jhead.cs4390.filetransfer.server.FileReceiver;
import jhead.cs4390.filetransfer.server.ReceiverClient;

public class StreamReceiverClient extends ReceiverClient implements Runnable {
  
  protected final SocketChannel channel;
  protected final Socket socket;
  protected final DataInputStream stream;
  
  public StreamReceiverClient(FileReceiver server,SocketChannel channel) throws IOException {
    super(server);
    
    this.channel = channel;
    
    socket = channel.socket();
    stream = new DataInputStream(socket.getInputStream());
  }
  
  @Override
  public void run() {
    onConnect();
    
    try {
      while (canReadData()) {
        parsePacket(stream);
      }
    } catch (EOFException ex) {
      // Do nothing
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    onDisconnect();
  }

  @Override
  public boolean canReadData() {
    return (channel.isOpen() && !socket.isClosed());
  }

  @Override
  public SocketAddress getRemoteEndpoint() {
    return socket.getRemoteSocketAddress();
  }
  
  @Override
  protected void onDisconnect() {
    try {
      if (channel.isOpen()) {
        channel.close();
      }
    } catch (IOException ex) { }
  }

  @Override
  protected void sendMessage(ByteBuffer buffer) throws IOException {
    channel.write(buffer);
  }

}
