# AI_Game
## Wumpus World AI

## Wumpus world:
The Wumpus world is a simple world example to illustrate the worth of a knowledge-based agent and to represent knowledge representation. It was inspired by a video game Hunt the Wumpus by Gregory Yob in 1973.

The Wumpus world is a cave which has N by N rooms connected with passageways. So there are total N^2 rooms which are connected with each other. We have a knowledge-based agent who will go forward in this world. The cave has a room with a beast which is called Wumpus, who eats anyone who enters the room. The Wumpus can be shot by the agent, but the agent has a single arrow. In the Wumpus world, there are some Pits rooms which are bottomless, and if agent falls in Pits, then he will be stuck there forever. The exciting thing with this cave is that in one room there is a possibility of finding a heap of gold. So the agent goal is to find the gold and climb out the cave without fallen into Pits or eaten by Wumpus. The agent will get a reward if he comes out with gold, and he will get a penalty if eaten by Wumpus or falls in the pit.
	
## Description of Wumpus world:
To explain the Wumpus world we have given PEAS description as below:

## Performance measure:
+1000 reward points if the agent comes out of the cave with the gold.
-1000 points penalty for being eaten by the Wumpus or falling into the pit.
-1 for each action, and -10 for using an arrow.
The game ends if either agent dies or came out of the cave.

## Environment:
A N*N grid of rooms. (N: random number from 4 to 10)
The agent initially in room square [1, 1], facing toward the right.
Location of Wumpus and gold are chosen randomly.
Each square of the cave can be a pit.

## Actuators:
Left turn,
Right turn
Move forward
Grab
Release
Shoot.	

## Sensors:
The agent will perceive the stench if he is in the room adjacent to the Wumpus. (Not diagonally).
The agent will perceive breeze if he is in the room directly adjacent to the Pit.
The agent will perceive the glitter in the room where the gold is present.
The agent will perceive the bump if he walks into a wall.
When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.
