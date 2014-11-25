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

  public HWDBClient(String addr, int port, String serviceName) {
    try {
      srpc = new SRPC();
      myServiceName = serviceName;
      service = srpc.offer(myServiceName);
      conn = srpc.connect(addr, port, "HWDB");
      System.out.println(conn.call("SQL:create table radio (time integer, id integer, event varchar(40), isRadioOn integer, signal real, power real)"));
      System.out.println(conn.call("SQL:create table cpu (time integer, id integer, state integer, stateStr varchar(10))"));
      System.out.println(conn.call("SQL:create persistenttable mote (id integer primary key, duty real, duration integer, radioOn integer, lastTime integer, lastStatus varchar(40), wasRadioOn integer)"));
      System.out.println(conn.call("SQL:create persistenttable cpuState (id integer primary key, duty real, duration integer, radioOn integer, lastTime integer, state integer)"));

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
    hwdb.insert("radio", "('394832904', '10', \"radio\")");
  }

}
