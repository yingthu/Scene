package cs4620.scene;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.yaml.snakeyaml.Yaml;

import cs4620.framework.Transforms;
import cs4620.shape.Mesh;
import cs4620.shape.Sphere;

public class Scene
{
	protected DefaultTreeModel treeModel;

	public Scene(GL2 gl)
	{
		SceneNode root = new SceneNode("Root");

		MeshNode sphereNode = new MeshNode(gl, "Sphere");
		sphereNode.setMesh(new Sphere(gl));
		root.add(sphereNode);

		LightNode lightNode = new LightNode("Light");
		lightNode.setIntensity(1.0f, 1.0f, 1.0f);
		lightNode.setTranslation(5, 5, 5);
		root.add(lightNode);

		treeModel = new DefaultTreeModel(root);
	} 
	
	public SceneNode getSceneRoot()
	{
		return (SceneNode)treeModel.getRoot();
	}
	
	/**
	 * Traverses the scene, giving each traversed node to the traverser object so
	 * it can do something with the node (e.g. render it, rebuild its meshes,
	 * gather info about lights). The modelView matrix describes a transformation
	 * that takes the scene described by the hierarchy to where it should be drawn.
	 * Each time a node is "traversed", the traverser should be given the node as
	 * well as the full transformation that takes the node up to the coordinate
	 * frame of the root and through the transformation specified by modelView.
	 */
	
	public void traverse(SceneTraverser traverser, Matrix4f modelView)
	{
		traverseHelper(traverser, getSceneRoot(), modelView);
		traverser.traversalDone(); // perform any post-traversal actions
	}
	
	protected void traverseHelper(SceneTraverser traverser, SceneNode node, Matrix4f transform)
	{
		Matrix4f nextTransform = new Matrix4f(transform);
		
		// TODO (Scene P3):
		// * Modify nextTransform so the transform sends geometry from the
		//   local frame of node to the frame of the root of the scene hierarchy
		//   and through the modelView transform that was given to traverse. 
		//   The Transforms class (cs4620.framework) contains basic transformation matrices.
		// * Use traverser.traverseNode() to traverse this node with nextTransform.
		// * Recursively traverse the tree.
	}

	public void rebuildMeshes(GL2 gl, float tolerance)
	{
		RebuildTraverser rebuildTraverser = new RebuildTraverser(gl, tolerance);
		traverse(rebuildTraverser, new Matrix4f()); // we don't care about transformations in this case
	}

	public void render(GL2 gl, SceneProgram program, Matrix4f modelView)
	{
		RenderTraverser renderTraverser = new RenderTraverser(gl, program);
		traverse(renderTraverser, modelView);
	}
	
	public void renderWireframe(GL2 gl, SceneProgram program, Matrix4f modelView)
	{
		RenderTraverser renderTraverser = new RenderTraverser(gl, program, true);
		traverse(renderTraverser, modelView);
	}

	public void setupLighting(GL2 gl, SceneProgram program, Matrix4f modelView)
	{
		LightingTraverser lightingTraverser = new LightingTraverser(gl, program);
		traverse(lightingTraverser, modelView);
	}

	public SceneNode searchForMeshId(int meshId)
	{
		return searchForMeshIdHelper(getSceneRoot(), meshId);		
	}

	private SceneNode searchForMeshIdHelper(SceneNode node, int meshId)
	{
		if (node instanceof MeshNode)
		{
			MeshNode meshNode = (MeshNode)node;
			if (meshNode.getMesh().getId() == meshId)
				return meshNode;
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			SceneNode result = searchForMeshIdHelper(node.getSceneNodeChild(i), meshId);
			if (result != null)
				return result;
		}
		return null;
	}
	
	/**
	 * Save the current scene to a file.
	 */
	public void save(String filename) throws IOException
	{
		Yaml yaml = new Yaml();
		Object rep = ((SceneNode)treeModel.getRoot()).getYamlObjectRepresentation();
		String output = yaml.dump(rep);

		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
	}

	/**
	 * Stolen from http://snippets.dzone.com/posts/show/1335
	 */
	private static String readFileAsString(String filePath) throws IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}

	public void load(GL2 gl, String filename) throws java.io.IOException
	{
		String fileContent = readFileAsString(filename);
		Yaml yaml = new Yaml();
		Object yamlObject = yaml.load(fileContent);

		SceneNode newRoot = SceneNode.fromYamlObject(gl, yamlObject);
		treeModel.setRoot(newRoot);
	}

	private static String getBaseClassName(Class<? extends Object> c)
	{
		String classname = c.getName();
		String name = classname.substring(classname.lastIndexOf('.') + 1, classname.length());
		return name;
	}
	
	public void addNewShape(TreePath path, Mesh mesh, String name)
	{
		//Get the node to insert into
		SceneNode selected = getSceneRoot();
		if (path != null)
			selected = (SceneNode) path.getLastPathComponent();

		MeshNode node = new MeshNode(name, mesh);
		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}

	/**
	 * Add a new light to the tree at the end of the given path.
	 */
	public void addNewLight(TreePath path)
	{
		//Create a light
		LightNode node = new LightNode("Light");

		//Get the node to insert into
		SceneNode selected = (SceneNode)treeModel.getRoot();
		if (path != null)
			selected = (SceneNode) path.getLastPathComponent();

		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}

	/**
	 * Filters out extraneous child nodes from the
	 * supplied array of Treeables.
	 */
	public Vector<SceneNode> filterChildren(SceneNode[] nodes)
	{
		// Trim extraneous child nodes
		Vector<SceneNode> filtered = new Vector<SceneNode>();
		for (int i = 0; i < nodes.length; i++)
			filtered.add(nodes[i]);

		//Find the redundant children
		for (Iterator<SceneNode> j = filtered.iterator(); j.hasNext();) {
			SceneNode tj = j.next();
			for (int i = 0; i < nodes.length; i++) {
				SceneNode ti = nodes[i];
				if (ti == tj) continue;
				if (tj.isNodeAncestor(ti)) j.remove();
			}
		}
		return filtered;
	}

	public void deleteNodes(SceneNode[] nodes)
	{
		Vector<SceneNode> filtered = filterChildren(nodes);
		for (int i=0; i<filtered.size(); i++) {
			SceneNode t = (SceneNode)filtered.get(i);
			if(t == treeModel.getRoot())
				continue;
			treeModel.removeNodeFromParent(t);
		}
		treeModel.reload();
	}

	/**
	 * Groups a set of selected nodes into a new parent. Return
	 * the new parent node.
	 * @param nodes
	 * @param groupName
	 */
	public SceneNode groupNodes(SceneNode[] nodes, String groupName)
	{
		Vector<SceneNode> filtered = filterChildren(nodes);

		if (filtered.size() == 0) return null;

		//Form the new group and add it to the tree
		SceneNode groupNode = new SceneNode(groupName);
		SceneNode firstSelected = (SceneNode)filtered.get(0);
		SceneNode groupParent = (SceneNode)firstSelected.getParent();
		if (groupParent == null) return null;
		int groupIdx = groupParent.getIndex(firstSelected);

		treeModel.insertNodeInto(groupNode, groupParent, groupIdx);
		for (int i = 0; i < filtered.size(); i++)
		{
			SceneNode node = (SceneNode) filtered.get(i);
			treeModel.removeNodeFromParent(node);
			treeModel.insertNodeInto(node, groupNode, groupNode.getChildCount());
		}
		treeModel.reload();

		return groupNode;
	}

	/**
	 * Reparents the items in nodesToReparent underneath
	 * the currently selected node.
	 */
	public void reparent(SceneNode[] nodesToReparent, SceneNode parent)
	{
		// Invalid children selected?
		for (int i=0; i<nodesToReparent.length; i++) {
			if (parent.isNodeAncestor(nodesToReparent[i])) return;
		}

		Vector<SceneNode> filtered = filterChildren(nodesToReparent);

		//reparent the filtered children each seperately
		for (int i=0; i<filtered.size(); i++) {
			SceneNode t = (SceneNode)filtered.get(i);
			t.removeFromParent();
			parent.insert(t,0);
		}

		treeModel.reload();
	}

	public DefaultTreeModel getTreeModel()
	{
		return treeModel;
	}
}

class RenderTraverser extends SceneTraverser {
	
	GL2 gl;
	SceneProgram program;
	boolean drawWireframe;
	
	public RenderTraverser(GL2 gl, SceneProgram program)
	{
		this.gl = gl;
		this.program = program;
		this.drawWireframe = false;
	}
	
	public RenderTraverser(GL2 gl, SceneProgram program, boolean drawWireframe)
	{
		this.gl = gl;
		this.program = program;
		this.drawWireframe = drawWireframe;
	}

	@Override
	public void traverseNode(SceneNode node, Matrix4f toEye) {
		if (node instanceof MeshNode)
		{
			MeshNode meshNode = (MeshNode)node;
			if(drawWireframe)
				meshNode.drawWireframe(gl, program, toEye);
			else
				meshNode.draw(gl, program, toEye);
		}
	}
	
}

class LightingTraverser extends SceneTraverser {
	
	GL2 gl;
	SceneProgram program;
	Vector3f [] lightEyePositions;
	Vector3f [] lightColors;
	int numLights;
	
	public LightingTraverser(GL2 gl, SceneProgram program)
	{
		this.gl = gl;
		this.program = program;
		
		numLights = 0;
		lightEyePositions = new Vector3f [SceneProgram.NUM_LIGHTS];
		lightColors = new Vector3f [SceneProgram.NUM_LIGHTS];
		
		// black out all lights, initially
		for(int i = 0; i < SceneProgram.NUM_LIGHTS; i++)
		{
			lightEyePositions[i] = new Vector3f(0.0f, 0.0f, 0.0f);
			lightColors[i] = new Vector3f(0.0f, 0.0f, 0.0f);
		}
	}

	@Override
	public void traverseNode(SceneNode node, Matrix4f toEye) {
		if (node instanceof LightNode)
		{
			if(numLights >= SceneProgram.NUM_LIGHTS)
			{
				System.err.println("WARNING: scene has more than " + SceneProgram.NUM_LIGHTS + " lights!");
			}
			else
			{
				LightNode lNode = (LightNode) node;
				lightEyePositions[numLights].set(toEye.m03, toEye.m13, toEye.m23);
				lightColors[numLights].set(lNode.intensity[0], lNode.intensity[1], lNode.intensity[2]);
				numLights++;
			}
		}
	}
	
	@Override
	public void traversalDone()
	{
		// we've now traversed the entire scene and seen all lights,
		// so give them to program
		program.setLightPositions(gl, lightEyePositions);
		program.setLightIntensities(gl, lightColors);
	}
	
}

class RebuildTraverser extends SceneTraverser {
	
	GL2 gl;
	float tolerance;
	
	public RebuildTraverser(GL2 gl, float tolerance)
	{
		this.gl = gl;
		this.tolerance = tolerance;
	}

	@Override
	public void traverseNode(SceneNode node, Matrix4f toEye) {
		if (node instanceof MeshNode)
		{
			MeshNode meshNode = (MeshNode)node;
			meshNode.getMesh().buildMesh(gl, tolerance);
		}
	}
	
}
