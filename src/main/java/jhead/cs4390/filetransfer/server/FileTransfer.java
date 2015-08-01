package jhead.cs4390.filetransfer.server;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileTransfer {
  
  public static final String OUTPUT_DIR = "./output/";
  
  protected final int id;
  protected final String name;
  protected final long size;
  
  protected FileOutputStream stream;
  protected long bytesWritten = 0L;
  
  public FileTransfer(int id, String name, long size) throws IOException {
    this.id = id;
    this.name = name;
    this.size = size;
    
    stream = new FileOutputStream(OUTPUT_DIR + name);
  }
  
  public void append(byte[] data) throws IOException {
    stream.write(data);
    
    bytesWritten += data.length;
  }
  
  public String getFilename() {
    return name;
  }
  
  public boolean isComplete() {
    return (bytesWritten >= size);
  }
  
  public int getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
  
  public long getSize() {
    return size;
  }

}
