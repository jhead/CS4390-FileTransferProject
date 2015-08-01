package jhead.cs4390.filetransfer.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import jhead.cs4390.filetransfer.FileTransferProject;

public abstract class FileSender {
  
  private static final int CHUNK_SIZE = 512;
  
  protected final InetSocketAddress remoteEndpoint;
  protected final InetAddress remoteAddress;
  protected final int remotePort;
  private final Random random;
  
  public FileSender(String host, int port) throws UnknownHostException {
    remoteAddress = InetAddress.getByName(host);
    remotePort = port;
    
    remoteEndpoint = new InetSocketAddress(remoteAddress, remotePort);
    
    random = new Random();
  }  
  
  public void send(String path) throws IOException, FileNotFoundException {
    File file = new File(path);
    
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    
    System.out.println("Sending file: " + path);
    
    initSocket();
    transferFile(file);
  }
  
  protected abstract void initSocket() throws IOException;
  
  protected void transferFile(File file) throws IOException {
    Path filePath = Paths.get(file.getAbsolutePath());
    
    int transferID = random.nextInt();
    long fileSize = file.length();
    
    System.out.println("Beginning transfer");
    sendBeginTransferPacket(transferID, file);
    
    System.out.println("Sending file data");
    sendFileDataPackets(transferID, file);
    
    System.out.println("Ending transfer");
    readAcknowledgement(transferID);
  }
  
  protected abstract void sendBeginTransferPacket(int transferID, File file) throws IOException;
  
  protected void sendFileDataPackets(int transferID, File file) throws IOException {
    Path filePath = Paths.get(file.getAbsolutePath());
    long fileSize = file.length();
    
    FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
      
    ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE + 13);
    long bytesWritten = 0;
    int payloadLength;

    while (bytesWritten < fileSize) {
      buffer.clear();
      payloadLength = (int) (bytesWritten + CHUNK_SIZE <= fileSize ? CHUNK_SIZE : fileSize - bytesWritten);

      // Header
      buffer.putInt(FileTransferProject.MAGIC); // Magic
      buffer.put((byte) 0x02); // Opcode
      buffer.putInt(transferID); // Transfer ID
      buffer.putInt(payloadLength); // Data length

      // Data (Payload)
      bytesWritten += fileChannel.read(buffer);

      buffer.flip();
      
      sendFileDataPacket(buffer);
    }
  }
  
  protected abstract void sendTransferCompletePacket(int transferID, File file) throws IOException;
  
  protected abstract void sendFileDataPacket(ByteBuffer buffer) throws IOException;
  
  protected abstract void readAcknowledgement(int transferID) throws IOException;
  
  public static ByteBuffer getBeginTransferPacket(int transferID, File file) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(byteStream);

    // Magic
    stream.writeInt(FileTransferProject.MAGIC);

    // Begin transfer opcode
    stream.writeByte(0x01);

    // Transfer ID
    stream.writeInt(transferID);

    // Filename 
    stream.writeUTF(file.getName());

    // File size
    stream.writeLong(file.length());

    return ByteBuffer.wrap(byteStream.toByteArray());
  }

  public static ByteBuffer getTransferCompletePacket(int transferID, File file) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(9);
      
    // Magic
    buffer.putInt(FileTransferProject.MAGIC);

    // Opcode
    buffer.put((byte) 0x03);

    // Transfer ID
    buffer.putInt(transferID);

    buffer.flip();
    return buffer;
  }
  
}
