package hadl.m2.component;


/**
 * A service required by a component.
 */
public abstract class RequiredService extends Service {
    
    public RequiredService(final String label) {
        super(label);
    }
}
