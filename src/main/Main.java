package main;


import graphComponents.*;
import graphUtils.*;
import java.io.IOException;
import java.util.Objects;
import java.util.HashSet;


public class Main {
    
    public static void main(String[] args) throws IOException {
        Graph g = General.fromSNAPFile("Graphs/6KAS.txt");
        
		g.starReduction();
		g.degreeReduction();
		
    }

}
