import java.io.IOException;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.util.logging.Level;
import hwdb.*;
import hwdb.Service;

public class HWDBClient {
private String radioTable = "create table radio (integer i, string s)";
    public static void main(String[] args)
    {
        try {
            String serviceName = "handler";
            SRPC srpc = new SRPC();
            service = srpc.offer(serviceName);
            Connection conn = srpc.connect("localhost", 1234,"HWDB");
            int port = srpc.details().getPort();
            System.out.println(conn.call(String.format("SQL:select * from a 127.0.0.1 %d %s", port, serviceName)));

            } catch (Exception e) {
                System.exit(1);
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


}
