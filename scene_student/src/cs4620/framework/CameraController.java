package cs4620.framework;

import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * A view controller with associated camera and scene drawer.
 * The drawer class draws geometry to a previously-configured
 * viewport given projection and modelview matrices, and the
 * camera class specifies the matrices used by the drawer.
 * 
 * Provides machinery for using right mouse button to drag
 * cameras around; per-camera-type specifics are implemented
 * in subclasses.
 */

public abstract class CameraController extends ViewController {

	// drawing
	protected Camera camera;
	protected GLSceneDrawer drawer;
	protected ViewsCoordinator coordinator;
	protected int viewId;
	
	// mouse state
	protected final Vector2f lastMousePosition = new Vector2f();
	protected final Vector2f currentMousePosition = new Vector2f();
	protected final Vector2f mouseDelta = new Vector2f();
	protected final Vector3f worldMotion = new Vector3f();
	protected int mode;
	
	// static
	public static final int NO_MODE = 0;
	public static final int ROTATE_MODE = 1;
	public static final int TRANSLATE_MODE = 2;
	public static final int ZOOM_MODE = 3;

	//protected int captureFrameNumber = -1;
	//protected boolean captureNextFrame = false;
	//protected static int nFrames  = 0;
	/*
	protected class FrameExporter
	{
		void writeFrame(int width, int height)
		{
			long   timeNS   = -System.nanoTime();
			String number   = String.format("%05d", nFrames); //Utils.getPaddedNumber(nFrames, 5, "0");
			String filename = "export"+"-"+number+".png";/// BUG: DIRECTORY MUST EXIST!

			try{
				System.out.println(filename);
				java.io.File   file     = new java.io.File(filename);
				if(file.exists()) System.out.println("WARNING: OVERWRITING PREVIOUS FILE: "+filename);

				/// WRITE IMAGE: ( :P Screenshot asks for width/height --> cache in GLEventListener.reshape() impl)
				com.jogamp.opengl.util.awt.Screenshot.writeToFile(file, width, height);

				timeNS += System.nanoTime();
				System.out.println((timeNS/1000000)+"ms:  Wrote image: "+filename);

			}catch(Exception e) {
				e.printStackTrace();
				System.out.println("OOPS: "+e);
			}

			nFrames += 1;
		}
	}
	*/

	public CameraController(Camera camera, GLSceneDrawer drawer)
	{
		this(camera, drawer, null, 0);
	}

	public CameraController(Camera camera, GLSceneDrawer drawer, ViewsCoordinator coordinator, int viewId)
	{
		super();
		this.camera = camera;
		this.drawer = drawer;
		this.coordinator = coordinator;
		this.viewId = viewId;
		
		camera.updateFrame();
		mode = NO_MODE;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		drawer.init(drawable, this);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		/*
		if(isOnlyController())
		{
			// we are the only display() to be run, so we should clear buffer
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}
		*/
		/*
		// set scissor to constrain glClear to this viewport, and clear
		gl.glScissor(left, bottom, width, height);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		*/
		setAndClearView(gl);

		camera.updateFrame();
		//setViewport(gl);
		drawer.draw(drawable, this);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		super.reshape(drawable, x, y, width, height);
		if(height > 0)
			camera.setAspect(width * 1.0f / height);
	}

	// FWDO this is now inaccurate -- take bottom and left into consideration
	// Also, naming for indicating whether something expects GL window-space positions
	// or Java canvas-space positions?
	public void windowToViewport(Tuple2f p) {
		int w = width;
		int h = height;
		p.set((2 * p.x - w) / w, (2 * (h - p.y - 1) - h) / h);
	}

	protected boolean isFlagSet(MouseEvent e, int flag) {
		return (e.getModifiersEx() & flag) == flag;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMousePosition.set(e.getX(), e.getY());
		windowToViewport(lastMousePosition);

		if (!isFlagSet(e, MouseEvent.BUTTON1_DOWN_MASK) &&
			!isFlagSet(e, MouseEvent.BUTTON2_DOWN_MASK) &&
			 isFlagSet(e, MouseEvent.BUTTON3_DOWN_MASK))
		{
			if (isFlagSet(e, MouseEvent.ALT_DOWN_MASK))	{
				mode = TRANSLATE_MODE;
			} else if (isFlagSet(e, MouseEvent.CTRL_DOWN_MASK)) {
				mode = ZOOM_MODE;
			} else if (isFlagSet(e, MouseEvent.SHIFT_DOWN_MASK)) {
				mode = ROTATE_MODE;
			} else {
				mode = NO_MODE;
			}
		}
		else
			mode = NO_MODE;

		drawer.mousePressed(e, this);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mode = NO_MODE;
		drawer.mouseReleased(e, this);
	}

	protected abstract void processMouseDragged(MouseEvent e);

	@Override
	public void mouseDragged(MouseEvent e) {
		currentMousePosition.set(e.getX(), e.getY());
		windowToViewport(currentMousePosition);
		mouseDelta.sub(currentMousePosition, lastMousePosition);

		processMouseDragged(e);
		drawer.mouseDragged(e, this);

		lastMousePosition.set(e.getX(), e.getY());
		windowToViewport(lastMousePosition);
	}

	public Camera getCamera() {
		return camera;
	}
	
	public Matrix4f getModelView() {
		return camera.getModelView();
	}
	
	public Matrix4f getProjection() {
		return camera.getProjection();
	}

	public GLSceneDrawer getDrawer()
	{
		return drawer;
	}

	public Vector2f getCurrentMousePosition()
	{
		return currentMousePosition;
	}

	public Vector2f getMouseDelta()
	{
		return mouseDelta;
	}

	public Vector2f getLastMousePosition()
	{
		return lastMousePosition;
	}

	public void captureNextFrame()
	{
		//this.captureNextFrame = true;
	}

	public void setViewId(int viewId)
	{
		this.viewId = viewId;
	}

	public void setCoordinator(ViewsCoordinator coordinator)
	{
		this.coordinator = coordinator;
	}
}