import java.io.*;
import java.util.*;

import spatialindex.spatialindex.*;
import spatialindex.storagemanager.*;
import spatialindex.rtree.*;
import Tool.Hasher;
public class RTreeTraverse {
	public static void main(String[] args)
	{
		args = new String[] {"tree1"};
		new RTreeTraverse(args);
	}
	
	//test constructor function
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
			//System.out.println(hashnode.get(""+tree.rootID()));
			double[] f1 = new double[2];
			double[] f2 = new double[2];

			//f1[0] = 4; f1[1] = 2.7;
			//f2[0] = 7.5; f2[1] = 5;
			
			f1[0] = 0.219045; f1[1] = 0.0220734;
			f2[0] = 0.467862; f2[1] = 0.528927;

			Region r = new Region(f1, f2);
			//System.out.print("intersect");
			//tree.intersectionQuery(r, vis);
			LinkedList<String> VO = new LinkedList<String>();
			tree.secureRangeQuery(tree.rootID(),r,hashnode,hashdata,VO);
			
			// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
			VOreturn vo = RootHash(VO);
			String []splitvo = vo.hash.split(" ");
			System.out.println(splitvo[splitvo.length-1].equals(hashnode.get(""+tree.rootID())));
			tree.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//transform the String version MBR to double array version
	public static double [] StringtoMBR(String str) {
		String [] s = str.split(" ");
		double []mbr = new double[4];
		mbr[0]=Double.valueOf(s[0]);
		mbr[1]=Double.valueOf(s[1]);
		mbr[2]=Double.valueOf(s[3]);
		mbr[3]=Double.valueOf(s[4]);
		return mbr;
	}
	
	//transform the double array MBR to String version
	public static String MBRtoString(double [] MBR) {
		String str= "";
		str = str + String.valueOf(MBR[0])+" ";
		str = str + String.valueOf(MBR[1])+" ";
		str = str + ": ";
		str = str + String.valueOf(MBR[2])+" ";
		str = str + String.valueOf(MBR[3])+" ";
		return str;
	}
	
	public static boolean isPoint(double n1,double n2,double n3,double n4) {
		if(n1==n3 && n2==n4) {
			return true;
		}
		else {
			return false;
		}
	}
	//enlarge MBR by including MBR_c
	public static double[] enLargeMBR(double[] MBR_c,double[] MBR){
		if(MBR==null) {
			MBR = new double[4];
			for(int i=0;i<4;i++) {
				MBR[i]=MBR_c[i];
			}
		}
		else {
			if(MBR_c[0]<MBR[0])
				MBR[0]=MBR_c[0];
			if(MBR_c[1]<MBR[1])
				MBR[1]=MBR_c[1];
			if(MBR_c[2]>MBR[2])
				MBR[2]=MBR_c[2];
			if(MBR_c[3]>MBR[3])
				MBR[3]=MBR_c[3];
		}
		
		return MBR;
	}
	
	//define a inner class to create return value of RootHash
	public static class VOreturn{
		public String hash="";
		public double[] MBR=null;
	}
	
	//transform a str:(MBR hash) to a VOreturn obj including MBR and hash
	public static VOreturn transform(String str) {
		VOreturn ret = new VOreturn();
		str = str.substring(1,str.length()-1);
		String [] result = str.split(" ");
		ret.hash = result[5];
		double[] MBR = new double[4];
		MBR[0] = Double.valueOf(result[0]);
		MBR[1] = Double.valueOf(result[1]);
		MBR[2] = Double.valueOf(result[3]);
		MBR[3] = Double.valueOf(result[4]);
		ret.MBR = MBR;
		return ret;
	}
	
	//verify the completeness and soundness of the result using VO
	public static VOreturn RootHash(LinkedList<String> VO) {
		String str = "";
		double [] MBR = null;
		VOreturn ret = new VOreturn();
		while(!VO.isEmpty()) {
			String f = VO.poll();
			if(f.charAt(0)>='0' && f.charAt(0)<='9') {
				str = str + new Hasher().stringMD5(f);
				double [] MBR_c = StringtoMBR(f);
				MBR = enLargeMBR(MBR_c,MBR);
			}
			if(f.charAt(0)=='(') {
				VOreturn n = transform(f);
				MBR  = enLargeMBR(n.MBR,MBR);
				str = str + MBRtoString(n.MBR)+ n.hash;
				//System.out.println(str);
			}
			if(f == "[") {
				ret = RootHash(VO);
				MBR = enLargeMBR(ret.MBR,MBR);
				str = str + MBRtoString(ret.MBR)+ret.hash;
				//System.out.println(str);
			}
			if(f == "]") {
				ret.hash = new Hasher().stringMD5(str);
				ret.MBR=MBR;
				return ret;
			}
		}
		ret.hash =str;
		ret.MBR=MBR;
		return ret;
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
