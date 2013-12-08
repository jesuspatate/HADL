package fr.univnantes.alma.hadl.m1.serverDetails;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.univnantes.alma.hadl.m1.DBRequest;
import fr.univnantes.alma.hadl.m1.DBResponse;
import fr.univnantes.alma.hadl.m2.Request;
import fr.univnantes.alma.hadl.m2.Response;
import fr.univnantes.alma.hadl.m2.component.AtomicComponent;
import fr.univnantes.alma.hadl.m2.component.Port;
import fr.univnantes.alma.hadl.m2.service.ProvidedService;
import fr.univnantes.alma.hadl.m2.service.Service;


public class Database extends AtomicComponent {
	
	private static final Map<String, Class<?>> SECURITY_MANAGEMENT_PARAMS =
            new HashMap<String, Class<?>>();
	
	private static final Map<String, Class<?>> HANDLE_QUERY_PARAMS =
            new HashMap<String, Class<?>>();
    
    static {
        SECURITY_MANAGEMENT_PARAMS.put("login", String.class);
        SECURITY_MANAGEMENT_PARAMS.put("password", String.class);
        HANDLE_QUERY_PARAMS.put("request", DBRequest.class);
    }
    
    private class SecurityManagement extends Service {
    	
        public SecurityManagement() {
            super("securityManagement", boolean.class, SECURITY_MANAGEMENT_PARAMS);
        }
    }
    
    private class HandleQuery extends ProvidedService {
        public HandleQuery() {
            super("handleQuery", DBResponse.class, HANDLE_QUERY_PARAMS);
        }

		@Override
		public Response excecute(Map<String, Object> parameters) {
			Response resp = null;
			Map<String, Object> authParameters = new HashMap<String, Object>();
			authParameters.put("login", parameters.get("login"));
			authParameters.put("password", parameters.get("password"));
			Request authRequest = new Request("securityManagement", authParameters);
			resp = send(authRequest);
			boolean authorized = (Boolean) resp.getValue();
			
			if(authorized){
				List<Object> values = new LinkedList<Object>();
				values.add("Sucess !!!");
				DBResponse dbResp = new DBResponse(values);
				resp = new Response(dbResp);
			}
			else{
				List<Object> values = new LinkedList<Object>();
				values.add("Not authorized to access to databse");
				DBResponse dbResp = new DBResponse(values, true);
				resp = new Response(dbResp);
			}
			
			return resp;
		}
    }
    
    public Database(String label){
        super(label);
        Port securityManagement = new Port("securityManagement");
        Port query = new Port("query");
        
        addRequiredConnection(securityManagement, new SecurityManagement());
        addProvidedConnection(query, new HandleQuery());
    }
}
