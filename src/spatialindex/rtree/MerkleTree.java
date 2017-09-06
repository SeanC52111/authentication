package spatialindex.rtree;
import java.util.Stack;

import spatialindex.rtree.*;
import spatialindex.rtree.RTree.Data;
public class MerkleTree {
	public MerkleNode buildMerkleTree(int i,RTree rtree) {
		Node n = rtree.readNode(i);
		MerkleNode sn = new MerkleNode();
		sn.correspondNode = n;
		sn.mbr = n.getShape();
		sn.identifier = n.getIdentifier();
		if(n.m_level != 0) {
			for (int cChild = 0; cChild < n.m_children; cChild++)
			{
				MerkleNode nsn = buildMerkleTree(n.m_pIdentifier[cChild],rtree);
				sn.children.add(nsn);
			}
		}
		if(n.m_level == 0) {
			for (int cChild = 0; cChild < n.m_children; cChild++)
			{
				MerkleNode nsn = new MerkleNode();
				nsn.identifier = n.m_pIdentifier[cChild];
				nsn.mbr = n.m_pMBR[cChild];
				sn.children.add(nsn);
			}
		}
		return sn;
	}
}
