package comp559.particle;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.SceneGraphNode;

/**
 * Implementation of a simple particle system
 * @author kry
 */
public class ParticleSystem implements SceneGraphNode, Function, Filter {
    
    private List<Particle> particles = new LinkedList<Particle>();
    
    private List<Spring> springs = new LinkedList<Spring>();
    
    /**
     * Creates an empty particle system
     */
    public ParticleSystem() {
        // do nothing
    }

    /**
     * Creates one of a number of simple test systems.
     * @param which
     */
    public void createSystem( int which ) {
        
        if ( which == 1) {        
            Point2d p = new Point2d( 100, 100 );
            Vector2d d = new Vector2d( 20, 0 );            
            Particle p1, p2, p3, p4;
            p1 = new Particle( p.x - d.y, p.y + d.x, 0, 0 );
            p1.index = particles.size();
            particles.add( p1 );
            p2 = new Particle( p.x + d.y, p.y - d.x, 0, 0 );
            p2.index = particles.size();
            particles.add( p2 );
            springs.add( new Spring ( p1, p2 ) );           
            p1.pinned = true;
            p2.pinned = true;            
            p.add( d );
            p.add( d );                    
            int N = 10;
            for (int i = 1; i < N; i++ ) {                
                //d.set( 20*Math.cos(i*Math.PI/N), 20*Math.sin(i*Math.PI/N) );                
                p3 = new Particle( p.x - d.y, p.y + d.x, 0, 0 );
                p3.index = particles.size();
                particles.add( p3 );
                p4 = new Particle( p.x + d.y, p.y - d.x, 0, 0 );
                p4.index = particles.size();
                particles.add( p4 );
                springs.add( new Spring ( p3, p1 ) );
                springs.add( new Spring ( p3, p2 ) );
                springs.add( new Spring ( p4, p1 ) );
                springs.add( new Spring ( p4, p2 ) );
                springs.add( new Spring ( p4, p3 ) );
                p1 = p3;
                p2 = p4;                
                p.add( d );
                p.add( d );            
            }
        } else if ( which == 2) {
            Particle p1 = new Particle( 320, 100, 0, 0 );
            p1.index = particles.size();
            particles.add( p1 );
            Particle p2 = new Particle( 320, 200, 0, 0 );
            p2.index = particles.size();
            particles.add( p2 );
            p1.pinned = true;
            springs.add( new Spring( p1, p2 ) );
        } else if ( which == 3 ) {
            int ypos = 100;
            Particle p0 = null;
            Particle p1, p2;
            p1 = new Particle( 320, ypos, 0, 0 );
            p1.index = particles.size();
            p1.pinned = true;            
            particles.add( p1 );
            int N = 10;
            for ( int i = 0; i < N; i++ ) {
                ypos += 20;
                p2 = new Particle( 320, ypos, 0, 0 );
                p2.index = particles.size();
                particles.add( p2 );
                springs.add( new Spring( p1, p2 ) );                
                // Hum.. this is not great in comparison to a proper bending energy...
                // use Maple to generate some code though, as it is painful to write by hand! :(
                if ( p0 != null ) springs.add( new Spring( p2, p0 ) );
                p0 = p1;
                
                p1 = p2;
            }
        }
    }
    
    /**
     * Gets the particles in the system
     * @return the particle set
     */
    public List<Particle> getParticles() {
        return particles;
    }
    
    /**
     * Gets the springs in the system
     * @return the spring list
     */
    public List<Spring> getSprings() {
    	return springs;
    }
    
    /**
     * Resets the positions of all particles
     */
    public void resetParticles() {
        for ( Particle p : particles ) {
            p.reset();
        }
        time = 0;
    }
    
    /**
     * Deletes all particles
     */
    public void clearParticles() {
        particles.clear();
        springs.clear();
    }
    
    /**
     * Gets the phase space state of the particle system
     * @param phaseSpaceState
     */
    public void getPhaseSpace( double[] phaseSpaceState ) {
        int count = 0;
        for ( Particle p : particles ) {
            phaseSpaceState[count++] = p.p.x;
            phaseSpaceState[count++] = p.p.y;
            phaseSpaceState[count++] = p.v.x;
            phaseSpaceState[count++] = p.v.y;
        }
    }
    
    /**
     * Gets the dimension of the phase space state
     * (particles * 2 dimensions * 2 for velocity and position)
     * @return dimension
     */
    public int getPhaseSpaceDim() {        
        return particles.size() * 4;
    }
    
    /**
     * Sets the phase space state of the particle system
     * @param phaseSpaceState
     */
    public void setPhaseSpace( double[] phaseSpaceState ) {
        int count = 0;
        for ( Particle p : particles ) {
            if ( p.pinned ) {
                count += 4;
            } else {
                p.p.x = phaseSpaceState[count++];
                p.p.y = phaseSpaceState[count++];
                p.v.x = phaseSpaceState[count++];
                p.v.y = phaseSpaceState[count++];
            }
        }
    }
    
    /**
     * Fixes positions and velocities after a step to deal with collisions 
     */
    public void postStepFix() {
        for ( Particle p : particles ) {
            if ( p.pinned ) {
                p.v.set(0,0);
            }
        }
        // do wall collisions
        double r = restitution.getValue();
        for ( Particle p : particles ) {            
            if ( p.p.x <= 0 ) {
                p.p.x = 0;
                if ( p.v.x < 0 ) p.v.x = - p.v.x * r;
                if ( p.f.x < 0 ) p.f.x = 0;                
            }
            if ( p.p.x >= width ) {
                p.p.x = width;
                if (p.v.x > 0 ) p.v.x = - p.v.x * r;
                if (p.f.x > 0 ) p.f.x = 0;
            } 
            
            if ( p.p.y >= height ) {
                p.p.y = height;
                if ( p.v.y > 0 ) p.v.y = - p.v.y * r;
                if ( p.f.y > 0 ) p.f.y = 0;
            } 
            if ( p.p.y <= 0 ) {
                p.p.y = 0;
                if ( p.v.y < 0 ) p.v.y = - p.v.y * r;
                if ( p.f.y < 0 ) p.f.y = 0;
            }
        }
    }
    
    /** Elapsed simulation time */
    public double time = 0;

    /** The explicit integrator to use, if not performing backward Euler implicit integration */
    public Integrator integrator;
    
    public double[] state = new double[1];
    public double[] stateOut = new double[1];

    // these get created in init() and are probably useful for Backward Euler computations
    private ConjugateGradientMTJ CG;
    private DenseMatrix A;
    private DenseMatrix dfdx;
    private DenseMatrix dfdv;
    private DenseVector deltaxdot;
    private DenseVector b;
    private DenseVector f;
    private DenseVector xdot;
    
    /**
     * Initializes the system 
     * Allocates the arrays and vectors necessary for the solve of the full system
     */
    public void init() {
        int N = particles.size();
        // create matrix and vectors for solve
        CG = new ConjugateGradientMTJ(2*N);
        CG.setFilter(this);
        A = new DenseMatrix(2*N, 2*N);
        dfdx = new DenseMatrix(2*N, 2*N);
        dfdv = new DenseMatrix(2*N, 2*N);
        deltaxdot = new DenseVector(2*N);
        b = new DenseVector(2*N);
        f = new DenseVector(2*N);
        xdot = new DenseVector(2*N);
    }
    
    /**
     * Fills in the provided vector with the particle velocities.
     * @param xd
     */
    private void getVelocities(DenseVector xd) {
        for ( Particle p : particles ) {
            int j = p.index * 2;
            if( p.pinned ) {
                xd.set( j, 0 );
                xd.set( j+1, 0 );
            } else {
                xd.set( j, p.v.x );
                xd.set( j+1, p.v.y );
            }
        }       
    }

    /**
     * Sets the velocities of the particles given a vector
     * @param xd
     */
    private void setVelocities(DenseVector xd) {
        for ( Particle p : particles ) {
            int j = p.index * 2;
            if( p.pinned ) {
                p.v.set(0,0);
            } else {
                p.v.x = xd.get(j);
                p.v.y = xd.get(j+1);
            }
        }
    }
    
    /**
     *  Evaluates derivatives for ODE integration.
     * @param t time 
     * @param p phase space state
     * @param dydt to be filled with the derivative
     */
    @Override
    public void derivs(double t, double[] p, double[] dpdt) {
        // set particle positions to given values
        setPhaseSpace( p );
        
        // TODO: Objective 2, for explicit integrators, compute forces, and accelerations, and set dpdt
        //forces: gravtiational, spring forces and viscous damping forces
        
        // p is the vector of position and velocity
        // t is time
        //dpdt = [xdot, ydot, x2dot, y2dot]
        
    	// force of gravity
        Vector2d fg = new Vector2d();
        
        // force of damping
        Vector2d fDamp = new Vector2d();
        
        //Accelerations
        //dpdt = [x2dot, y2dot, xdot, ydot]

        // LOOP FOR ADDING FORCES ON PARTICLES
        for(Particle tempParticle : particles) {
        	// for everytime before applying forces, we have to clear the forces on the particles
        	tempParticle.clearForce();
            // if the gravity is used true, then it adds the force of gravity
        	if(useGravity.getValue()){
        		// adds the force of fg into only the Y axis
            	// for each particle on the list adds the gravitational force
            	fg.x=0;
            	fg.y = tempParticle.mass*gravity.getValue();
            	tempParticle.addForce(fg);
        	}
        	
        	//DAMPING
        	//adding the viscous damping
        	// separated in for X and Y because of value double
        	fDamp.x = -viscousDamping.getValue()*tempParticle.v.x;
        	fDamp.y = -viscousDamping.getValue()*tempParticle.v.y;
        	
        	// added the force of damping
        	tempParticle.addForce(fDamp);
        	
        }
        
        //SPRING FORCES
        // for each spring  on the list adds springs forces to each particle which contain the (damping and stiffness)
    	// Added spring force (damping and stiffness) (F= B*xdot+K*x)
    	for(Spring tempSprings : springs) {
    		tempSprings.apply();
    	}
    	
    	
    	// ACCELERATIONS -> dpdt = [xdot, ydot, x2dot, y2dot]
    	// velocity x and y, acceleration x and y
    	// dpdt is a vector of 4 values for each particle, so i++ including for each
    	// it continues increassing for all particles
    	int i=0;
    	// After the spring forces added
		// dpdt = [xdot, ydot, x2dot, y2dot]
    	for(Particle tempParticle : particles) {
    		//velocity = position + velocity*acceleration
        	dpdt[i++] = tempParticle.v.x;
        	dpdt[i++] = tempParticle.v.y;
        	//acceleration = force/mass
        	dpdt[i++] = tempParticle.f.x/tempParticle.mass;
        	dpdt[i++] = tempParticle.f.y/tempParticle.mass;
    	}
    	
        // list of 4 elements of each particle velocity, acceleration, dp/dt= [x., y., x.., y..]
        // vecotrs p=(x over x.) = explicit over each one(x y x. y.), calculating dp/dt for each particle in the whole system
        // x.. = Fx/m
        // y.. = Fy/M
        
        
        
        
    }
    
    /** Time in seconds that was necessary to advance the system */
    public double computeTime;
    
    /**
     * Advances the state of the system
     * @param elapsed
     */
    public void advanceTime( double elapsed ) {
        Spring.k = springStiffness.getValue();
        Spring.c = springDamping.getValue();
            
        int n = getPhaseSpaceDim();
        
        long now = System.nanoTime();        
        
        if ( explicit.getValue() ) {
            if ( n != state.length ) {
                state = new double[n];
                stateOut = new double[n];
            }
            // TODO: See explicit stepping here
            getPhaseSpace(state);         
            integrator.step( state, n, time, elapsed, stateOut, this);                
            setPhaseSpace(stateOut);
        } else {        
            if ( f == null || f.size() != n ) {
                init();
            }
            
            // TODO: Objective 8, your backward Euler implementation will go here!
            // Note that the init() method called above creates a bunch of very 
            // useful MTJ working variables for you, and the ConjugateGradientMTJ object.
            // Go look at that code now!
            
            
            dfdx.zero(); // clears the dfdx for the springs
            f.zero(); // clear the forces 
            
            // going to build A and b  /    A*deltaXdot = b
            // for each particle, we are going to do a loop to multiple byt the M matrix
            for(Particle tempParticle : particles) {
            	// setting A, H in this case is called elapse
            	// K is dfdx
            	// *2 the index because each fo the vector and matrixes have components in X and Y
            	A.set(tempParticle.index*2, tempParticle.index*2, (1-elapsed*viscousDamping.getValue())*tempParticle.mass);
            	A.set(tempParticle.index*2+1, tempParticle.index*2+1, (1-elapsed*viscousDamping.getValue())*tempParticle.mass); // set the index for Y
            	xdot.set(tempParticle.index*2, tempParticle.v.x); // velocity of xdot in X, built for b
            	xdot.set(tempParticle.index*2+1, tempParticle.v.y); // velocity of xdot in Y, built for b
            	
            	// set b first part (right part of the equation)
            	b.set(tempParticle.index*2, elapsed*viscousDamping.getValue()*tempParticle.mass*tempParticle.v.x); // index of the particle, second part of b (h*alfa0*M*X0dot) alfa0 is the viscous damping
            	b.set(tempParticle.index*2+1, elapsed*viscousDamping.getValue()*tempParticle.mass*tempParticle.v.y);
            	
            	if(useGravity.getValue()) {
            		f.add(tempParticle.index*2+1, tempParticle.mass*gravity.getValue()); // adding the force of gravity, the index*2+1 adds for the Y axis
            	}
            	
            }
           
            // for every spring call the add.dfdx function and give it the dfdx matrix
            for(Spring tempSptring : springs) {
            	tempSptring.addDfdx(dfdx); // adding the dfdx to the springs
            	tempSptring.addForce(f); // adding forces to the spring
            }
            
            // so adding to the matrix of A (first part calculated), -(h�+h*alpha)*K
            A.add(-((elapsed*elapsed)+elapsed*Spring.c), dfdx); // A solved
            
            b.add(elapsed, f); // first part of B, h*f(xa)
            //going to do  K  (which is dfdx) / Spring.c is alpha1
            
            //multiplying dfdx by xdot, then adds it to b
            dfdx.multAdd(elapsed*elapsed+elapsed*Spring.c, xdot, b);
            
            // A has identity matrix of M, everything but main line as M
            CG.solve(A, b, deltaxdot, iterations.getValue());
            
            for(Particle tempParticle : particles) {
                //updating the velocity of the particles
            	// slide taking a closer look at backward euler
            	tempParticle.v.x+=deltaxdot.get(tempParticle.index*2);
            	tempParticle.v.y+=deltaxdot.get(tempParticle.index*2+1);
            	
            	//updating the position of the particles
            	tempParticle.p.x+=elapsed*tempParticle.v.x;
            	tempParticle.p.y+=elapsed*tempParticle.v.y;
            }
            
            
        }
        time = time + elapsed;
        postStepFix();
        computeTime = (System.nanoTime() - now) / 1e9;
    }
    
    @Override
    public void filter(Vector v) {
        for ( Particle p : particles ) {
            if ( !p.pinned ) continue;
            v.set( p.index*2+0, 0 );
            v.set( p.index*2+1, 0 );
        }
    }

    /**
     * Creates a new particle and adds it to the system
     * @param x
     * @param y
     * @param vx
     * @param vy
     * @return the new particle
     */
    public Particle createParticle( double x, double y, double vx, double vy ) {
        Particle p = new Particle( x, y, vx, vy );
        p.index = particles.size();
        particles.add( p );
        return p;
    }
    
    public void remove( Particle p ) {
    	for ( Spring s : p.springs ) {
    		Particle other = s.p1 == p ? s.p2 : s.p1; 
    		other.springs.remove( s );
    		springs.remove( s );
    	}
    	p.springs.clear(); // not really necessary
    	particles.remove( p );
    	// reset indices of each particle :(
    	for ( int i = 0 ; i < particles.size(); i++ ) {
    		particles.get(i).index = i;
    	}
    }
    
    /**
     * Creates a new spring between two particles and adds it to the system.
     * @param p1
     * @param p2
     * @return the new spring
     */
    public Spring createSpring( Particle p1, Particle p2 ) {
        Spring s = new Spring( p1, p2 ); 
        springs.add( s );         
        return s;
    }
    
    /**
     * Removes a spring between p1 and p2 if it exists, does nothing otherwise
     * @param p1
     * @param p2
     * @return true if the spring was found and removed
     */
    public boolean removeSpring( Particle p1, Particle p2 ) {
    	Spring found = null;
    	for ( Spring s : springs ) {
    		if ( ( s.p1 == p1 && s.p2 == p2 ) || ( s.p1 == p2 && s.p2 == p1 ) ) {
    			found = s;
    			break;
    		}
    	}
    	if ( found != null ) {
    		found.p1.springs.remove(found);
    		found.p2.springs.remove(found);
    		springs.remove(found);
			return true;
    	}
    	return false;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        // do nothing
    }

    private int height;
    private int width;

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // update the width and the height for wall collisions
        height = drawable.getSurfaceHeight();
        width = drawable.getSurfaceWidth();
        
        gl.glPointSize( 10 );
        gl.glBegin( GL.GL_POINTS );
        for ( Particle p : particles ) {
            double alpha = 0.5;
            if ( p.pinned ) {
                gl.glColor4d( 1, 0, 0, alpha );
            } else {
                gl.glColor4d( p.color.x, p.color.y, p.color.z, alpha );
            }
            gl.glVertex2d( p.p.x, p.p.y );
        }
        gl.glEnd();
        
        gl.glColor4d(0,.5,.5,.5);
        gl.glLineWidth(2f);
        gl.glBegin( GL.GL_LINES );
        for (Spring s : springs) {
            gl.glVertex2d( s.p1.p.x, s.p1.p.y );
            gl.glVertex2d( s.p2.p.x, s.p2.p.y );
        }
        gl.glEnd();
    }
    
    public BooleanParameter useGravity = new BooleanParameter( "use gravity", true );
    public DoubleParameter gravity = new DoubleParameter( "gravity", 9.8, 0.01, 1000 );
    public DoubleParameter springStiffness = new DoubleParameter( "spring stiffness", 100, 0, 10000 );
    public DoubleParameter springDamping = new DoubleParameter( "spring damping", 0, 0, 50 );
    public DoubleParameter viscousDamping = new DoubleParameter( "viscous damping", 0, 0, 10 );
    public DoubleParameter restitution = new DoubleParameter( "restitution", 0, 0, 1 );
    public JTextArea comments = new JTextArea("enter comments in control panel");
    public IntParameter iterations = new IntParameter( "iterations", 100, 1, 100 );
    
    /** chooses between explicit or implicit integration methods*/
    public BooleanParameter explicit = new BooleanParameter( "explicit", true );
    
    @Override
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( comments );
        vfp.add( useGravity.getControls() );
        vfp.add( gravity.getSliderControls(true) );
        vfp.add( springStiffness.getSliderControls(false) );
        vfp.add( springDamping.getSliderControls(false) );
        vfp.add( viscousDamping.getSliderControls(false) );
        vfp.add( restitution.getSliderControls(false) );
        vfp.add( iterations.getSliderControls() );
        vfp.add( explicit.getControls() );
        return vfp.getPanel();        
    }
    
    @Override
    public String toString() {
        // TODO: Add your name below
        String ret = "Guilherme Eidam Gaya Zanin\n" +
                     comments.getText() + "\n" +
                     "particles = " + particles.size() + "\n";
        if ( explicit.getValue() ) {
            ret += "integrator = " + integrator.getName() + "\n";
        } else {
            ret += "integrator = Backward Euler\n";
        }
        ret += "k = " + springStiffness.getValue() + "\n" +
               "c = " + springDamping.getValue() + "\n" +
               "b = " + viscousDamping.getValue() +"\n" + 
               "time = " + time;
        return ret;
    }
    
}
