package cs4620.framework;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/*
 * Provides methods to generate commonly-useful transformation matrices. Methods
 * are named to clearly indicate what type of space the transformation is meant
 * for (what dimension, and whether the transformation is meant to be used with
 * homogeneous coordinates).
 * 
 * Naming conventions:
 * prefixXD  : produces X-by-X non-homogeneous matrix
 * prefixXDH : produces (X+1)-by-(X+1) matrix for use with X-dimensional homogeneous coordinates
 */

public class Transforms {
	
	public static Matrix3f identity3D()
	{
		return new Matrix3f(1.0f, 0.0f, 0.0f,
				            0.0f, 1.0f, 0.0f,
				            0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix3f identity2DH()
	{
		return identity3D();
	}
	
	public static Matrix4f identity4D()
	{
		return new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f,
				            0.0f, 1.0f, 0.0f, 0.0f,
				            0.0f, 0.0f, 1.0f, 0.0f,
				            0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix4f identity3DH()
	{
		return identity4D();
	}
	
	public static Matrix3f translate2DH(float tx, float ty)
	{
		return new Matrix3f(1.0f, 0.0f, tx,
		                     0.0f, 1.0f, ty,
		                     0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix4f translate3DH(float tx, float ty, float tz)
	{
		return new Matrix4f(1.0f, 0.0f, 0.0f, tx,
		                     0.0f, 1.0f, 0.0f, ty,
		                     0.0f, 0.0f, 1.0f, tz,
		                     0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	/**
	 * Produce a rotation matrix of ang degrees.
	 */
	
	public static Matrix3f rotate2DH(float ang)
	{
		return rotateRadians2DH((ang * (float) Math.PI) / 180.0f);
	}
	
	/**
	 * Produce a rotation matrix of ang radians.
	 */
	
	public static Matrix3f rotateRadians2DH(float ang)
	{
		float c = (float) Math.cos(ang);
		float s = (float) Math.sin(ang);
		return new Matrix3f(c,   -s,    0.0f,
		                    s,    c,    0.0f,
		                    0.0f, 0.0f, 1.0f);
	}
	
	/**
	 * Produce a rotation of ang degrees around the given axis
	 * (0 = x, 1 = y, 2 = z).
	 */
	
	public static Matrix4f rotateAxis3DH(int axis, float ang)
	{
		return rotateAxisRadians3DH(axis, (ang * (float) Math.PI) / 180.0f);
	}
	
	/**
	 * Produce a rotation of ang radians around the given axis
	 * (0 = x, 1 = y, 2 = z).
	 */
	
	public static Matrix4f rotateAxisRadians3DH(int axis, float ang)
	{
		float c = (float) Math.cos(ang);
		float s = (float) Math.sin(ang);
		
		if(axis == 0)
		{
			return new Matrix4f(
					1.0f, 0.0f, 0.0f, 0.0f,
	                0.0f, c,    -s,   0.0f,
	                0.0f, s,    c,    0.0f,
	                0.0f, 0.0f, 0.0f, 1.0f
	                );
		}
		else if (axis == 1)
		{
			return new Matrix4f(
					c,    0.0f, s,    0.0f,
	                0.0f, 1.0f, 0.0f, 0.0f,
	                -s,   0.0f, c,    0.0f,
	                0.0f, 0.0f, 0.0f, 1.0f);
		}
		else if (axis == 2)
		{
			return new Matrix4f(
					c,    -s,   0.0f, 0.0f,
	                s,    c,    0.0f, 0.0f,
	                0.0f, 0.0f, 1.0f, 0.0f,
	                0.0f, 0.0f, 0.0f, 1.0f);
		}
		else
		{
			return identity3DH();
		}
	}
	
	public static Matrix3f scale2DH(float sx, float sy)
	{
		return new Matrix3f(sx,   0.0f, 0.0f,
		                     0.0f, sy,   0.0f,
		                     0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix3f scale2DH(float s)
	{
		return new Matrix3f(s,    0.0f, 0.0f,
		                     0.0f, s,    0.0f,
		                     0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix4f scale3DH(float sx, float sy, float sz)
	{
		return new Matrix4f(sx,   0.0f, 0.0f, 0.0f,
		                     0.0f, sy,   0.0f, 0.0f,
		                     0.0f, 0.0f, sz,   0.0f,
		                     0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	public static Matrix4f scale3DH(float s)
	{
		return scale3DH(s, s, s);
	}
	
	// GL-esque matrix commands
	
	/**
	 * Produce a matrix equivalent to that produced by glPerspective.
	 * The notation of the function follows the man page.
	 */
	
	public static Matrix4f perspective3DH(float fovy, float aspect, float zNear, float zFar)
	{
		// fovy comes in as degrees
		float f = 1.0f / (float) Math.tan(fovy * Math.PI / 360.0);
		
		return new Matrix4f(
				f / aspect,  0.0f,  0.0f,                             0.0f,
				0.0f,        f,     0.0f,                             0.0f,
				0.0f,        0.0f,  (zFar + zNear) / (zNear - zFar),  2.0f * zFar * zNear / (zNear - zFar),
				0.0f,        0.0f, -1.0f,                             0.0f
				);
	}
	
	/**
	 * Produce a matrix equivalent to that produced by gluLookAt.
	 * The notation of the function follows the man page.
	 */
	
	public static Matrix4f lookAt3DH(Point3f eye, Point3f center, Vector3f up)
	{
		Vector3f F = new Vector3f(center);
		F.sub(eye);
		
		Vector3f f = new Vector3f(F);
		f.normalize();
		Vector3f uPrime = new Vector3f(up);
		uPrime.normalize();
		
		Vector3f s = new Vector3f();
		s.cross(f, uPrime);
		Vector3f u = new Vector3f();
		u.cross(s, f);
		
		Matrix4f m = new Matrix4f(
				 s.x,  s.y,  s.z, 0.0f,
				 u.x,  u.y,  u.z, 0.0f,
				-f.x, -f.y, -f.z, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f);
		Matrix4f t = new Matrix4f(
				1.0f, 0.0f, 0.0f, -eye.x,
				0.0f, 1.0f, 0.0f, -eye.y,
				0.0f, 0.0f, 1.0f, -eye.z,
				0.0f, 0.0f, 0.0f, 1.0f
				);
		
		m.mul(t);
		return m;
	}
	
	/**
	 * Produce a matrix equivalent to that produced by glOrtho.
	 */
	
	public static Matrix4f ortho3DH(float l, float r, float b, float t, float n, float f)
	{
		return new Matrix4f(
				2.0f / (r - l),  0.0f,            0.0f,            -(r + l) / (r - l),
				0.0f,            2.0f / (t - b),  0.0f,            -(t + b) / (t - b),
				0.0f,            0.0f,           -2.0f / (f - n),  -(f + n) / (f - n),
				0.0f,            0.0f,            0.0f,            1.0f
				);
	}
	
	/**
	 * Produce a matrix equivalent to that produced by gluOrtho2D.
	 * Applying the transformation
	 *   ortho2DH(l, r, b, t)
	 * has the effect of mapping the rectangle bounded by (l, b) and (r, t) so it
	 * exactly fills a GL viewport.
	 */
	
	public static Matrix3f ortho2DH(float l, float r, float b, float t)
	{
		// mimics gluOrtho2D. Applying the transformation
		//   ortho2DH(l, r, b, t)
		// has the effect of mapping the rectangle bounded by (l, b) and (r, t) so it
		// exactly fills a GL viewport.
		return new Matrix3f(2.0f / (r - l),  0.0f,            -(l + r) / (r - l),
		                    0.0f,            2.0f / (t - b),  -(b + t) / (t - b),
		                    0.0f,            0.0f,            1.0f);
	}
}
