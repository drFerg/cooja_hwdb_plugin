import java.io.IOException;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import hwdb.*;
import hwdb.Service;

static SEND_LIMIT = 700;

public class HWDBClient implements Runnable {
  private Service service;
  private String myServiceName = "handler";
  private SRPC srpc;
  private Connection conn;
  private LinkedBlockingQueue<String> buffer;
  private Thread t;

  public HWDBClient(String addr, int port, String serviceName) {
    try {
      srpc = new SRPC();
      myServiceName = serviceName;
      service = srpc.offer(myServiceName);
      conn = srpc.connect(addr, port, "HWDB");
      System.out.println("Connected to HWDB successfully");
      t = new Thread(this);
      t.start();
    } 
    catch (Exception e) {
      System.out.println("HWDB connection failed");
    }
    buffer = new LinkedBlockingQueue<String>();
  }

  public void insert(String table, String values) {
    String result = "";
    try {
      result = conn.call(String.format("SQL:insert into %s values %s", table, values));
    } 
    catch (Exception e) {
      System.out.println("SQL Insert failed");
    }
    if (!result.contains("Success")) {
      System.out.println(String.format("Insert failed for table %s with %s", table, values));
    }
  }

  public void insertLater(String line) {
    try {
      buffer.put(line); /* blocking put */
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
  }


  public void run() {
    int count;
    long start;
    String line;
    String result = "";
    StringBuilder query;
    System.out.println("Starting hwdb insert loop");
    while (true) { 
      query = new StringBuilder();
      try {
        query.append(buffer.take());
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
      count = 1;
      while (count < SEND_LIMIT && (line = buffer.poll()) != null) {
        query.append(line);
        count++;
      }
      try {
        result = conn.call(String.format("BULK: %d\n%s", count, query.toString()));
      } catch (Exception e) {
        System.out.println(String.format("Error: %s, %s", e.getMessage(), result));
      }
    }
  }
}