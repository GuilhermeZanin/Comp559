package comp559.particle;

public class Midpoint implements Integrator {

    @Override
    public String getName() {
        return "midpoint";
    }

    private double[] tmp;
    
    @Override
    public void step(double[] p, int n, double t, double h, double[] pout, Function derivs) {
        // TODO: Objective 4, implement midpoint method

    	// You will probably want a temporary array in this method and perhaps
    	// multiple temporary arrays in other higher order explicit integrators.
    	// Avoid thrashing memory by reallocating only when necessary.
    	if ( tmp == null || tmp.length != n ) {
            tmp = new double[n];
    	}
    	
    	// dpdt into the pout  /   pout is the xdot1y and xdot1y
    	derivs.derivs(t, p, pout);
    	
    	// for every particle is going assign a value for the position and velocities
    	// doing half a step on pout 
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+(h/2)*pout[i];
    	}
    	// calling derivs again but with pout, midpoint method the equation with halfstep then a normal step
    	derivs.derivs(t, pout, pout);
    	
    	// doing full step now
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+h*pout[i];
    	}
    	
    }

}
