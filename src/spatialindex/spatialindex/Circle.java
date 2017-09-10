package spatialindex.spatialindex;

public class Circle implements IShape{
	public Point center=null;
	public double radius;
	public Circle(){
		
	}
	public Circle(Point p,double r){
		this.center = p;
		this.radius = r;
	}
	public boolean intersects(final IShape s) {
		Region r = s.getMBR();
		double [] low = r.m_pLow;
		double []high = r.m_pHigh;
		double cx = this.center.m_pCoords[0];
		double cy = this.center.m_pCoords[1];
		if(cx>=low[0]-radius && cx<=high[0]+radius && cy>=low[1]-radius && cy<=high[1]+radius)
			return true;
		return false;
		
	}
	public boolean contains(final IShape s) {
		return false;
	}
	public boolean touches(final IShape s) {
		return false;
	}
	public double[] getCenter() {
		double[] center = new double[2];
		center[0] = this.center.m_pCoords[0];
		center[1] = this.center.m_pCoords[1];
		return center;
	}
	public long getDimension() {
		long d = 2;
		return d;
	};
	public Region getMBR() {
		Region r = null;
		return r;
	}
	public double getArea() {
		return Math.PI*radius*radius;
	}
	public double getMinimumDistance(final IShape s) {
		return 0;
	}
	
}
