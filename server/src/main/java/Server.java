import com.google.gson.GsonBuilder;
import exceptions.ConnectionException;
import net.Method;
import net.Request;
import net.Response;
import other.typeAdapters.DateAdapter;
import other.typeAdapters.ZonedDateTimeAdapter;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static Server server;


    private ServerSocketChannel channel;
    private Selector selector;
    private final InetSocketAddress address = new InetSocketAddress(System.getenv("server_host"), Integer.parseInt(System.getenv("server_port")));
    private final Set<SocketChannel> session;
    private GsonBuilder builder;


    private Server(){
        session = new HashSet<>();
        this.builder = new GsonBuilder();
        builder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
   }

    public static Server getServer(){
       if (server == null){
           server = new Server();
       }
       return server;
    }


    public void start(){
       if(!bindChannel()) return;

       while (channel.isOpen()){
            try{
               Request request = null;
               selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                        continue;
                    }

                    else if (key.isReadable()) {
                        request = read(key);
                    }

                    if (request != null) {

                        try {
                            Response response = RequestHandler.handle(request);
                            write(response, key);
                            if (response.method == Method.EXIT) stop();
                        } catch (RuntimeException e) {
                            Launcher.log.error(e.getMessage());
                            key.cancel();
                        }
                    } else key.cancel();
                }
           }
            catch (IOException e) {
                Launcher.log.error("Server is stopping");
            }
       }
    }

    private void stop(){
        try{
            Launcher.log.info("Closing channel...");
            channel.close();
            Launcher.log.info("Server is stopped");
        }

        catch (IOException e) {
            Launcher.log.fatal("Error while stopping server");
        }
    }

    private Request read(SelectionKey key) {
       try {
           SocketChannel socketChannel = (SocketChannel) key.channel();
           ByteBuffer buffer = ByteBuffer.allocate(4096);

           int amount = socketChannel.read(buffer);

           if (amount == -1){
               session.remove(socketChannel);
               Launcher.log.info("Disconnected from " + socketChannel.getRemoteAddress().toString());
               socketChannel.close();
               key.cancel();
               return null;
           }

           byte[] data = new byte[amount];
           System.arraycopy(buffer.array(), 0, data, 0, amount);
           String json = new String(data, StandardCharsets.UTF_8);
           return Request.fromJson(json);
       } catch (IOException e) {
           Launcher.log.fatal("Error while reading request: " + e.getClass().getName() + " " + e.getMessage());
           throw new ConnectionException();
       }
    }

    private void accept(SelectionKey key) {
       try{
           SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();

           if (channel == null) {
               return;
           }

           channel.configureBlocking(false);
           channel.register(selector, SelectionKey.OP_READ);
           session.add(channel);
           Launcher.log.info("Connected to " + channel.getRemoteAddress().toString());
       } catch (IOException e) {
           Launcher.log.fatal("Selector error");
       }
    }


    private boolean bindChannel(){
       try{
           Launcher.log.info("Starting server on " + address.toString());
           selector = Selector.open();
           channel = ServerSocketChannel.open();
           channel.bind(address);
           channel.configureBlocking(false);
           channel.register(selector, SelectionKey.OP_ACCEPT);
           return true;
        } catch (ClosedChannelException e) {
           Launcher.log.fatal("Channel was interrupted");
           return false;
       } catch (BindException e ){
           Launcher.log.fatal("Address is already bound");
           return false;
       } catch (IOException e) {
           Launcher.log.fatal("Server error");
           return false;
       }
    }

    private void write(Response response, SelectionKey key){
        try{
            SocketChannel channel = (SocketChannel) key.channel();

            byte[] data = Response.toJson(response).getBytes(StandardCharsets.UTF_8);
            int size = data.length;
            ByteBuffer buffer = ByteBuffer.allocate(4100);
            int packagesNumber = size / 4092 + (size % 4092 == 0 ? 0 : 1);

            Launcher.log.info("Sending response...");
            Launcher.log.info("Packages number: " + packagesNumber);

            for (int i = 1; i < packagesNumber; i++){
                buffer.putInt(i);
                buffer.putInt(packagesNumber);
                buffer.put(Arrays.copyOfRange(data, (i - 1) * 4092, i * 4092));
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
            buffer.putInt(packagesNumber);
            buffer.putInt(packagesNumber);
            buffer.put(Arrays.copyOfRange(data, (packagesNumber - 1) * 4092, size));
            buffer.flip();
            channel.write(buffer);
            Launcher.log.info("Disconnecting from " + channel.getRemoteAddress());
            channel.close();
            key.cancel();
        } catch (IOException e) {
            Launcher.log.fatal("Error while sending response");
            key.cancel();
        }
    }
}
