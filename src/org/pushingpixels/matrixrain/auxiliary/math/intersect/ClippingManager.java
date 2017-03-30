/*
 * Copyright (c) 2003-2017 Matrix Rain, Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Matrix Rain, Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.matrixrain.auxiliary.math.intersect;

import org.pushingpixels.matrixrain.auxiliary.math.coord.Point2D;

public final class ClippingManager {
	// Cohen Sutherland cliiping algorithm

	// function to check that the the point is inside the half-plane that is
	// formed by one of the lines that bound the rectangle.
	// it gets the point, a corner point of the rectangle, and the side of the
	// corner to compare.
	// note that we work in the coordinate system of the screen, i.e. "up" is
	// low "y" value.
	private static boolean isInside(Point2D point, Point2D bndryPoint, int side) {
		// side 0 = top
		// side 1 = right
		// side 2 = bottom
		// side 3 = left
		switch (side) {
		case 0: // top
			return (point.getY() >= bndryPoint.getY());
		case 1: // right
			return (point.getX() <= bndryPoint.getX());
		case 2: // bottom
			return (point.getY() <= bndryPoint.getY());
		case 3: // left
			return (point.getX() >= bndryPoint.getX());
		default:
			return false;
		}
	}

	// function to intersect the s-p line segment with one of the lines of the
	// rectangle.
	// IsIntersecting is true when there is intersection.
	// Intersection_Point2D is valid only if there is intersection.
	private static Point2D intersect(Point2D sPoint, Point2D pPoint,
			Point2D bndryPoint, int side) {

		switch (side) {
		case 0:
		case 2: // top, bottom
			if (((sPoint.getY() > bndryPoint.getY()) && (pPoint.getY() > bndryPoint
					.getY()))
					|| ((sPoint.getY() < bndryPoint.getY()) && (pPoint.getY() < bndryPoint
							.getY())))
				return null;

			if (sPoint.getX() == pPoint.getX())
				return new Point2D(sPoint.getX(), bndryPoint.getY());
			else
				return new Point2D(sPoint.getX()
						+ (pPoint.getX() - sPoint.getX())
						* (bndryPoint.getY() - sPoint.getY())
						/ (pPoint.getY() - sPoint.getY()), bndryPoint.getY());

		case 1:
		case 3: // right, left
			if (((sPoint.getX() > bndryPoint.getX()) && (pPoint.getX() > bndryPoint
					.getX()))
					|| ((sPoint.getX() < bndryPoint.getX()) && (pPoint.getX() < bndryPoint
							.getX())))
				return null;

			if (sPoint.getY() == pPoint.getY())
				return new Point2D(bndryPoint.getX(), sPoint.getY());
			else
				return new Point2D(bndryPoint.getX(), sPoint.getY()
						+ (pPoint.getY() - sPoint.getY())
						* (bndryPoint.getX() - sPoint.getX())
						/ (pPoint.getX() - sPoint.getX()));
		default:
			return null;
		}
	}

	private static Point2D[] addPoint(Point2D[] polygon, Point2D point) {
		if (polygon == null)
			return new Point2D[] { point };
		Point2D[] result = new Point2D[polygon.length + 1];
		for (int i = 0; i < polygon.length; i++)
			result[i] = polygon[i];
		result[polygon.length] = point;
		return result;
	}

	private static Point2D[] clip(Point2D[] polygon, Point2D bndryPoint,
			int side) {
		if (polygon == null)
			return null;

		Point2D i, p, s;

		Point2D[] result = null;
		int inLength = polygon.length;

		// last point
		s = polygon[inLength - 1];
		for (int j = 0; j < inLength; j++) {
			// current point
			p = polygon[j];
			if (isInside(p, bndryPoint, side)) {
				if (isInside(s, bndryPoint, side)) {
					result = addPoint(result, p);
				} else {
					i = intersect(s, p, bndryPoint, side);
					if (i != null) {
						result = addPoint(result, i);
						result = addPoint(result, p);
					}
				}
			} else {
				if (isInside(s, bndryPoint, side)) {
					i = intersect(s, p, bndryPoint, side);
					if (i != null) {
						result = addPoint(result, i);
					}
				}
			}
			s = p;
		}
		return result;
	}

	// function to clip a closed polygon. the function clips the polygon against
	// the
	// 4 sides of the rectangle (given by TL and BR points)
	public static Point2D[] clipPolygon(Point2D[] polygon,
			Point2D topLeftPoint, Point2D bottomRightPoint) {

		// clip against the top side of the rectangle
		Point2D[] outT_Polygon = clip(polygon, topLeftPoint, 0);
		if (outT_Polygon == null)
			return null;

		// clip against the right side of the rectangle
		Point2D[] outTR_Polygon = clip(outT_Polygon, bottomRightPoint, 1);
		if (outTR_Polygon == null)
			return null;

		// clip against the bottom side of the rectangle
		Point2D[] outTRB_Polygon = clip(outTR_Polygon, bottomRightPoint, 2);
		if (outTRB_Polygon == null)
			return null;

		// clip against the left side of the rectangle
		Point2D[] outFinalPolygon = clip(outTRB_Polygon, topLeftPoint, 3);
		return outFinalPolygon;
	}

	public static double generalPolygonArea(Point2D[] polygon) {
		if (polygon == null)
			return 0.0;

		int count = polygon.length;
		if (count <= 2)
			return 0.0;

		// edges going from left to right - negative integral
		// edges going from right to left - positive integral

		// compute minimal y
		double minY = polygon[0].getY();
		for (int i = 1; i < count; i++) {
			double currY = polygon[i].getY();
			if (currY < minY)
				minY = currY;
		}

		double area = 0.0;
		for (int i = 0; i < count - 1; i++) {
			double x1 = polygon[i].getX();
			double x2 = polygon[i + 1].getX();
			double y1 = polygon[i].getY();
			double y2 = polygon[i + 1].getY();
			area += ((x1 - x2) * (y1 + y2) / 2.0);
		}
		// edge from last vertice to first vertice
		double x1 = polygon[count - 1].getX();
		double x2 = polygon[0].getX();
		double y1 = polygon[count - 1].getY();
		double y2 = polygon[0].getY();
		area += ((x1 - x2) * (y1 + y2) / 2.0);

		if (area < 0.0)
			return -area;
		else
			return area;
	}

	public static double intersectionArea(Point2D[] polygon, Point2D pointTL,
			Point2D pointBR) {
		Point2D[] intersectionPolygon = clipPolygon(polygon, pointTL, pointBR);
		return generalPolygonArea(intersectionPolygon);
	}
}
