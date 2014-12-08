import java.io.IOException;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import hwdb.*;
import hwdb.Service;


public class HWDBClient implements Runnable {
  static int SEND_LIMIT = 600;
  private Service service;
  private String myServiceName = "handler";
  private SRPC srpc;
  private Connection conn;
  private LinkedBlockingQueue<String> insertBuffer;
  private Thread insertThread;

  public HWDBClient(String addr, int port, String serviceName) {
    try {
      srpc = new SRPC();
      myServiceName = serviceName;
      service = srpc.offer(myServiceName);
      conn = srpc.connect(addr, port, "HWDB");
      System.out.println("Connected to HWDB successfully");
      insertThread = new Thread(this);
      insertThread.start();
    } 
    catch (Exception e) {
      System.out.println("HWDB connection failed");
    }
    insertBuffer = new LinkedBlockingQueue<String>();
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

  /* Inserts a full table insert line into buffer, to later be processed by insert thread */
  public void insertLater(String line) {
    try {
      insertBuffer.put(line); /* blocking put, don't want to lose inserts */
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
  }

  /* Insert thread, generates BULK inserts into HWDB when entries are inserted into the insert buffer.
   * 
   * Waits for new inserts and sends all that are available in buffer up to SEND_LIMIT,
   * but does not wait till fill up to that limit. 
   */
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
        query.append(insertBuffer.take()); /* Wait for at least one insert */
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
      count = 1;
      /* take as many as we can that are available, but don't wait for more */
      while (count < SEND_LIMIT && (line = insertBuffer.poll()) != null) {
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