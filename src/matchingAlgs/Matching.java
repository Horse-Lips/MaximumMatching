package matchingAlgs;


import graphComponents.*;
import java.util.*;	//ArrayList, HashMap


public class Matching {
	static HashMap<Integer, ArrayList<Integer>> grouping;
	
	/** Find the maximum matching in the graph */
	public static void maxMatch(Graph g, HashMap<Integer, ArrayList<Integer>> vertGroups) {
		grouping = vertGroups;
		ArrayList<Integer> path;

		while ((path = findAug(g)) != null) { augment(path, g); }
	}


	/** Class used to store vertex information */
	static class VertInfo {
		public Integer parent;		//Vertex' parent in augmenting path
		public Integer treeRoot;	//Root of the augmenting path
		public boolean outer;		//Whether or not the vertex is outer

		public VertInfo(Integer parent, Integer treeRoot, boolean outer) {
			this.parent   = parent;
			this.treeRoot = treeRoot;
			this.outer    = outer;
		}
	}


	/** Class used to represent an edge between two vertices */
	static class Edge {
		public Integer v;	//Vertex at one end of edge
		public Integer w;	//Vertex at other end of edge

		public Edge(Integer v, Integer w) {
			this.v = v;
			this.w = w;
		}
	}


	/** Class representing a blossom in the graph */
	static class Blossom {
		public Integer            root;
		public ArrayList<Integer> cycle;

		public Blossom(Integer root, ArrayList<Integer> cycle) {
			this.root  = root;
			this.cycle = cycle;
		}
	}


	/** Find an augmenting path in G */
	public static ArrayList<Integer> findAug(Graph g) {
		HashMap<Integer, VertInfo> F = new HashMap<Integer, VertInfo>();	//Augmenting path forest
		ArrayList<Edge>            q = new ArrayList<Edge>();				//Edges to expand

		// Add all unmatched vertices to forest with VertInfo and edges to the queue
		for (Vertex v: g.vertList) {
			if (v.matched) { continue; }
			
			ArrayList<Integer> vGrouping = grouping.get(v.id);

			for (Integer w: v.adjList) {
				if (vGrouping == null || !vGrouping.contains(w)) {
					F.put(v.id, new VertInfo(null, v.id, true));
					q.add(new Edge(v.id, w));
				}
			}
		}


		while (!q.isEmpty()) {	//While there is an unmarked edge (v, w)

			Edge e = q.remove(0);
			VertInfo vInfo = F.get(e.v);
			VertInfo wInfo = F.get(e.w);

			if (wInfo != null) {	//We have VertInfo for the neighbour, so it is unmatched
				if (wInfo.outer && wInfo.treeRoot == vInfo.treeRoot) {	//v and w share a root, potential blossom
					Blossom b = findBlossom(e, F);
					ArrayList<Integer> path = findAug(contract(g, b));

					if (path == null) { return null; }
					return expand(path, g, F, b);

				} else if (wInfo.outer && wInfo.treeRoot != vInfo.treeRoot) {	//No shared root, augmenting path
					ArrayList<Integer> path = new ArrayList<Integer>();

					for (Integer u1 = e.v; u1 != null; u1 = F.get(u1).parent) { path.add(0, u1); }
					for (Integer u2 = e.w; u2 != null; u2 = F.get(u2).parent) { path.add(u2);    }

					return path;

				}

			} else if (g.get(e.w).partner != null) {	//No VertInfo so w matched (null if grouped)
				Vertex w = g.get(e.w);

				F.put(e.w,       new VertInfo(e.v, vInfo.treeRoot, false));				
				F.put(w.partner, new VertInfo(e.w, vInfo.treeRoot, true ));

				for (Integer u: g.get(w.partner).adjList) { q.add(new Edge(w.partner, u)); }

			}
		}

		return null;
	}


	/** Augment M along an augmenting path starting from the end vertex */
	public static boolean augment(ArrayList<Integer> path, Graph g) {
		int i = 0;

		while (i < path.size()) {
			g.get(path.get(i)).partner     = path.get(i + 1);
			g.get(path.get(i + 1)).partner = path.get(i);
			i += 2;
		}

		for (Integer j: path) {
			g.get(j).matched = true;

			ArrayList<Integer> jGroup = grouping.get(j);

			if (jGroup != null) { for (Integer k: jGroup) { g.get(k).matched =  true; }} 
		}

		return true;
	}

	
	/** Given an edge in a graph, trace backwards to find the blossom it is a part of */
	public static Blossom findBlossom(Edge e, HashMap<Integer, VertInfo> F) {
		/*Find the root which is the lowest common ancestor of the two nodes*/
		ArrayList<Integer> pathV = new ArrayList<Integer>();
		ArrayList<Integer> pathW = new ArrayList<Integer>();

		for (Integer v = e.v; v != null; v = F.get(v).parent) { pathV.add(0, v); }
		for (Integer w = e.w; w != null; w = F.get(w).parent) { pathW.add(0, w); }

		int misMatch = 0;

		for (; misMatch < pathV.size() && misMatch < pathW.size(); misMatch++) {
			if (pathV.get(misMatch) != pathW.get(misMatch)) { break; }
		}

		/* Now we are able to recover the cycle using the index of the mismatch */
		ArrayList<Integer> cycle = new ArrayList<Integer>();

		for (int i = misMatch - 1; i < pathV.size(); i++) { cycle.add(pathV.get(i)); }
		for (int i = pathW.size() - 1; i >= misMatch; i--) { cycle.add(pathW.get(i)); }

		return new Blossom(pathV.get(misMatch - 1), cycle);
	}


	/** Contract a blossom in a graph */
	public static Graph contract(Graph gOld, Blossom b) {
		Graph gNew = new Graph();
		Vertex newRoot = gNew.get(b.root);

		for (Vertex vOld: gOld.vertList) {
			if (!b.cycle.contains(vOld.id)) {
				Vertex vNew = gNew.get(vOld.id);

				for (Integer wOld: vOld.adjList) {
					if (!b.cycle.contains(wOld)) {
						vNew.adjList.add(wOld);

					} else {
						vNew.adjList.add(b.root);
						gNew.vertList.get(b.root).adjList.add(vNew.id);

					}
				}
			}
		}

		return gNew;
	}

	
	/** Expand a path found in a contracted blossom to the original graph */
	public static ArrayList<Integer> expand(ArrayList<Integer> path, Graph g, HashMap<Integer, VertInfo> F, Blossom b) {
		int index = path.indexOf(b.root);

		if (index == -1) { return path; }	//The path is fine if the root doesn't appear
		
		//Make sure index is even
		if (index % 2 != 0) { Collections.reverse(path); index = path.indexOf(b.root); }
		

		ArrayList<Integer> newPath = new ArrayList<Integer>();
		
		for (int i = 0; i < path.size(); i++) {
			if (path.get(i) != b.root) {
				newPath.add(path.get(i));

			} else {
				newPath.add(b.root);

				int outVert = findVert(g, b, path.get(i + 1));
				int outInd  = b.cycle.indexOf(outVert);

				int start = b.cycle.size();
				int step  = -1;

				if (outInd % 2 == 0) { start = 1; step = 1; }

				for (int j = start; j != outInd + step; j += step) { newPath.add(b.cycle.get(j)); }
			}
		}

		return newPath;
	}

	
	/** Given a node and a blossom, find a node in the blossom that has an edge to the given node */
	public static int findVert(Graph g, Blossom b, int vInd) {
		for (int bInd: b.cycle) {
			if (g.get(vInd).adjList.contains(bInd)) { return bInd; }
		}

		return -1;
	}

}












































