package jhead.cs4390.filetransfer.server;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import jhead.cs4390.filetransfer.FileTransferProject;

public abstract class ReceiverClient {
  
  protected final Map<Integer, FileTransfer> transfers;
  protected final FileReceiver server;
  
  public ReceiverClient(FileReceiver server) {
    this.server = server;
    transfers = new HashMap<>();
  }
  
  public void addTransfer(FileTransfer transfer) {
    System.out.println("[" + getRemoteEndpoint() + "] Transfer started: " + transfer.getName() + ", " + transfer.getSize() + " bytes, ID " + transfer.getId());
    
    transfers.put(transfer.getId(), transfer);
  }
  
  public FileTransfer getTransfer(int id) {
    return transfers.get(id);
  }
  
  public abstract boolean canReadData();
  
  public abstract SocketAddress getRemoteEndpoint();
  
  protected void onConnect() {
    System.out.println("[" + getRemoteEndpoint() + "] Connected");
  }
  
  protected void onDisconnect() {
    System.out.println("[" + getRemoteEndpoint() + "] Disconnected");
  }
  
  protected abstract void sendMessage(ByteBuffer buffer) throws IOException;
  
  protected void acknowledgeTransfer(int transferID) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(9);
    
    buffer.putInt(FileTransferProject.MAGIC);
    buffer.put((byte) 0x03);
    buffer.putInt(transferID);
    
    buffer.flip();
    
    sendMessage(buffer);
  }
  
  protected void parsePacket(DataInputStream stream) throws EOFException, IOException {  
    // Magic identifier
    int magic = stream.readInt();

    if (magic != FileTransferProject.MAGIC) {
      throw new IOException("[" + getRemoteEndpoint() + "] Unsupported protocol: " + magic);
    }

    // Opcode
    int opcode = stream.readByte();

    // Transfer Begin
    if (opcode == 0x01) {
      // Transfer ID
      int id = stream.readInt();

      // Filename
      String fileName = stream.readUTF();

      // File size in bytes
      long fileSize = stream.readLong();

      addTransfer(new FileTransfer(id, fileName, fileSize));
      return;
    }

    // Transfer data chunk
    if (opcode == 0x02) {
      // Transfer ID
      int id = stream.readInt();

      // Data length
      int length = stream.readInt();

      // Data
      byte[] buffer = new byte[length];
      stream.read(buffer, 0, buffer.length);

      FileTransfer transfer = getTransfer(id);
      
      if (transfer == null) {
        System.out.println("[" + getRemoteEndpoint() + "] Invalid transfer ID " + id);
        return;
      }
      
      transfer.append(buffer);

      if (transfer.isComplete()) {
        System.out.println("[" + getRemoteEndpoint() + "] Transfer complete: " + transfer.getName());
        acknowledgeTransfer(id);
      }
      
      return;
    }
    
    if (opcode == 0x03) {
      int id = stream.readInt();
      
      FileTransfer transfer = getTransfer(id);
      
      if (transfer == null) {
        System.out.println("[" + getRemoteEndpoint() + "] Invalid transfer ID " + id);
        return;
      }
      
      System.out.println("[" + getRemoteEndpoint() + "] Transfer completion acknowledged: " + transfer.getName());
      
      return;
    }
  }
  

}
