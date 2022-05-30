import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repositories.DataBase;


public class Launcher {

    public static Logger log = LogManager.getLogger(Launcher.class.getName());

    public static void main(String args[]){
        DataBase.create();
        Server server = Server.getServer();
        server.start();

    }
}
