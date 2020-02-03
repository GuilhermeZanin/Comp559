package comp559.particle;

/**
 * TODO: finish this class!
 */
public class ForwardEuler implements Integrator {
    
    @Override
    public String getName() {
        return "Forward Euler";
    }
    
    /** 
     * Advances the system at t by h 
     * @param p The state at time h
     * @param n The dimension of the state (i.e., p.length)
     * @param t The current time (in case the derivs function is time dependent)
     * @param h The step size
     * @param pout  The state of the system at time t+h
     * @param derivs The object which computes the derivative of the system state
     */
    @Override
    public void step(double[] p, int n, double t, double h, double[] pout, Function derivs) {
        // TODO: Objective 3, implement the forward Euler method
    	// Pout is going to be the result/output of x1 and xdot1 of forward euler
    	// p is the values of x0
    	
    	// dpdt into the pout  /   pout is the xdot1y and xdot1y
    	derivs.derivs(t, p, pout);
    	
    	// for every particle is going assign a value for the position and velocities
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+h*pout[i];
    	}
    	
        
    }

}
