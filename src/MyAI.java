/*
	FILE:        MyAI.java
	DESCRIPTION: I implemented a Wumpus World AI agent, which should be able to solve a Wumpus World game.
	
	Wumpus world:
		The Wumpus world is a simple world example to illustrate the worth of a knowledge-based agent and to represent knowledge representation. It was inspired by a video game Hunt the Wumpus by Gregory Yob in 1973.

		The Wumpus world is a cave which has N by N rooms connected with passageways. So there are total N^2 rooms which are connected with each other. We have a knowledge-based agent who will go forward in this world. The cave has a room with a beast which is called Wumpus, who eats anyone who enters the room. The Wumpus can be shot by the agent, but the agent has a single arrow. In the Wumpus world, there are some Pits rooms which are bottomless, and if agent falls in Pits, then he will be stuck there forever. The exciting thing with this cave is that in one room there is a possibility of finding a heap of gold. So the agent goal is to find the gold and climb out the cave without fallen into Pits or eaten by Wumpus. The agent will get a reward if he comes out with gold, and he will get a penalty if eaten by Wumpus or falls in the pit.
	
	Description of Wumpus world:
		To explain the Wumpus world we have given PEAS description as below:

	Performance measure:
		+1000 reward points if the agent comes out of the cave with the gold.
		-1000 points penalty for being eaten by the Wumpus or falling into the pit.
		-1 for each action, and -10 for using an arrow.
		The game ends if either agent dies or came out of the cave.

	Environment:
		A N*N grid of rooms. (N: random number from 4 to 10)
		The agent initially in room square [1, 1], facing toward the right.
		Location of Wumpus and gold are chosen randomly.
		Each square of the cave can be a pit.

	Actuators:
		Left turn,
		Right turn
		Move forward
		Grab
		Release
		Shoot.	

	Sensors:
		The agent will perceive the stench if he is in the room adjacent to the Wumpus. (Not diagonally).
		The agent will perceive breeze if he is in the room directly adjacent to the Pit.
		The agent will perceive the glitter in the room where the gold is present.
		The agent will perceive the bump if he walks into a wall.
		When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.	
 */

import java.util.*;

public class MyAI extends Agent
{

	public MyAI ( )
	{
		// mark start room as safe
		safeRecord[minX][minY] = true;
	}
	
	/*
		Determine the next action based on current situation
		
		stench: The agent will perceive the stench if he is in the room adjacent to the Wumpus. (Not diagonally).
		breeze: The agent will perceive breeze if he is in the room directly adjacent to the Pit.
		glitter: The agent will perceive the glitter in the room where the gold is present.
		bump: The agent will perceive the bump if he walks into a wall.
		scream: When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.		
	 */
	public Action getAction
	(
		boolean stench,
		boolean breeze,
		boolean glitter,
		boolean bump,
		boolean scream
	)
	{
		// If agent bumps on wall(boundary), it will change direction.
		// If agent does not bump on wall(boundary), it will keep moving on current direction.
		if (previousAction == Action.FORWARD) {
			if (bump) {
				// Current agent location does not change
				// Have to change agent direction
				System.out.println("bump");
				switch(currentDir){
					case 0: 
						maxX = currentX;
						targetX = currentX;

						break;
/* 					case 1: currentY--;
						break;
					case 2: currentX--;
						break; */
					case 3: 
						maxY = currentY;
						targetY = currentY;

						break;						
				}
			} else {
				// Update current agent location
				switch(currentDir){
					case 0: currentX++;
						break;
					case 1: currentY--;
						break;
					case 2: currentX--;
						break;
					case 3: currentY++;
						break;						
				}
			}
			
		}
		
		// Marked the visited room
		checkedP[currentX][currentY] = true;
		
		// The agent will grab the gold and walk back to entrance.
		if ( glitter ) {
			gotGold = true;
			leavingCave = true;
			previousAction = Action.GRAB;
			return Action.GRAB;
		}
		System.out.println(" ");
		System.out.println("************************************************");
		System.out.println("currentX: " + currentX + " currentY: " + currentY);
		
		// The agent will perceive the stench if he is in the room adjacent to the Wumpus. (Not diagonally). // Marked the adjacent rooms as danger rooms.
		if ( stench ) {
			System.out.println("stench");
			stenchP[currentX][currentY] = true;
			markDangerPoint();			
		}
		
		// The agent will perceive breeze if he is in the room directly adjacent to the Pit.
		// Marked the adjacent rooms as danger rooms.
		if ( breeze ) {
			System.out.println("breezeP");
			breezeP[currentX][currentY] = true;
			markDangerPoint();
		}
		
		// The agent will perceive no breeze and no stench if he is not in the room directly adjacent to the Pit or the Wumpus.
		// Marked the adjacent rooms as safe rooms.
		// put current room into queue for walking to adjacent rooms in the future
		if ( !breeze && !stench ) {
			putSafeQ();
			markSafePoint();
		}
		
		if (currentX == targetX && currentY == targetY){
			
			// Agent found the gold.  OR   There is no more safe room for agent to walk in.
			// It will set entrance as destination and walk to it
			// Update target position
			if (leavingCave || safeQ.size() == 0){		
			
				targetX = 1;
				targetY = 1;
				leavingCave = true;
				
				// The agent is currently on entrance room.
				if (currentX == 1 && currentY == 1){
					previousAction = Action.CLIMB;
					return Action.CLIMB;
				}
				
			} else {
				// Set the next valid and safe room location as target 
				// The agent will walk to this new target room.
				do{
					// There is no more safe room for agent to walk in.
					if (safeQ.size() == 0){
						targetX = 1;
						targetY = 1;
						leavingCave = true;
						break;
					}
					
					// Obtain the next valid and safe room location from queue
					targetX = safeQ.peek().get(0);
					targetY = safeQ.poll().get(1);
					
				}while (targetX > maxX || targetY > maxY);
				
			}
			
			// Draw a graph for finding out optimum path from current room to target room
			adjanceyMap = new HashMap<Map<String, Integer>, List<Map<String, Integer>>>();
			drawGraph();
			
			// results: Store possible safe paths
			results = new ArrayList<List<Map<String, Integer>>>();
						
			// ministep: ministep from current room to target room
			miniStep = 100;
			
			// Find optimum path and store into results
			findAllPathsDFS(0, currentX, currentY, targetX, targetY, new boolean[11][11], new ArrayDeque<>(), results);
			
			// Remove current room from path
			results.get(0).remove(0);
			
			// Get the next room from path
			nextX=results.get(0).get(0).get("x");
			nextY=results.get(0).get(0).get("y");	

			// Remove next room from path
			results.get(0).remove(0);			
			
		} else if (currentX == nextX && currentY == nextY) {
			
			// Get the next room from path
			nextX=results.get(0).get(0).get("x");
			nextY=results.get(0).get(0).get("y");
			
			// Remove next room from path			
			results.get(0).remove(0);			
		}
		
		if (leavingCave && currentX == minX && currentY == minY) {
			previousAction = Action.CLIMB;
			return Action.CLIMB;
		}
		
		// set target direction
		if (nextX > currentX)
			targetDir = 0;
		if (nextX < currentX)
			targetDir = 2;
		if (nextY > currentY)
			targetDir = 3;	
		if (nextY < currentY)
			targetDir = 1;
		
		// Rotate agent direction
		if(currentDir != targetDir){
			if ( turnL(currentDir) == targetDir ){				
				currentDir = turnL(currentDir);
				previousAction = Action.TURN_LEFT;
				return Action.TURN_LEFT;
			}
			if ( turnL(turnL(currentDir)) == targetDir ){
				currentDir = turnL(currentDir);
				previousAction = Action.TURN_LEFT;
				return Action.TURN_LEFT;
			}
			if ( turnR(currentDir) == targetDir ){
				currentDir = turnR(currentDir);
				previousAction = Action.TURN_RIGHT;
				return Action.TURN_RIGHT;
			}				
		}
		
		// move agent forward if current direction is correct
		if(currentDir == targetDir){	
			previousAction = Action.FORWARD;
			return Action.FORWARD;		
		}		

		return Action.CLIMB;
	}
	
	// Reading from System.in
	Scanner reader = new Scanner(System.in);  
	
	// current room location in X and Y
	int currentX = 1;
	int currentY = 1;
	
	// previous room location in X and Y
	int previousX = 1;
	int previousY = 1;
	
	// boundary of X and Y limit
	int maxX = 10;
	int maxY = 10;
	int minX = 1;
	int minY = 1;
	
	Action previousAction;
	
	// Current facing direction
	int currentDir = 0; 
	
	// Target facing direction
	int targetDir = 0;
	
	// Previous facing direction
	int previousDir = 0;
	
	// Target room location in X and Y
	int targetX = 1;
	int targetY = 1;
	
	// next room location in X and Y	
	int nextX = 1;
	int nextY = 1;
	
	// results: possible and safe paths
	List<List<Map<String, Integer>>> results;
	
	// True if agent has the gold
	boolean gotGold = false;
	
	// True if agent is going to leave the cave
	boolean leavingCave = false;

	// Safe room location
	boolean[][] safeP = new boolean[12][12]; 
	// Visited room location
	boolean[][] checkedP = new boolean[12][12];
	// Stench room location
	boolean[][] stenchP = new boolean[12][12];
	// Breeze room location
	boolean[][] breezeP = new boolean[12][12];
	// Danger room location
	boolean[][] dangerP = new boolean[12][12]; 
	
	// Graph for all valid and safe path
	Map<Map<String, Integer>, List<Map<String, Integer>>> adjanceyMap;
	
	// Store safe room location record as matrix
	boolean[][] safeRecord = new boolean[11][11];
	
	// Safe and unvisited room in queue
	Deque< List<Integer> > safeQ = new ArrayDeque<>();	
	
	// Minimum step from current room to target room
	int miniStep;
		
	// Draw a graph to connect all safe rooms
	void drawGraph(){
		//g = new Graph();
		for ( int i = minX; i <= maxX; i++ ){
			for ( int j = minY; j <= maxY; j++ ){
				if ( i+1 <= maxX && safeP[i+1][j] )
					addConnection( i, j, i+1, j );
				if ( j+1 <= maxY && safeP[i][j+1] )
					addConnection( i, j, i, j+1 );
			}
		}
		
	}
	
	// 0, 1, 2, 3 represent 4 different directions
	// Agent turns left
	int turnL(int dir){
		if (dir == 0)
			return dir = 3;
		if (dir == 1)
			return dir = 0;
		if (dir == 2)
			return dir = 1;
		//if (dir == 3)
			return dir = 2;
	}
	
	// Agent turns right
	int turnR(int dir){
		if (dir == 0)
			return dir = 1;
		if (dir == 1)
			return dir = 2;
		if (dir == 2)
			return dir = 3;
		//if (dir == 3)
			return dir = 0;		
	}
		
	// Put safe room location into queue
	// Record safe room location into matrix to prevent duplicate
	void putSafeQ(){
		
		List<Integer> safexy1 = new ArrayList<>();
		safexy1.add(currentX+1);
		safexy1.add(currentY);
		if (currentX+1 <= maxX && !safeRecord[currentX+1][currentY] ){
			safeQ.add( safexy1 );
			safeRecord[currentX+1][currentY] = true;
		}
		
		List<Integer> safexy2 = new ArrayList<>();
		safexy2.add(currentX-1);
		safexy2.add(currentY);
		if (currentX-1 >= minX && !safeRecord[currentX-1][currentY]  ){
			safeQ.add( safexy2 );
			safeRecord[currentX-1][currentY] = true;
		}
		
		List<Integer> safexy3 = new ArrayList<>();
		safexy3.add(currentX);
		safexy3.add(currentY+1);
		if (currentY+1 <= maxY && !safeRecord[currentX][currentY+1]  ){
			safeQ.add( safexy3 );
			safeRecord[currentX][currentY+1] = true;
		}
		
		List<Integer> safexy4 = new ArrayList<>();
		safexy4.add(currentX);
		safexy4.add(currentY-1);
		if (currentY-1 >= minY && !safeRecord[currentX][currentY-1]  ){
			safeQ.add( safexy4 );
			safeRecord[currentX][currentY-1] = true;
		}
	}
	
	// Mark current room and adjacent rooms as safe
	void markSafePoint(){
			safeP[currentX][currentY] = true;
		if (currentX+1 <= maxX)
			safeP[currentX+1][currentY] = true;
		if (currentX-1 >= minX )
			safeP[currentX-1][currentY] = true;
		if (currentY+1 <= maxY )
			safeP[currentX][currentY+1] = true;
		if (currentY-1 >= minX )
			safeP[currentX][currentY-1] = true;			
	}
	
	// Mark adjacent rooms as danger if they have not been visited
	void markDangerPoint(){
		if (!checkedP[currentX+1][currentY] && !safeP[currentX+1][currentY] )
			dangerP[currentX+1][currentY] = true;
		if (!checkedP[currentX-1][currentY] && !safeP[currentX-1][currentY] )
			dangerP[currentX-1][currentY] = true;	
		if (!checkedP[currentX][currentY+1] && !safeP[currentX][currentY+1] )
			dangerP[currentX][currentY+1] = true;	
		if (!checkedP[currentX][currentY-1] && !safeP[currentX][currentY-1] )
			dangerP[currentX][currentY-1] = true;		
	}
	
	// Connect two adjacent safe rooms to each other in a graph represented in adjanceyMap 
	void addConnection(int ux, int uy, int vx, int vy){

		Map<String, Integer> startPoint = new HashMap<>();
		startPoint.put("x", ux);
		startPoint.put("y", uy);
		
		Map<String, Integer> endPoint = new HashMap<>();
		endPoint.put("x", vx);
		endPoint.put("y", vy);	
		
		if(adjanceyMap.get(startPoint) == null){
			adjanceyMap.put(startPoint, new ArrayList<Map<String, Integer>>());
		}		
						
		if(adjanceyMap.get(endPoint) == null){
			adjanceyMap.put(endPoint, new ArrayList<Map<String, Integer>>());
		}	
								
		adjanceyMap.get(startPoint).add(endPoint);
		adjanceyMap.get(endPoint).add(startPoint);
	}
	
	// Find the optimum path from agent current room to target room by DFS
	void findAllPathsDFS(int depth, int ux, int uy, int vx, int vy, boolean[][] visitedNode, Deque<Map<String, Integer>> path, List<List<Map<String, Integer>>> result){
		Map<String, Integer> startPoint = new HashMap<>();
		startPoint.put("x", ux);
		startPoint.put("y", uy);
		
		// Add to the current path end
		path.add(startPoint); 
		depth++;

		if(depth<miniStep){
			// skip to search deeper if >= ministep value
					
			for(Map<String, Integer> i : adjanceyMap.get(startPoint)){
				if(ux == vx && uy == vy){
					// Current Path: The optimum path from agent current room to destination room
					
					// remove higher step path list
					if (result.size()>0) {
						result.remove(0);
					}
					result.add(new ArrayList<Map<String, Integer>>(path));
					miniStep = depth;
					break;
				}
				
				// Transverse unvisited room in recursive call
				if(!visitedNode[i.get("x")][i.get("y")]){
					visitedNode[i.get("x")][i.get("y")] = true; // Mark visitedNode					
					
					findAllPathsDFS(depth, i.get("x"), i.get("y"), vx, vy, visitedNode, path, result);
					
					visitedNode[i.get("x")][i.get("y")] = false;
				}
			}

		}

		depth--;
		path.removeLast();

	}
}