package hadl.m1.serverDetails;

import hadl.m1.CSMessage;
import hadl.m1.RPCCall;
import hadl.m2.Call;
import hadl.m2.Message;
import hadl.m2.component.AtomicComponent;
import hadl.m2.component.NoSuchPortException;
import hadl.m2.component.NoSuchServiceException;
import hadl.m2.component.Port;
import hadl.m2.component.ProvidedService;
import hadl.m2.component.RequiredService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Database extends AtomicComponent {
    
    class SecurityManagementService extends ProvidedService {
        
        public SecurityManagementService() {
            super("securityManagement");
        }
        
        @Override
        public void receive(final Message msg) {
            boolean wrongParam = false;
            
            if (msg.getHeader().equals("CALL")) {
                Call call = (Call) msg;
                
                if (call.getCalledService().equals(this.label)) {
                    // TODO Vérifier les paramètres
                    
                    if (wrongParam) {
                        // TODO Gérer appel incorrect
                    }
                    
                    System.out.println("La base de données reçoit : "
                            + call); // DBG
                    
                    CSMessage queryMsg = (CSMessage)
                            call.getParameters().get(0);
                    
                    String msgType = (String) queryMsg.getElement(
                            CSMessage.Part.HEADER, "type");
                    
                    if (msgType.equals("CREDQUERY")) {
                        boolean ok = Database.this.check(queryMsg);
                        
                        Database.this.sendAuthentication(queryMsg, ok);
                    }
                }
                else {
                    
                }
            }
        }
    }
    
    class HandleQuery extends ProvidedService {
        
        public HandleQuery() {
            super("handleQuery");
        }
        
        @Override
        public void receive(final Message msg) {
            boolean wrongParam = false;
            
            if (msg.getHeader().equals("CALL")) {
                Call call = (Call) msg;
                
                if (call.getCalledService().equals(this.label)) {
                    // TODO Vérifier les paramètres
                    
                    if (wrongParam) {
                        // TODO Gérer appel incorrect
                    }
                    
                    System.out.println("La base de données reçoit : "
                            + call); // DBG
                    
                    CSMessage queryMsg = (CSMessage)
                            call.getParameters().get(0);
                    
                    String msgType = (String) queryMsg.getElement(
                            CSMessage.Part.HEADER, "type");
                    
                    if (msgType.equals("QUERY")) {
                    	Database.this.sendQueryResponse(queryMsg, "meuh");
                    }
                }
                else {
                    // TODO Gérer appel incorrect
                }
            }
        }
    }
    
    class ReceiveCredAuth extends RequiredService {

        public ReceiveCredAuth() {
            super("receiveCredentialsAuthentication");
        }

        @Override
        public void receive(Message msg) {}
    }
    
    class ReceiveDBResponse extends RequiredService {

        public ReceiveDBResponse() {
            super("receiveDBResponse");
        }

        @Override
        public void receive(Message msg) {}
    }
    
    class SecurityManagementPort extends Port {
        
        public SecurityManagementPort(final Database component) {
            super("securityManagement");
        }
        
        @Override
        public void receive(final Message msg) {
            Call call = (Call) msg;
            String service = call.getCalledService();
            
            if (this.linked(service)) {
                this.getLink(service).send(this, msg);
            }
        }
    }
    
    class QueryInterface extends Port {
        
        public QueryInterface(final Database component) {
            super("queryInterface");
        }
        
        @Override
        public void receive(final Message msg) {
            Call call = (Call) msg;
            String service = call.getCalledService();
            
            if (this.linked(service)) {
                this.getLink(service).send(this, msg);
            }
        }
    }
    
    private Map<String, String> users = new HashMap<String, String>();
    
    public Database(String label) throws NoSuchServiceException,
            NoSuchPortException {
        
        super(label);

        RequiredService recCredAuth = new ReceiveCredAuth();
        RequiredService recDBResponse = new ReceiveDBResponse();
        ProvidedService secuMgmt = new SecurityManagementService();
        ProvidedService handleQuery = new HandleQuery();
        Port securityPort = new SecurityManagementPort(this);
        Port queryPort = new QueryInterface(this);
        
        this.addProvidedService(secuMgmt);
        this.addPort(securityPort);
        this.addConnection(secuMgmt.getLabel(), secuMgmt, securityPort);
        
        this.addProvidedService(handleQuery);
        this.addPort(queryPort);
        this.addConnection(handleQuery.getLabel(), handleQuery, queryPort);
        
        this.addRequiredService(recCredAuth);
        this.addConnection(recCredAuth.getLabel(), recCredAuth, securityPort);
        
        this.addRequiredService(recDBResponse);
        this.addConnection(recDBResponse.getLabel(), recDBResponse, queryPort);
    }

    public boolean addUser(final String login, final String pwd) {
        boolean res = false;
        
        String output = this.users.get(login);
        
        if (output == null) {
            this.users.put(login, pwd);
            res = true;
            
            System.out.println("Utilisateur \"" + login + "\" ajouté."); // DBG
        }
        
        return res;
    }
    
    public boolean deleteUser(final String login, final String pwd) {
        boolean res = false;
        
        final String suggestedPwd = this.users.get(login);
        
        if (suggestedPwd != null && pwd.contentEquals(suggestedPwd)) {
            this.users.remove(login);
            res = true;
            
            System.out.println("Utilisateur supprimé."); // DBG
        }
        
        return res;
    }
    
    private boolean check(final CSMessage msg) {
        boolean res = false;
        String login = (String) msg.getElement(CSMessage.Part.BODY, "login");
        String suggestedPwd = (String) msg.getElement(CSMessage.Part.BODY,
                "password");
        
        String pwd = this.users.get(login);
        
        if (pwd != null) { // Existing user
            res = suggestedPwd.contentEquals(pwd);
        }
        
        System.out.println("Utilisateur trouvé dans la bd : " + res); // DBG
        
        return res;
    }
    
    private void sendAuthentication(CSMessage msg, boolean ok) {
        String id = (String) msg.getElement(CSMessage.Part.HEADER, "id");
        
        CSMessage response = new CSMessage(id);
        
        response.addHeaderElement("type", "CREDRESP");
        response.addBodyElement("auth", ok);
        
        List<Object> args = new ArrayList<Object>();
        args.add(response);
        
        Call call = new RPCCall("receiveCredentialsAuthentication", args);
        
        System.out.println("La base de données envoie : "
                + call); // DBG
        
        try {
            Port port = getRequestingPort("receiveCredentialsAuthentication");
            port.receive(call);
        }
        catch (NoSuchServiceException e) {
            // Couldn't be reachable
        }
    }
    
    private void sendQueryResponse(CSMessage queryMsg, Object response) {
        String id = (String) queryMsg.getElement(
        		CSMessage.Part.HEADER, "id");
    	
    	CSMessage responseMsg = new CSMessage(id);
    	responseMsg.addHeaderElement("type", "QUERYRESP");
    	responseMsg.addBodyElement("response", response);
        
        List<Object> args = new ArrayList<Object>();
        args.add(responseMsg);
    	
    	Call call = new RPCCall("receiveDBResponse", args);
    	
    	System.out.println("La base de données envoie : " + call); // DBG
        
        try {
            Port port = getRequestingPort("receiveDBResponse");
            port.receive(call);
        }
        catch (NoSuchServiceException e) {
            // Couldn't be reachable
        }
    }
}
