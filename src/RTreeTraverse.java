import java.io.*;
import java.util.*;

import spatialindex.spatialindex.*;
import spatialindex.storagemanager.*;
import spatialindex.rtree.*;

public class RTreeTraverse {
	public static void main(String[] args)
	{
		args = new String[] {"tree"};
		new RTreeTraverse(args);
	}
	RTreeTraverse(String[] args)
	{
		try
		{
			if (args.length != 1)
			{
				System.err.println("Usage: RTreeQuery query_file tree_file query_type [intersection | 10NN].");
				System.exit(-1);
			}

			// Create a disk based storage manager.
			PropertySet ps = new PropertySet();

			ps.setProperty("FileName", args[0]);
				// .idx and .dat extensions will be added.

			IStorageManager diskfile = new DiskStorageManager(ps);

			IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);
				// applies a main memory random buffer on top of the persistent storage manager
				// (LRU buffer, etc can be created the same way).

			PropertySet ps2 = new PropertySet();

			// If we need to open an existing tree stored in the storage manager, we only
			// have to specify the index identifier as follows
			Integer i = new Integer(1); // INDEX_IDENTIFIER_GOES_HERE (suppose I know that in this case it is equal to 1);
			ps2.setProperty("IndexIdentifier", i);
				// this will try to locate and open an already existing r-tree index from file manager file.

			RTree tree = new RTree(ps2, file);
			MyVisitor vis = new MyVisitor();
			//tree.tranverse(vis);
			HashMap<String, String> hashnode = new HashMap<String, String>();  
			HashMap<String, String> hashdata = new HashMap<String, String>();  
			tree.createHashTable(tree.rootID(), hashnode, hashdata);
			//System.out.println(hashnode.size());
			double[] f1 = new double[2];
			double[] f2 = new double[2];

			f1[0] = 6; f1[1] = 6.5;
			f2[0] = 9; f2[1] = 10;

			Region r = new Region(f1, f2);
			//System.out.print("intersect");
			tree.intersectionQuery(r, vis);

			// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
			tree.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// example of a Visitor pattern.
	// findes the index and leaf IO for answering the query and prints
	// the resulting data IDs to stdout.
	class MyVisitor implements IVisitor
	{
		public int m_indexIO = 0;
		public int m_leafIO = 0;
		public ArrayList<MerkleNode> snodes=new ArrayList<MerkleNode>();

		public void visitNode(final INode n)
		{
			if (n.isLeaf()) m_leafIO++;
			else m_indexIO++;
		}

		public void visitData(final IData d)
		{
			//System.out.println(d.getShape());
			System.out.println(d.getIdentifier());
			System.out.println(d.getShape());
				// the ID of this data entry is an answer to the query. I will just print it to stdout.
		}
	}

	// example of a Strategy pattern.
	// traverses the tree by level.
	class MyQueryStrategy implements IQueryStrategy
	{
		private ArrayList ids = new ArrayList();

		public void getNextEntry(IEntry entry, int[] nextEntry, boolean[] hasNext)
		{
			Region r = entry.getShape().getMBR();

			System.out.println(r.m_pLow[0] + " " + r.m_pLow[1]);
			System.out.println(r.m_pHigh[0] + " " + r.m_pLow[1]);
			System.out.println(r.m_pHigh[0] + " " + r.m_pHigh[1]);
			System.out.println(r.m_pLow[0] + " " + r.m_pHigh[1]);
			System.out.println(r.m_pLow[0] + " " + r.m_pLow[1]);
			System.out.println();
			System.out.println();
				// print node MBRs gnuplot style!

			// traverse only index nodes at levels 2 and higher.
			if (entry instanceof INode && ((INode) entry).getLevel() > 1)
			{
				for (int cChild = 0; cChild < ((INode) entry).getChildrenCount(); cChild++)
				{
					ids.add(new Integer(((INode) entry).getChildIdentifier(cChild)));
				}
			}

			if (! ids.isEmpty())
			{
				nextEntry[0] = ((Integer) ids.remove(0)).intValue();
				hasNext[0] = true;
			}
			else
			{
				hasNext[0] = false;
			}
		}
	};

	// example of a Strategy pattern.
	// find the total indexed space managed by the index (the MBR of the root).
	class MyQueryStrategy2 implements IQueryStrategy
	{
		public Region m_indexedSpace;

		public void getNextEntry(IEntry entry, int[] nextEntry, boolean[] hasNext)
		{
			// the first time we are called, entry points to the root.
			IShape s = entry.getShape();
			m_indexedSpace = s.getMBR();

			// stop after the root.
			hasNext[0] = false;
		}
	}
}
