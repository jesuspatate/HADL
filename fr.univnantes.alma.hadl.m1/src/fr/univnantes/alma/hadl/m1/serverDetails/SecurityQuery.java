package fr.univnantes.alma.hadl.m1.serverDetails;

import fr.univnantes.alma.hadl.m2.connector.AtomicConnector;
import fr.univnantes.alma.hadl.m2.connector.Role;
import fr.univnantes.alma.hadl.m2.service.IncompatibleServiceException;
import fr.univnantes.alma.hadl.m2.service.NotConnectedServiceException;
import fr.univnantes.alma.hadl.m2.service.Service;


public class SecurityQuery extends AtomicConnector {
	private class SecurityManagerService extends Service {
		SecurityManagerService() {
			// TODO: signature à compléter
			super("securityManager", null, null);
		}
    }
	
    public SecurityQuery(String label) {
        super(label);
        Role requestor = new Role("requestor");
        Role securityManager = new Role("securityManager");
        SecurityManagerService required = new SecurityManagerService();
        SecurityManagerService provided = new SecurityManagerService();
        
        addRequiredService(securityManager, required);
        try {
			addProvidedService(requestor, provided, required);
		} catch (NotConnectedServiceException e) {
			e.printStackTrace();
		} catch (IncompatibleServiceException e) {
			e.printStackTrace();
		}
    }
}
