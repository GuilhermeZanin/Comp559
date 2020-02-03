package comp559.particle;

import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 * Spring class for 599 assignment 1
 * @author kry
 */
public class Spring {

    Particle p1 = null;
    Particle p2 = null;
    
    /** Spring stiffness, sometimes written k_s in equations */
    public static double k = 1;
    /** Spring damping (along spring direction), sometimes written k_d in equations */
    public static double c = 1;
    /** Rest length of this spring */
    double l0 = 0;
    
    /**
     * Creates a spring between two particles
     * @param p1
     * @param p2
     */
    public Spring( Particle p1, Particle p2 ) {
        this.p1 = p1;
        this.p2 = p2;
        recomputeRestLength();
        p1.springs.add(this);
        p2.springs.add(this);
    }
    
    /**
     * Computes and sets the rest length based on the original position of the two particles 
     */
    public void recomputeRestLength() {
        l0 = p1.p0.distance( p2.p0 );
    }
    
    /**
     * Applies the spring force by adding a force to each particle
     */
    public void apply() {
        // TODO: Objective 1, FINISH THIS CODE!
    	// starting the vecotr for force on particle A and deltaV (difference of the two velocities)
    	Vector2d fa = new Vector2d();
    	Vector2d deltaV = new Vector2d();
    	double l = this.p1.distance(this.p2.p.x, this.p2.p.y);

    	// deltaL - difference between the two particles
    	fa.sub(this.p1.p, this.p2.p);
    	fa.normalize();
    	
    	//calculating the spring force
    	deltaV.sub(this.p1.v,this.p2.v);
    	// Spring damping force
    	double tempFa =-(k*(l-l0) + c*(deltaV.dot(fa)));
    	// final fa
        fa.scale(tempFa);
        this.p1.addForce(fa);
        
        //final fb = negative of fa
        fa.scale(-1);
        this.p2.addForce(fa);
    }
   
    /** TODO: the functions below are for the backwards Euler solver */
    
    /**
     * Computes the force and adds it to the appropriate components of the force vector.
     * (This function is something you might use for a backward Euler integrator)
     * @param f
     */
    public void addForce( Vector f ) {
        // TODO: Objective 8, FINISH THIS CODE for backward Euler method (probably very simlar to what you did above)
    	
    	// starting the vecotr for force on particle A and deltaV (difference of the two velocities)
    	Vector2d fa = new Vector2d();
    	Vector2d deltaV = new Vector2d();
    	double l = this.p1.distance(this.p2.p.x, this.p2.p.y);

    	// deltaL - difference between the two particles
    	fa.sub(this.p1.p, this.p2.p);
    	fa.normalize();
    	
    	//calculating the spring force
    	deltaV.sub(this.p1.v,this.p2.v);
    	// Spring damping force // NO DAMPING, we adding the damp later with alfa0 and alfa 1, reayleigh damping
    	double tempFa =-(k*(l-l0));
    	// final fa
        fa.scale(tempFa);
        
        
        // forces to build b in backward euler
        f.add(p1.index*2, fa.x);
        f.add(p1.index*2+1, fa.y);
        
        f.add(p2.index*2, -fa.x);
        f.add(p2.index*2+1, -fa.y);
        
    }
    
    /**
     * Adds this springs contribution to the stiffness matrix
     * @param dfdx
     */
    public void addDfdx( Matrix dfdx ) {
        // TODO: Objective 8, FINISH THIS CODE... necessary for backward euler integration
        
    	// setting -K on the K matrix for particle 1  // setting for the I
    	dfdx.add(2*p1.index, 2*p1.index, -k);
    	dfdx.add(2*p2.index, 2*p2.index, -k);
    	//other diagonal of the same matrix and particle 2
    	dfdx.add(2*p1.index+1, 2*p1.index+1, -k);
    	dfdx.add(2*p2.index+1, 2*p2.index+1, -k);
    	
    	dfdx.add(2*p1.index, 2*p2.index, k);
    	dfdx.add(2*p2.index, 2*p1.index, k);
    	//other diagonal of the same matrix and particle 2 // setting for the J
    	dfdx.add(2*p2.index+1, 2*p1.index+1, k);
    	dfdx.add(2*p1.index+1, 2*p2.index+1, k);
    }   
 
    /**
     * Adds this springs damping contribution to the implicit damping matrix
     * @param dfdv
     */
    public void addDfdv( Matrix dfdv ) {
        // TODO: Objective 8, FINISH THIS CODE... necessary for backward Euler integration
    	
    	
        
    } 
    
}
