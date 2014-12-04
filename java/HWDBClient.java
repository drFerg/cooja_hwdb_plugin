import java.io.IOException;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.ArrayList;
import hwdb.*;
import hwdb.Service;

public class HWDBClient {
  private String myServiceName = "handler";
  private SRPC srpc;
  private Connection conn;

  public HWDBClient(String addr, int port, String serviceName) {
    try {
      srpc = new SRPC();
      myServiceName = serviceName;
      service = srpc.offer(myServiceName);
      conn = srpc.connect(addr, port, "HWDB");
      System.out.println("Connected to HWDB successfully");
    } 
    catch (Exception e) {
      System.out.println("HWDB connection failed");
    }
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

  public void bulk(final ArrayList<String> lines){
    Thread t = new Thread(new Runnable() {
      public void run() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("BULK: ").append(lines.size()).append("\n");
        for (String line: lines) {
          buffer.append(line);
        }
        String result = "";
        try {
          result = conn.call(buffer.toString());
        } 
        catch (Exception e) {
          System.out.println(e.getMessage() + result);

        }
      }
    });
    t.start();
  }

  static Service service;
  public void run() {
    try {
      Message query;
      while ((query = service.query()) != null) {
        System.out.println(query.getContent());
        query.getConnection().response("OK");
      }
    } catch (IOException e) {
      System.exit(1);
    }
  }
  public static void main(String[] args) {
    HWDBClient hwdb = new HWDBClient("localhost", 1234, "COOJA");
  }

}
