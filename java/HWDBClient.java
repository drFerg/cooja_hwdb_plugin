import java.io.IOException;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.util.logging.Level;
import hwdb.*;
import hwdb.Service;

public class HWDBClient {
  private String myServiceName = "handler";
  private SRPC srpc;
  private Connection conn;
  private int incomingPort = 0;

  public HWDBClient(String addr, int port, String serviceName){
    try {
      srpc = new SRPC();
      myServiceName = serviceName;
      service = srpc.offer(myServiceName);
      conn = srpc.connect(addr, port, "HWDB");
      incomingPort = srpc.details().getPort();
      System.out.println(conn.call(String.format("SQL:create table radio (coojaTime integer, id integer, event varchar(10))")));
    } 
    catch (Exception e) {
      System.out.println("HWDB connection failed");
    }
  }

  public void insert(String stmt){
    try {
      System.out.println(conn.call(String.format("SQL:%s", stmt)));
      System.out.println(conn.call(String.format("SQL:select * from radio")));
    } 
    catch (Exception e) {
      System.out.println("SQL Insert failed");
    }

    //System.out.println(conn.call(String.format("SQL:select * from radio 127.0.0.1 %d %s", incomingPort, myServiceName)));
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
    hwdb.insert("insert into radio values ('394832904', '10', 'radio')");
  }

}
