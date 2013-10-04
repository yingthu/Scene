package cs4620.shape;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;

public class Sphere extends TriangleMesh {

	public Sphere(GL2 gl) {
		super(gl);
	}

	@Override
	public void buildMesh(GL2 gl, float tolerance)
	{
		// TODO (Scene P2): Implement mesh generation for Sphere. Your code should
		// fill arrays of vertex positions/normals and vertex indices for triangles/lines
		// and put this information in the GL buffers using the
		//   set*()
		// methods from TriangleMesh.
		int latitudes = (int)Math.ceil(Math.PI / (tolerance/2.0));
		int longitudes = (int)Math.ceil(2.0*Math.PI / (tolerance/2.0));
		
		// Note that n intervals mean n+1 lines
		int vertexCount = (latitudes+1) * (longitudes+1);
		float[] vertices = new float[3*vertexCount];
		float[] normals = new float[3*vertexCount];
		
		for (int i = 0; i <= latitudes; i++) {
			for (int j = 0; j <= longitudes; j++) {
				float theta = (float) (i * Math.PI / latitudes);
				float phi = (float) (j * 2.0 * Math.PI / longitudes);
				// Transform to xyz coordinates
				// It's a unit sphere
				// Adjust phi to -phi here to fit the axis
				float x = (float) (1.0*Math.sin(theta)*Math.cos(-phi));
				float y = (float) (1.0*Math.sin(-phi)*Math.sin(theta));
				float z = (float) (1.0*Math.cos(theta));
				// Index for current vertex
				int curIndex = j + longitudes * i;
				// Set vertex values
				vertices[3*curIndex] = x;
				vertices[3*curIndex+1] = y;
				vertices[3*curIndex+2] = z;
				// Set normals at vertex
				normals[3*curIndex] = x;
				normals[3*curIndex+1] = y;
				normals[3*curIndex+2] = z;
			}
		}
		// Set triangles
		int triangleCount = latitudes * longitudes * 2;
		int[] triangles = new int[triangleCount*3];
		int tNum = 0;
		for(int i = 0;i < latitudes;i++)
		{
			int nextI = (i + 1) % latitudes;
			for(int j = 0; j < longitudes; j++)
			{
				int nextJ = (j + 1) % longitudes;
				triangles[tNum] = i*longitudes + j;
				triangles[tNum + 1] = nextI*longitudes+j;
				triangles[tNum + 2] = i*longitudes+nextJ;
				
				triangles[tNum + 3] = i*longitudes+nextJ;
				triangles[tNum + 4] = nextI*longitudes+j;
				triangles[tNum + 5] = nextI*longitudes+nextJ;
				tNum += 6;
			}
		}
		// Set lines
		int lineCount = 2 * latitudes * longitudes;
		int[] lines = new int[lineCount*2];
		int toLong = 2 * latitudes * longitudes;
		
		for (int i = 0; i < latitudes; i++)
		{
			// Don't use % here to avoid unnecessary wires
			int nextI = i + 1;
			for (int j = 0; j < longitudes; j++)
			{
				int nextJ = (j + 1) % longitudes;
				// lines in longitudes direction
				lines[2*(j*latitudes+i)+0] = i * longitudes + j;
				lines[2*(j*latitudes+i)+1] = nextI * longitudes + j;
				// lines in latitudes direction
				lines[toLong+2*(i*longitudes+j)+0] = nextI * longitudes + j;
				lines[toLong+2*(i*longitudes+j)+1] = nextI * longitudes + nextJ;
			}
		}
		// Put into buffer
		setVertices(gl, vertices);
		setNormals(gl, normals);
		setTriangleIndices(gl, triangles);
		setWireframeIndices(gl, lines);
	}

	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<Object,Object> result = new HashMap<Object, Object>();
		result.put("type", "Sphere");
		return result;
	}


}
