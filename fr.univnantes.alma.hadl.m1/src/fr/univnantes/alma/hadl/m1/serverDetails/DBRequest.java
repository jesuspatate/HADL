package fr.univnantes.alma.hadl.m1.serverDetails;


public class DBRequest {
    
    private final String query;
    
    private final String login;
    
    private final String password;
    
    public DBRequest(final String query, final String login, final String pwd) {
        this.query = query;
        this.login = login;
        this.password = pwd;
    }

    public String getQuery() {
        return query;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
    
    @Override
    public String toString() {
    	return String.format("query=%s ; login=%s ; password=%s", this.query, this.login,
    			this.password);
    }
}
