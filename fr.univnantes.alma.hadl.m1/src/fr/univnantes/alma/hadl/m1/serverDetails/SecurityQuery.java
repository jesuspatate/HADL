package fr.univnantes.alma.hadl.m1.serverDetails;

import java.util.HashMap;
import java.util.Map;

import fr.univnantes.alma.hadl.m2.connector.AtomicConnector;
import fr.univnantes.alma.hadl.m2.connector.Role;
import fr.univnantes.alma.hadl.m2.service.IncompatibleServiceException;
import fr.univnantes.alma.hadl.m2.service.NotConnectedServiceException;
import fr.univnantes.alma.hadl.m2.service.Service;

public class SecurityQuery extends AtomicConnector {

	private static final Map<String, Class<?>> SECURITY_MANAGEMENT_PARAMS = new HashMap<String, Class<?>>();

	static {
		SECURITY_MANAGEMENT_PARAMS.put("login", String.class);
		SECURITY_MANAGEMENT_PARAMS.put("password", String.class);
	}

	private class SecurityManagement extends Service {

		SecurityManagement() {
			super("securityManagement", boolean.class,
					SECURITY_MANAGEMENT_PARAMS);
		}
	}

	public SecurityQuery(String label) {
		super(label);
		Role requestor = new Role("requestor");
		Role securityManager = new Role("securityManager");
		SecurityManagement required = new SecurityManagement();
		SecurityManagement provided = new SecurityManagement();
		addProvidedService(securityManager, provided);
		try {
			addRequiredService(requestor, required, provided);
		} catch (NotConnectedServiceException e) {
			e.printStackTrace();
		} catch (IncompatibleServiceException e) {
			e.printStackTrace();
		}
	}
}
