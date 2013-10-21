package hadl.m2.component;

import hadl.m2.Interface;


/**
 * A service of a component.
 * 
 * <p>
 * Superclass of provided and required service. A service represents a
 * functionality provided or required by a component. It can be dynamically
 * attached to a port in order to be provided to other components (or by another
 * one if it is required).
 * </p>
 * 
 * @see hadl.m2.component.Port
 * @see
 */
abstract class Service extends Interface {
    
    protected Service(final String label) {
        super(label);
    }
}
