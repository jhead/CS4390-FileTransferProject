package jhead.cs4390.filetransfer.server.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import jhead.cs4390.filetransfer.server.FileReceiver;

/**
 *
 * @author Justin Head (jx122430@utdallas.edu)
 */
public class StreamReceiver extends FileReceiver {
  
  protected Selector selector;
  
  public StreamReceiver(int port) {
    super(port, ServerSocketChannel.class);
  }
  
  @Override
  protected void listen() throws IOException {
    super.listen();
    
    ServerSocketChannel serverChannel = (ServerSocketChannel) this.serverChannel;
    serverChannel.configureBlocking(false);
    
    selector = Selector.open();   
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
  }
  
  @Override
  protected void onListen() {
    System.out.println("TCP server bound to " + localAddress.toString());
  }
  
  @Override
  protected void processRequests() {
    try {
      selector.select();

      Iterator selectedKeys = selector.selectedKeys().iterator();

      while (selectedKeys.hasNext()) {   
        SelectionKey key = (SelectionKey) selectedKeys.next();
        selectedKeys.remove();

        if (!key.isValid()) continue;

        // New client socket connection initializing
        if (key.isAcceptable()) {
          acceptConnection(key);
          continue;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void acceptConnection(SelectionKey key) throws IOException {
    // This should be the same as our main serverChannel
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    
    // Client socket channel
    SocketChannel clientChannel = serverChannel.accept();
    
    StreamReceiverClient client = new StreamReceiverClient(this, clientChannel);
    clients.put(clientChannel, client);
    
    new Thread(client).start();
  }
  
}
