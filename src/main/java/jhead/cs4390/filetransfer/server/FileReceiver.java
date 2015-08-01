package jhead.cs4390.filetransfer.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.util.HashMap;
import java.util.Map;

public abstract class FileReceiver implements Runnable {
  
  public static final int READ_BUFFER_SIZE = 4096;
  
  protected final InetSocketAddress localAddress;
  protected final Map<Object, ReceiverClient> clients;
  protected NetworkChannel serverChannel;  
  protected final ByteBuffer readBuffer;
  
  private final Class channelType;
  private boolean running = false;
  
  public FileReceiver(int port, Class channelType) {
    this.channelType = channelType;
    
    readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
    localAddress = new InetSocketAddress(port);
    clients = new HashMap<>();
  }
  
  @Override
  public void run() {
    try {
      listen();

      running = true;
      while (running) {
        processRequests();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
    shutdown();
  }
  
  protected void listen() throws IOException {
    try {     
      Method openChannelMethod = channelType.getMethod("open");
      serverChannel = (NetworkChannel) openChannelMethod.invoke(null);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new IOException("Invalid Channel type provided: " + channelType.getName());
    }
    
    serverChannel.bind(localAddress);
    
    onListen();
  }
  
  public boolean isRunning() {
    return running;
  }
  
  public void shutdown() {
    running = false;

    try {
      serverChannel.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  
  protected abstract void processRequests() throws IOException;
  
  protected abstract void onListen();
  
  protected void setChannel(NetworkChannel channel) {
    serverChannel = channel;
  }
  
  public NetworkChannel getChannel() {
    return serverChannel;
  }
  
}
