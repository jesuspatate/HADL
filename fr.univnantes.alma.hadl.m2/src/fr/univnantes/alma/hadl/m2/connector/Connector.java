package fr.univnantes.alma.hadl.m2.connector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.univnantes.alma.hadl.m2.ArchitecturalElement;
import fr.univnantes.alma.hadl.m2.Request;
import fr.univnantes.alma.hadl.m2.Response;
import fr.univnantes.alma.hadl.m2.configuration.Configuration;
import fr.univnantes.alma.hadl.m2.service.IncompatibleServiceException;
import fr.univnantes.alma.hadl.m2.service.NotConnectedServiceException;
import fr.univnantes.alma.hadl.m2.service.Service;

/**
 * A connector of a software architecture.
 * 
 * <p>
 * A connector models a specific communication mechanism enabling interaction
 * between components.
 * </p>
 */
public abstract class Connector extends ArchitecturalElement {
	private Map<String, Service> providedServices = new HashMap<String, Service>();
	private Map<String, Service> requiredServices = new HashMap<String, Service>();
	private Map<Role, Set<Service>> roleToProvided = new HashMap<Role, Set<Service>>();
	private Map<Service, Role> providedToRole = new HashMap<Service, Role>();
	private Map<Role, Set<Service>> roleToRequired = new HashMap<Role, Set<Service>>();
	private Map<Service, Role> requiredToRole = new HashMap<Service, Role>();
	private Map<Service, Service> requiredToProvided = new HashMap<Service, Service>();
	private Glue glue = new Glue();
	private Configuration configuration = null;

	public Connector(final String label) {
		super(label);
	}

	public void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	public Set<Role> getRoles() {
		return new HashSet<Role>(roleToProvided.keySet());
	}

	protected void addRole(Role role) {
		role.setConnector(this);
		roleToProvided.put(role, new HashSet<Service>());
		roleToRequired.put(role, new HashSet<Service>());
	}

	protected void addRequiredService(Role role, Service service,
			Service equivalent, GlueFunction function)
			throws NotConnectedServiceException, IncompatibleServiceException {
		if (!service.equals(equivalent)) {
			throw new IncompatibleServiceException(String.format(
					"%s incompatible with %s", service.getLabel(),
					equivalent.getLabel()));
		} else if (providedToRole.get(equivalent) == null) {
			throw new NotConnectedServiceException(String.format(
					"%s not added in %s", equivalent.getLabel(), label));
		}

		if (!roleToRequired.containsKey(role)) {
			addRole(role);
		}

		requiredServices.put(service.getLabel(), service);
		roleToRequired.get(role).add(service);
		requiredToRole.put(service, role);
		requiredToProvided.put(service, equivalent);
		glue.addTransformation(service, function);
	}
	
	protected void addRequiredService(Role role, Service service,
			Service equivalent) throws NotConnectedServiceException,
			IncompatibleServiceException {
		addRequiredService(role, service, equivalent, GlueFunction.identity);
	}

	protected void addProvidedService(Role role, Service service) {
		if (!roleToProvided.containsKey(role)) {
			addRole(role);
		}

		providedServices.put(service.getLabel(), service);
		roleToProvided.get(role).add(service);
		providedToRole.put(service, role);
	}

	protected Response receive(Request request) {
		Response resp = null;
		
		System.out.println(this.label + " reçoit " + request);
		
		if (configuration != null) {
			Service service = requiredServices.get(request.getService());
			Service equivalent = requiredToProvided.get(service);
			Role role = providedToRole.get(equivalent);
			Request transformedReq = glue.getTransformedRequest(service,
					request);
			resp = configuration.receive(role, transformedReq);
		} else {
			resp = new Response(null, false, String.format(
					"%s not connected to configuration", this.getLabel()));
		}
		
		System.out.println(this.label + " renvoie " + resp);

		return resp;
	}

	public Role getProvidingRole(final String service) {
		Service providedService = providedServices.get(service);
		return providedToRole.get(providedService);
	}

	public Role getRequestingRole(final String service) {
		Service requiredService = requiredServices.get(service);
		return requiredToRole.get(requiredService);
	}

	public Set<Service> getProvidedServices(final Role role) {
		return new HashSet<Service>(roleToProvided.get(role));
	}

	public Set<Service> getRequiredServices(final Role role) {
		return new HashSet<Service>(roleToRequired.get(role));
	}
}
