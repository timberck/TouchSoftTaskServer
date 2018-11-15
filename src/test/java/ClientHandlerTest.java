import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ClientHandlerTest {
    private Server serv;
    private boolean isClosed;
    private ClientHandler handler;
    private ClientHandler handler1;
    private int requestPosition;
    private ByteArrayOutputStream response;
    private static final List<String> REQUESTS_ARRAY = new ArrayList<String>();

    static {
        REQUESTS_ARRAY.add("tic");
        REQUESTS_ARRAY.add("tac");
    }

    @org.junit.Before
    public void setUp() throws Exception {
        isClosed = false;
        response = new ByteArrayOutputStream();
        requestPosition = 0;
        serv = new Server();
        handler = new ClientHandler(new Socket(){

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(REQUESTS_ARRAY.get(requestPosition++).getBytes("UTF-8"));
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return response;
            }

            @Override
            public void close() throws IOException {
                isClosed = true;
            }
        }, serv);
        handler1 = new ClientHandler(new Socket(){

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(REQUESTS_ARRAY.get(requestPosition++).getBytes("UTF-8"));
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                //return response;
                return response;
            }

            @Override
            public void close() throws IOException {
                isClosed = true;
            }
        }, serv);
    }

    @org.junit.After
    public void tearDown() throws Exception {
        response.close();
    }

    @org.junit.Test
    public void register_client_command_handler() throws Exception {
        int num_waiting_clients = serv.waiting_clients_size();
        int num_assigned_clients = serv.assigened_clients_size();
        handler.register_client_command_handler();
        assertTrue(num_waiting_clients +1 == serv.waiting_clients_size()|| num_assigned_clients + 1 == serv.assigened_clients_size());
        num_waiting_clients = serv.waiting_clients_size();
        num_assigned_clients = serv.assigened_clients_size();
        handler.register_client_command_handler();
        assertFalse(num_waiting_clients +1 == serv.waiting_clients_size()|| num_assigned_clients + 1 == serv.assigened_clients_size());
    }

    @org.junit.Test
    public void returning_client_handler() throws Exception {
        int num_waiting_clients = serv.waiting_clients_size();
        int num_assigned_clients = serv.assigened_clients_size();
        handler.returning_client_handler("/leave");
        assertFalse(num_waiting_clients +1 == serv.waiting_clients_size()|| num_assigned_clients + 1 == serv.assigened_clients_size());
        handler.returning_client_handler("something");
        assertTrue(num_waiting_clients +1 == serv.waiting_clients_size()|| num_assigned_clients + 1 == serv.assigened_clients_size());
    }

    @org.junit.Test
    public void register_agent_command_handler() throws Exception {
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.register_agent_command_handler();
        assertTrue(num_waiting_agents +1 == serv.waiting_consultants_size()|| num_assigned_agents + 1 == serv.assigned_consultants_size());
        num_waiting_agents = serv.waiting_consultants_size();
        num_assigned_agents = serv.assigned_consultants_size();
        handler.register_client_command_handler();
        assertFalse(num_waiting_agents +1 == serv.waiting_consultants_size()|| num_assigned_agents + 1 == serv.assigned_consultants_size());

    }

    @org.junit.Test
    public void returning_agent_handler() throws Exception {
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.returning_agent_handler("/leave");
        assertFalse(num_waiting_agents +1 == serv.waiting_consultants_size()|| num_assigned_agents + 1 == serv.assigned_consultants_size());
        handler.returning_agent_handler("something");
        assertTrue(num_waiting_agents +1 == serv.waiting_consultants_size()|| num_assigned_agents + 1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void releasing_client() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler1.leave_client_handler();
        assertTrue(num_waiting_clients==serv.waiting_clients_size());
        assertTrue(num_waiting_agents +1== serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void releasing_agent() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.exit_assigned_agent();
        assertTrue(num_waiting_clients+1==serv.waiting_clients_size());
        assertTrue(num_waiting_agents == serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void exit_waiting_client() throws Exception {
        handler.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        handler.exit_waiting_client();
        assertTrue(num_waiting_clients-1 ==serv.waiting_clients_size());
    }

    @org.junit.Test
    public void exit_waiting_agent() throws Exception {
        handler.register_agent_command_handler();
        int num_waiting_agents = serv.waiting_consultants_size();
        handler.exit_waiting_agent();
        assertTrue(num_waiting_agents-1 ==serv.waiting_consultants_size());
    }
    @org.junit.Test
    public void role_confirmation() throws Exception {
        handler.set_role(1);
        serv.add_waiting_client(handler);
        handler1.set_role(2);
        serv.add_waiting_consultant(handler1);
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.role_confirmation();
        assertTrue(num_waiting_clients-1==serv.waiting_clients_size());
        assertTrue(num_waiting_agents -1== serv.waiting_consultants_size());
        assertTrue(num_assigned_clients+1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents+1 == serv.assigned_consultants_size());
    }
    @org.junit.Test
    public void sendMsg() throws Exception {
        handler.sendMsg("Some message");
        assertTrue(response.toString("UTF-8").substring(11, response.toString().length()).equals("Some message"));
    }
    /*@org.junit.Test
    public void leave_agent_handler() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.leave_agent_handler();
        assertTrue(num_waiting_clients+1==serv.waiting_clients_size());
        assertTrue(num_waiting_agents == serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void leave_client_handler() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler1.leave_client_handler();
        assertTrue(num_waiting_clients==serv.waiting_clients_size());
        assertTrue(num_waiting_agents +1== serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void exit_assigned_agent() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler.exit_assigned_agent();
        assertTrue(num_waiting_clients+1==serv.waiting_clients_size());
        assertTrue(num_waiting_agents == serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }

    @org.junit.Test
    public void exit_assigned_client() throws Exception {
        handler.register_agent_command_handler();
        handler1.register_client_command_handler();
        int num_waiting_clients = serv.waiting_clients_size();
        int num_waiting_agents = serv.waiting_consultants_size();
        int num_assigned_clients = serv.assigened_clients_size();
        int num_assigned_agents = serv.assigned_consultants_size();
        handler1.leave_client_handler();
        assertTrue(num_waiting_clients==serv.waiting_clients_size());
        assertTrue(num_waiting_agents +1== serv.waiting_consultants_size());
        assertTrue(num_assigned_clients-1 == serv.assigened_clients_size());
        assertTrue(num_assigned_agents-1 == serv.assigned_consultants_size());
    }*/
}