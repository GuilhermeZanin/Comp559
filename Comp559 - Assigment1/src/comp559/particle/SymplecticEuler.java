package comp559.particle;

public class SymplecticEuler implements Integrator {

    @Override
    public String getName() {
        return "symplectic Euler";
    }

    @Override
    public void step(double[] p, int n, double t, double h, double[] pout, Function derivs) {
        // TODO: Objective 7, complete the symplectic Euler integration method.
    	// note you'll need to know how p is packed to properly implement this, so go
    	// look at ParticleSystem.getPhaseSpace()
    	
    	// dpdt into the pout  /   pout is the xdot1y and xdot1y
    	derivs.derivs(t, p, pout);
    	
    	
    	//similar to forward euler, increments by 4 every loop
    	// when loop the 4 equations
    	for(int i=0; i<n; i+=4) {

    		//this is for the first 2 equations 
    		// p1 p2 p3 p4 = positionX positionY velocityX velocityY
    		pout[i+2] = p[i+2]+h*pout[i+2]; // this one is to make p[2] for the velocity x
    		pout[i+3] = p[i+3]+h*pout[i+3]; // this one is to make p[2] for the velocity Y - which are used for the next one (the p1 and p2)
    		pout[i] = p[i]+h*pout[i+2]; // this one is for position X
    		pout[i+1] = p[i+1]+h*pout[i+3]; // this one is for position Y
    		
    	}

    }

}
