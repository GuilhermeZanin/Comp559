package comp559.particle;

public class RK4 implements Integrator {
    
    @Override
    public String getName() {
        return "RK4";
    }

    @Override
    public void step(double[] p, int n, double t, double h, double[] pout, Function derivs) {
        // TODO: Objective 6, implement the RK4 integration method
    	// see also efficient memory management suggestion in provided code for the Midpoint method.
    	
    	//implement yet
    	
    	// dpdt into the pout  /   pout is the xdot1y and xdot1y
    	derivs.derivs(t, p, pout);
    	
    	// for every particle is going assign a value for the position and velocities
    	// k1 = f(x0)
    	// k2 = f(x0+(h/2)*k1)
    	// k3 = f(x0+(h/2)*k2)
    	// k4 = f(x0+h*k3)
    	
    	// k1
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i];
    	}
    	
    	derivs.derivs(t, pout, pout);
    	
    	// k2
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+(h/2)*pout[i];
    	}
    	
    	derivs.derivs(t, pout, pout);
    	
    	// k3
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+(h/2)*pout[i];
    	}
    	
    	derivs.derivs(t, pout, pout);
    	
    	// k4
    	for(int i=0; i<n; i++) {
    		pout[i] = p[i]+h*pout[i];
    	}
    	

    }
}
