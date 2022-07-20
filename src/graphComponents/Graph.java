package graphComponents;


import java.util.ArrayList;
import java.util.HashSet;
import graphUtils.SimpleTuple;
import graphUtils.SimpleQueuePrio;


public class Graph {

    private ArrayList<Vertex> vertList;         //List of all Vertex objects in the graph
    private int               currentVertID;	//ID of next Vertex to be added to the graph

    public Graph(int graphSize) {
        this.vertList = new ArrayList<Vertex>();
        this.currentVertID = graphSize;

        for (int i = 0; i < graphSize; i++) {
            this.vertList.add(new Vertex(i));
        }
    }


    /** Return the number of vertices in the graph */
    public int getSize() {
        return this.vertList.size();
    }


    /** Return the vertex with the specified vertList index */
    public Vertex getVertex(int index) {
        return this.vertList.get(index);    
    }


	/** Create an edge between the two given vertices */
	public void createEdge(Vertex v1, Vertex v2) {
		v1.addToAdj(v2, 1);
		v2.addToAdj(v1, 1);
	}


	/** Helper function for grouping vertices*/
	public void groupVertices(ArrayList<Vertex> verts) {
		for (Vertex v: verts) {
			for (Vertex u: verts) {
				if (v != u) { v.addToGrouping(u); }
			}
		}
	}


    /** Reduce all vertices to degree 3*/
    public void degreeReduction() {
        System.out.println("=== Degree Reduction ===\nInitial graph size: " + this.getSize());

		ArrayList<Vertex> toAddVert = new ArrayList<Vertex>();

        for (Vertex v: this.vertList) {
			ArrayList<Vertex> grouping = new ArrayList<Vertex>();
			grouping.add(v);

            while (v.getDegree() > 3) {
                /* STEP 1: Create vertices v1 and v2 */
                Vertex v1 = new Vertex(this.currentVertID++);
                Vertex v2 = new Vertex(this.currentVertID++);

				grouping.add(v1);	//Group vertices so matchings are the same
				grouping.add(v2);

				this.createEdge(v1, v2);		//Create edge between v1 and v2

				toAddVert.add(v1);				//Add v1 and v2 to graph
				toAddVert.add(v2);

                /* STEP 2: Get last two neighbours u1 and u2 of v and remove them from its adjacency list*/
                Vertex u1 = v.removeFromAdj(v.getDegree() - 1);
                Vertex u2 = v.removeFromAdj(v.getDegree() - 1);

                /* STEP 3: Remove edges between u1, u2 and v */
                u1.removeFromAdj(v);
                u2.removeFromAdj(v);

                /* STEP 4: Add edges between u1, u2 and v2 */
				this.createEdge(u1, v2);
				this.createEdge(u2, v2);

                /** STEP 5: Add edge between v and v1 */
				this.createEdge(v, v1);
            }

			groupVertices(grouping);
        }
		
		this.vertList.addAll(toAddVert);

        System.out.println("Resulting graph size: " + this.getSize() + "\n");
    }

    
    /** Removes all 2-stars by reducing all k-stars to 1-stars */
    public void starReduction() {
        System.out.println("=== k-Star Removal ===\nInitial graph size: " + this.vertList.size());

        ArrayList<Vertex> toRemoveVert = new ArrayList<Vertex>();	//List of vertices to remove from graph
        for (Vertex v: this.vertList) { v.clearTokens(); }  		//Clear all tokens

        for (Vertex v: this.vertList) {
			Vertex[] neighbs = {v.getAdj().get(0), v.getAdj().get(0)};	//Get first neighbour twice

			/* Ignore all non-2-stars and non-3-double-stars */
			if (v.getDegree() == 1 && neighbs[0].getDegree() < 2) { continue; }
			if (v.getDegree() == 2 && (neighbs[0].getDegree() < 3 || (neighbs[1] = v.getAdj().get(1)).getDegree() < 3)) { continue; }
			if (v.getDegree() > 2) { continue; }

			SimpleTuple<Vertex> token = new SimpleTuple<Vertex>(neighbs[0], neighbs[1]);  //Create token on neighbs

			for (Vertex neighb: neighbs) { neighb.setToken(token); }	//Pass token to neighbours

			if (neighbs[0].getToken(token) > 2 && neighbs[1].getToken(token) > 2) {	//2-star/3-double-star exists
				neighbs[0].removeFromAdj(v);	//Remove neighbour connections with v
				neighbs[1].removeFromAdj(v);

				toRemoveVert.add(v);			//Set v as to-be-removed
			}
        }

        this.vertList.removeAll(toRemoveVert);	//Remove all vertices contributing to stars

        System.out.println("Resulting graph size: " + this.vertList.size() + "\n");
    }


	/** Check if degree reduction was successful (Max degree is 3) */
	public void checkDegree() {
		for (Vertex v: this.vertList) {
			if (v.getDegree() > 3) {
				System.out.println("Degree reduction unsuccessful!");
			}
		}
	}

}







































