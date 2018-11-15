import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Server server;
    private ObjectOutputStream outMessage;
    private ObjectInputStream inMessage;
    private  int role = 0;
    private ClientHandler comunicate_to = null;
    private static final String HOST = "localhost";
    private static final int PORT = 3443;
    private Socket clientSocket = null;
    private static int clients_count = 0;
    private  int connection_number;
    static final Logger rootLogger = LogManager.getRootLogger();

    public ClientHandler(Socket socket, Server server) {
        try {
            clients_count++;
            this.connection_number = clients_count;
            this.server = server;
            this.role = 0;
            this.clientSocket = socket;
            this.outMessage = new ObjectOutputStream(socket.getOutputStream());
            this.inMessage = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            rootLogger.info("connection with socket i/o streams wasn't established");
        }
    }
    public void set_role(int r)
    {
        this.role = r;
    }
    public synchronized void role_confirmation()
    {
        /*System.out.println("number of waiting clients = "+  this.server.waiting_clients_size());
        System.out.println("number of waiting agents = "+  this.server.waiting_consultants_size());
        System.out.println("number of assigned clients = "+  this.server.assigened_clients_size());
        System.out.println("number of assigned agents = "+  this.server.assigned_consultants_size());*/
        while (this.server.waiting_clients_size() > 0 && this.server.waiting_consultants_size() > 0)
        {
            this.server.add_assigned_client(this.server.getWaiting_clients().get(0));
            this.server.remove_waiting_client(0);
            this.server.add_assigned_consultant(this.server.getWaiting_consultants().get(0));
            this.server.remove_waiting_consultant(0);
            rootLogger.info("assignment has been processed");
        }
        if(this.role ==1)
        {
            ArrayList<ClientHandler> a_clients = this.server.getAssigned_clients();
            for (int i =0; i < a_clients.size(); i++)
                if (a_clients.get(i).connection_number == this.connection_number) {
                    this.role = 3;
                    sendMsg("an agent has been assigned to you");
                    this.server.getAssigned_consultants().get(i).role_confirmation();
                    this.comunicate_to = this.server.getAssigned_consultants().get(i);
                }
        }
        if(this.role == 2)
        {
            ArrayList<ClientHandler> a_consultants = this.server.getAssigned_consultants();
            for (int i =0; i < a_consultants.size(); i++)
                if(a_consultants.get(i).connection_number == this.connection_number)
                {
                    this.role = 4;
                    sendMsg("a client has been assigned to you");
                    this.server.getAssigned_clients().get(i).role_confirmation();
                    this.comunicate_to = this.server.getAssigned_clients().get(i);
                }
        }
    }

    @Override
    public void run() {
        try {
            this.role = 0;
            while (true) {
                String clientMessage = (String)inMessage.readObject();
                if (clientMessage.length() >0  ) {
                    if(clientMessage.substring(0, Math.min(clientMessage.length(), 16)).equals("/register client") )
                    {
                        register_client_command_handler();
                    }
                    if(this.role == 5)
                    {
                        returning_client_handler(clientMessage);
                    }
                    if(clientMessage.substring(0, Math.min(clientMessage.length(), 15)).equals("/register agent") )
                    {
                        register_agent_command_handler();
                    }
                    if (this.role == 6)
                    {
                        returning_agent_handler(clientMessage);
                    }
                    if(!clientMessage.substring(0, Math.min(clientMessage.length(), 16)).equals("/register client")&& !clientMessage.substring(0, Math.min(clientMessage.length(), 15)).equals("/register agent") &&(role == 1 || role ==2) )
                        sendMsg("please, wait, assignment will be processed soon");
                    if(role == 3 && !clientMessage.substring(0, Math.min(clientMessage.length(), 16)).equals("/register client")&& !clientMessage.substring(0, Math.min(clientMessage.length(), 15)).equals("/register agent"))
                    {
                        this.comunicate_to.sendMsg("Client: " + clientMessage);
                        rootLogger.info("Client: " + clientMessage);
                    }
                    if(role == 4 && !clientMessage.substring(0, Math.min(clientMessage.length(), 16)).equals("/register client")&& !clientMessage.substring(0, Math.min(clientMessage.length(), 15)).equals("/register agent"))
                    {
                        this.comunicate_to.sendMsg("Agent: " + clientMessage);
                        rootLogger.info("Agent: " + clientMessage);
                    }
                    if (clientMessage.equals("/leave")){
                        leave_command_handler();
                    }
                    if (clientMessage.equals("/exit")) {
                        exit_command_handler();
                        break;
                    }
                }
                //Thread.sleep(100);
            }
        }
        /*catch (InterruptedException ex) {
            ex.printStackTrace();

        } */catch (IOException e) {
            exit_command_handler();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }
    public void register_client_command_handler()
    {
        if(this.role == 0)
        {
            this.role =1;
            this.server.add_waiting_client(this);
            sendMsg("please, wait, we will assign a consultant to you soon");
            rootLogger.info("new client has registered");
            role_confirmation();
        }
        else
            sendMsg("You are already registered in the system");
    }
    public void returning_client_handler(String clientMessage)
    {
        if(!clientMessage.equals("/leave") && !clientMessage.equals("/exit") && !clientMessage.substring(0, Math.min(clientMessage.length(), 9)).equals("/register")) {
            this.role = 1;
            this.server.add_waiting_client(this);
            sendMsg("please, wait, we will assign a consultant to you soon");
            rootLogger.info("client has returned");
            role_confirmation();
        }
        if(clientMessage.equals("/leave"))
            sendMsg("You have already left your conversation");

    }
    public void register_agent_command_handler()
    {
        if(this.role == 0) {
            this.role = 2;
            this.server.add_waiting_consultant(this);
            sendMsg("please, wait, we will assign a client to you soon");
            rootLogger.info("new agent has registered");
            role_confirmation();
        }
        else
            sendMsg("You are already registered in the system");
    }
    public void returning_agent_handler(String clientMessage)
    {
        if(!clientMessage.equals("/leave") && !clientMessage.equals("/exit") && !clientMessage.substring(0, Math.min(clientMessage.length(), 9)).equals("/register")) {
            this.role = 2;
            this.server.add_waiting_consultant(this);
            sendMsg("please, wait, we will assign a client to you soon");
            rootLogger.info("agent has returned");
            role_confirmation();
        }
        if(clientMessage.equals("/leave"))
            sendMsg("You have already left your conversation");
    }
    public void leave_command_handler()
    {
        if(this.role == 4)
            leave_agent_handler();
        if(this.role == 3)
            leave_client_handler();
    }
    public void leave_agent_handler()
    {
        rootLogger.info("Agent has left conversation");
        this.sendMsg("you have left this conversation, if you wish to get to waitlist please print something");
        this.comunicate_to.sendMsg("Agent has left this conversation , you have been added to wait list");
        releasing_agent();
    }
    public void leave_client_handler()
    {
        rootLogger.info("Client has left conversation");
        this.sendMsg("you have left this conversation, if you wish to get to waitlist please print something");
        this.comunicate_to.sendMsg("Client has left the program, you have been added to wait list");
        releasing_client();
    }
    public void exit_command_handler()
    {
        String exit_log = "Someone";
        if(this.role == 4)
            exit_log = exit_assigned_agent();
        if(this.role == 3)
            exit_log = exit_assigned_client();
        if(this.role == 1)
            exit_log = exit_waiting_client();
        if(this.role == 2 )
            exit_log = exit_waiting_agent();
        rootLogger.info( exit_log + " has left the system");
    }
    public String exit_assigned_agent()
    {
        this.comunicate_to.sendMsg("Agent has left the program, you have been added to wait list");
        releasing_agent();
        return("Assigned agent");
    }
    public void releasing_agent()
    {
        this.server.add_waiting_client(this.comunicate_to);
        this.server.getWaiting_clients().get(this.server.getWaiting_clients().size()-1).role = 1;
        int num = 0;
        for(int i = 0; i < this.server.getAssigned_consultants().size(); i++ )
            if(this.connection_number == this.server.getAssigned_consultants().get(i).connection_number)
                num  = i;
        this.server.remove_assigned_consultant(num);
        this.server.remove_assigned_client(num);
        this.role  = 6; this.comunicate_to = null;
        this.server.getWaiting_clients().get(this.server.getWaiting_clients().size()-1).role_confirmation();
        role_confirmation();
    }
    public String exit_assigned_client()
    {
        this.comunicate_to.sendMsg("Client has left the program, you have been added to wait list");
        releasing_client();
        return ("Assigned client");
    }
    public void releasing_client()
    {
        this.server.add_waiting_consultant(this.comunicate_to);
        this.server.getWaiting_consultants().get(this.server.getWaiting_consultants().size()-1).role = 2;
        int num = 0;
        for(int i = 0; i < this.server.getAssigned_clients().size(); i++ )
            if(this.connection_number == this.server.getAssigned_clients().get(i).connection_number)
                num  = i;
        this.server.remove_assigned_consultant(num);
        this.server.remove_assigned_client(num);
        this.role  = 5; this.comunicate_to = null;
        this.server.getWaiting_consultants().get(this.server.getWaiting_consultants().size()-1).role_confirmation();
        role_confirmation();
    }
    public String exit_waiting_client()
    {
        int num = 0;
        for(int i =0 ; i < this.server.getWaiting_clients().size(); i++)
            if(this.connection_number == this.server.getWaiting_clients().get(i).connection_number)
                num = i;
        this.server.remove_waiting_client(num);
        role_confirmation();
        return("Waiting client");
    }
    public String exit_waiting_agent()
    {
        int num = 0;
        for(int i = 0; i < this.server.getWaiting_consultants().size(); i++)
            if(this.connection_number == this.server.getWaiting_consultants().get(i).connection_number)
                num = i;
        this.server.remove_waiting_consultant(num);
        role_confirmation();
        return("Waiting agent");
    }
    public void sendMsg(String msg) {
        try {
            outMessage.writeObject(msg);
            outMessage.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void close() {
        //server.removeClient(this);
    }
}
