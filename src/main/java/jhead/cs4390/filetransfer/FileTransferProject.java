package jhead.cs4390.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import jhead.cs4390.filetransfer.client.DatagramSender;
import jhead.cs4390.filetransfer.client.StreamSender;
import jhead.cs4390.filetransfer.server.datagram.DatagramReceiver;
import jhead.cs4390.filetransfer.server.stream.StreamReceiver;

public class FileTransferProject {
  
  public static final int MAGIC = 0x00122430;

  public static void main(String[] args) {
    if (!validateArguments(args)) {
      usage();
    }
    
    String type = args[0].substring(2).toLowerCase();
    
    try {
      if (type.equals("server")) {
        int port = Integer.parseInt(args[1]);
        
        StreamReceiver tcp = new StreamReceiver(port);
        DatagramReceiver udp = new DatagramReceiver(port);
          
        new Thread(tcp).start();
        new Thread(udp).start();
      } else if (type.equals("tcp")) {
        new StreamSender(args[1], Integer.parseInt(args[2])).send(args[3]);
      } else if (type.equals("udp")) {
        new DatagramSender(args[1], Integer.parseInt(args[2])).send(args[3]);
      } else if (type.equals("client")) {
        File file = new File(args[3]);
        
        if (!file.exists()) {
          throw new FileNotFoundException();
        }
        
        // Use TCP if file is larger than 25 MB
        if (file.length() > 1024 * 1024 * 25) {
          new StreamSender(args[1], Integer.parseInt(args[2])).send(args[3]);
        } else {
          new DatagramSender(args[1], Integer.parseInt(args[2])).send(args[3]);
        }
      }
      
    } catch (UnknownHostException ex) {
      System.err.println("Failed to resolve IP address. Please provide a valid IP or hostname.");
    } catch (FileNotFoundException ex) {
      System.err.println("File does not exist. Please provide a valid path.");
    } catch (Exception ex) {
      System.err.println("Unhandled exception");
      ex.printStackTrace();
    }
  }
  
  private static boolean validateArguments(String[] args) {
    boolean argsValid = true;
    
    try {
      argsValid &= (args.length == 2 || args.length == 4);
      argsValid &= (args[0].toLowerCase().equals("--server") ? args.length == 2 : true);
      argsValid &= (args[0].toLowerCase().equals("--client") ? args.length == 4 : true);
      argsValid &= (args[0].toLowerCase().equals("--tcp") ? args.length == 4 : true);
      argsValid &= (args[0].toLowerCase().equals("--udp") ? args.length == 4 : true);
    } catch (Exception ex) {
      return false;
    }
    
    return argsValid;
  }
  
  private static void usage() {
    System.err.println("Usage:");
    System.err.println("          java -jar FileTransferProject.jar --server <port>");
    System.err.println("          java -jar FileTransferProject.jar --client <host> <port> <file>");
    System.err.println("          java -jar FileTransferProject.jar --tcp <host> <port> <file>");
    System.err.println("          java -jar FileTransferProject.jar --udp <host> <port> <file>");
    System.exit(1);
  }
  
}
