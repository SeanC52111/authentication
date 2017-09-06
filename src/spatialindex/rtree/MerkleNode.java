package spatialindex.rtree;
import java.util.ArrayList;

import spatialindex.rtree.*;
import spatialindex.spatialindex.*;

public class MerkleNode{
	public IShape mbr;
	public String md5;
	public int parent;
	public int identifier;
	public ArrayList<MerkleNode> children;
	public ArrayList<String> childAttrHashValues = new ArrayList<String>();
	public int correspondNodeID;
	public Node correspondNode;
	public String mbrDigest;
	public String childDigest;
	public int level = -1;
	public MerkleNode() {
		
	}
	public MerkleNode(Node n) {
		this.mbr = n.getShape();
		this.identifier = n.getIdentifier();
		this.correspondNode = n;
	}
}
