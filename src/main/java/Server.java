import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    static final int PORT = 3443;
    //private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private ArrayList<ClientHandler> waiting_clients = new ArrayList<ClientHandler>();
    private ArrayList<ClientHandler> waiting_consultants = new ArrayList<ClientHandler>();
    private ArrayList<ClientHandler> assigned_clients = new ArrayList<ClientHandler>();
    private ArrayList<ClientHandler> assigned_consultants = new ArrayList<ClientHandler>();

    public void add_waiting_client(ClientHandler c){
        waiting_clients.add(c);
    }
    public void add_waiting_consultant(ClientHandler c){
        waiting_consultants.add(c);
    }
    public void add_assigned_consultant(ClientHandler c){
        assigned_consultants.add(c);
    }
    public void add_assigned_client(ClientHandler c){
        assigned_clients.add(c);
    }

    public int waiting_clients_size()
    {
        return waiting_clients.size();
    }
    public int waiting_consultants_size()
    {
        return waiting_consultants.size();
    }
    public int assigened_clients_size()
    {
        return assigned_clients.size();
    }
    public int assigned_consultants_size()
    {
        return assigned_clients.size();
    }

    public void remove_waiting_client(ClientHandler c)
    {
        waiting_clients.remove(c);
    }
    public void remove_waiting_consultant(ClientHandler c)
    {
        waiting_consultants.remove(c);
    }
    public void remove_assigned_client(ClientHandler c)
    {
        assigned_clients.remove(c);
    }
    public void remove_assigned_consultant(ClientHandler c)
    {
        assigned_consultants.remove(c);
    }

    public void remove_waiting_client(int c)
    {
        waiting_clients.remove(c);
    }
    public void remove_waiting_consultant(int c)
    {
        waiting_consultants.remove(c);
    }
    public void remove_assigned_client(int c)
    {
        assigned_clients.remove(c);
    }
    public void remove_assigned_consultant(int c)
    {
        assigned_consultants.remove(c);
    }

    public ArrayList<ClientHandler>  getWaiting_clients()
    {
        return waiting_clients;
    }
    public ArrayList<ClientHandler>  getWaiting_consultants()
    {
        return waiting_consultants;
    }
    public ArrayList<ClientHandler>  getAssigned_clients()
    {
        return assigned_clients;
    }
    public ArrayList<ClientHandler>  getAssigned_consultants()
    {
        return assigned_consultants;
    }

    public Server() {

    }
    public void getting_clients()
    {
        Logger rootLogger = LogManager.getRootLogger();
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            rootLogger.info("server is running");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                //clients.add(client);
                rootLogger.info("new someone has connected");
                new Thread(client).start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
                rootLogger.info("server shut down");
                serverSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*public void sendMessageToAllClients(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }

    }*/
   /*public void sendPersonalMessage(ClientHandler from, String message){
        for (ClientHandler o : clients) {
            if (o.get_Our_clientSocket().equals(from.get_Comunicate_to_clientSocket())) {

                o.sendMsg(message);
                from.sendMsg(message + "//sent");
                break;
            }
        }
    }*/

    /*public void removeClient(ClientHandler client) {
        clients.remove(client);
    }*/
}
