package puzzle.client;

import puzzle.message.ConnectionRequest;
import puzzle.message.NodeInfo;
import puzzle.message.TileMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Client {
    private String name;
    private int port;
    private String address;
    //Need this to write to server
    private ObjectInputStream in;
    private ObjectOutputStream out;
    ServerConnectionHandler serverConnectionHandler;
    private MessagesQueue queue;
    private boolean clientIsConnected = false;


    public Client(String address, int port, String name, MessagesQueue queue) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.queue = queue;
    }

    public void start() {
        try {
            Socket toServer = new Socket(address, port);
            System.out.println("Client: connected! ");
            String address = toServer.getInetAddress().getHostAddress();
            int port = toServer.getLocalPort();
            //Send a connection request for start game
            in = new ObjectInputStream(toServer.getInputStream());
            out = new ObjectOutputStream(toServer.getOutputStream());
            sendConnectionRequest(address, port, name, false);

            serverConnectionHandler = new ServerConnectionHandler(toServer, queue);
            serverConnectionHandler.start();

            clientIsConnected = true;

        } catch (Exception ex) {
            clientIsConnected = false;
            ex.printStackTrace();

        }
    }

    public void sendConnectionRequest(String address, int port, String name, boolean isServer) {
        NodeInfo nodeInfo;
        if (isServer) {
            nodeInfo = new NodeInfo(address, port, name, true);
        } else {
            nodeInfo = new NodeInfo(address, port, name, false);
        }
        ConnectionRequest conn = new ConnectionRequest(nodeInfo, false);
        try {
            out.reset();
            out.writeObject(conn);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTileToServer(TileMessage message) {
        serverConnectionHandler.sendTile(message);
    }

    public ServerConnectionHandler getServerConnectionHandler() {
        return serverConnectionHandler;
    }

    public void join() throws InterruptedException {
        serverConnectionHandler.join();
    }

    public boolean isClientIsConnected() {
        return clientIsConnected;
    }
}
