package comp559.a2ccd;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;

/**
 * Implementation of a robust collision detection
 * @author kry
 */
public class RobustCCD {
	        
    /** number of iterations in the last CCD processing loop, to keep an eye on how tricky the problem is */
    int iters;
    
    /**
     * Creates the new continuous collision detection and response object
     */
    public RobustCCD() {
        // do nothing
    }
    
    /** Might want to turn off collisions when testing? */
    BooleanParameter collision = new BooleanParameter( "apply collision impulses", true );

    /** Ignore this parameter unless you want to explore a Jacobi type resolution of collision */
    BooleanParameter useJacobi = new BooleanParameter( "use Jacobi", false );

    /** Use this as the maximum number of iterations, feel free to modify default, or the maximum! */
    IntParameter maxIterations = new IntParameter("maximum iterations", 60, 30, 300 );

    BooleanParameter repulsion = new BooleanParameter( "apply repulsion impulses", true );

    DoubleParameter restitutionValue = new DoubleParameter( "restitution", .0001, 0, 1 );

    DoubleParameter minDist = new DoubleParameter( "min distance (H)", 2, 0.1, 10 );

    public JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	vfp.setBorder( new TitledBorder("Robust CCD Controls"));
        ((TitledBorder) vfp.getPanel().getBorder()).setTitleFont(new Font("Tahoma", Font.BOLD, 18));
    	vfp.add( collision.getControls() );
    	vfp.add( useJacobi.getControls() );
    	vfp.add(maxIterations.getSliderControls());
    	vfp.add( repulsion.getControls() );
    	vfp.add( restitutionValue.getSliderControls(false) );
    	vfp.add( minDist.getSliderControls(true));    	
    	return vfp.getPanel();
    }
    
    /**
     * Try to deal with contacts before they happen
     * @param h
     * @param system
     */
    public void applyRepulsion( double h, ParticleSystem system ) {
    	if ( ! repulsion.getValue() ) return;
    	// TODO: apply repulsion forces
    	// use minDist.getValue() as the thickness
    	// use your spring stiffness for the repulsion stifness, or create 
    	// new parameters and set their value!
    }
    
    /**
     * Checks all collisions in interval t to t+h
     * @param h
     * @param system 
     * @return true if all collisions resolved
     */
    public boolean check( double h, ParticleSystem system ) {        
    	if ( ! collision.getValue() ) return true; // pretend everything is OK!
    	
    	// TODO: find collisions and apply impulses to resolve them
    	// use maxIterations.getValue() as max iteraitons before giving up
    	// use restitutionValue.getValue() for computing the impulse
    	
        
        return true;
    }
    
}
