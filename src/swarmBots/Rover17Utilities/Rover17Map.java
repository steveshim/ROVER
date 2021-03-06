package swarmBots.Rover17Utilities;

import common.MapTile;
import enums.Science;
import enums.Terrain;

import java.util.*;

public class Rover17Map {
    private GraphRepresentation graph;
    private int[][] terrainMap;
    private int mapHeight = 500;
    private int mapWidth = 500;

    public Rover17Map(){
        graph = new GraphRepresentation();
        terrainMap = new int[mapHeight][mapWidth];
    }

    public void setTerrainMap(int[][] tMap){
        terrainMap = tMap;
    }

    public int[][] getTerrainMap(){
        return terrainMap;
    }

    public int getMapHeight(){
        return mapHeight;
    }

    public void setMapHeight(int height){
        this.mapHeight = height;
    }

    public int getMapWidth(){
        return mapWidth;
    }

    public void setMapWidth(int width){
        this.mapWidth = width;
    }

    public void setGraph(GraphRepresentation graph){
        this.graph = graph;
    }
    public GraphRepresentation getGraph(){
        return graph;
    }


    // method to update int array map
    public void updateMap(int x, int y, MapTile[][] scanMapTiles) throws IndexOutOfBoundsException{
        //iterate through the rover's vision
        int curX, curY;
        for (int i=0;i<scanMapTiles.length;i++) {
            for (int j=0;j<scanMapTiles.length;j++) {
                //the actual coordinates we are updating on the main map
                curX = x-5+i;
                curY = y-5+j;

                //Create Node with coordinates
                Node tempNode = new Node(new Coordinates(curX, curY));

                //Detect science
                if(scanMapTiles[i][j].getScience() == Science.MINERAL){
                    tempNode.setScience("MINERAL");
                    graph.addSciences(tempNode);
                }

                if (curX < 0 || curY < 0){
                    //no need to create nodes at negative spots
                    continue;
                }
                //checks if tile is blocked, does not create edges
                else if (scanMapTiles[i][j].getTerrain() == Terrain.NONE ||
                        scanMapTiles[i][j].getTerrain() == Terrain.ROCK ||
                        scanMapTiles[i][j].getTerrain() == Terrain.SAND ||
                        (scanMapTiles[i][j].getHasRover()
                                && i != scanMapTiles.length/2 && j != scanMapTiles.length/2)) {
                    tempNode.setPassable(false);
                    if (scanMapTiles[i][j].getTerrain() == Terrain.NONE){
                        tempNode.setTerrain("NONE");
                        terrainMap[curY][curX] = -1;
                    } else if (scanMapTiles[i][j].getTerrain() == Terrain.ROCK){
                        tempNode.setTerrain("ROCK");
                        terrainMap[curY][curX] = 2;
                    } else if (scanMapTiles[i][j].getTerrain() == Terrain.SAND){
                        tempNode.setTerrain("SAND");
                        terrainMap[curY][curX] = 2;
                    } else{
                        terrainMap[curY][curX] = 1;
                        //if rover moves into scan, need to remove any edges through that tile
                        graph.removeNode(tempNode);

                    }
                    //checks if science has been removed since last visit
                    if(!graph.addNode(tempNode)){
                        if (!graph.getNodes().get(graph.getNodes().indexOf(tempNode))
                                .getScience().equals(tempNode.getScience())){
                            graph.getNodes().get(graph.getNodes().indexOf(tempNode)).setScience(tempNode.getScience());
                        }
                    }
                }
                //if tile is clear, create edges through it
                else{
                    terrainMap[curY][curX] = 1;
                    //checks if science has been removed since last visit
                    if(!graph.addNode(tempNode)){
                        if (!graph.getNodes().get(graph.getNodes().indexOf(tempNode))
                                .getScience().equals(tempNode.getScience())){
                            graph.getNodes().get(graph.getNodes().indexOf(tempNode)).setScience(tempNode.getScience());
                        }
                    }
                    //adding edges
                    if (i != 0 && curX != 0) {
                        Coordinates temp = new Coordinates(curX-1, curY);
                        if (graph.getNodes().get(graph.getNodes().indexOf(new Node(temp))).getPassable()) {
                            Edge fromCurrent = new Edge(tempNode, new Node(temp), 1);
                            Edge toCurrent = new Edge(new Node(temp), tempNode, 1);
                            graph.addEdge(fromCurrent);
                            graph.addEdge(toCurrent);
                        }
                    }
                    if (j != 0 && curY != 0) {
                        Coordinates temp = new Coordinates(curX, curY-1);
                        if (graph.getNodes().get(graph.getNodes().indexOf(new Node(temp))).getPassable()) {
                            graph.addEdge(new Edge(tempNode, new Node(temp), 1));
                            graph.addEdge(new Edge(new Node(temp), tempNode, 1));
                        }
                    }
                }
            }
        }
    }



    public LinkedList<Edge> getNextPath(int x, int y, MapTile[][] sm){
        Node current = new Node(new Coordinates(x, y));
        LinkedList<Edge> path = new LinkedList();
        int i=0;
        while (path == null || path.isEmpty()) {
            for (int k=i; k>=0; k--) {
                //South east corner
                if (terrainMap[y+6+i][x+6+i] == 0 && terrainMap[y+5][x+5] != -1) {
                    for (int j = 0; j < (sm.length/2)+1+i; j++) {
                        //East wall
                        Node temp = new Node(new Coordinates(x+5-j+k, y+5+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        //South wall
                        temp = new Node(new Coordinates(x+5+k, y+5-j+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //North east corner
                if ((y-6-i > -1) && terrainMap[y-6-i][x+6+i] == 0 && terrainMap[y-5][x+5] != -1) {
                    for (int j = 0; j < (sm.length/2)+1+i; j++) {
                        //North wall
                        Node temp = new Node(new Coordinates(x+5-j+k, y-5-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        //East wall
                        temp = new Node(new Coordinates(x+5+k, y-5+j-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //North west corner
                if ((x-6-i > -1) && (y-6-i > -1) && terrainMap[y-6-i][x-6-i] == 0 &&
                        terrainMap[y-5][x-5] != -1) {
                    for (int j = 0; j < (sm.length/2)+1+i; j++) {
                        //North wall
                        Node temp = new Node(new Coordinates(x-5+j-k, y-5-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        //West wall
                        temp = new Node(new Coordinates(x-5-k, y-5+j-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //South west corner
                if ((x-6-i > -1) && terrainMap[y+6+i][x-6-i] == 0 && terrainMap[y+5][x-5] != -1) {
                    for (int j = 0; j < (sm.length/2)+1+i; j++) {
                        //South wall
                        Node temp = new Node(new Coordinates(x-5+j-k, y+5+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        //West wall
                        temp = new Node(new Coordinates(x-5-k, y+5-j+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //East wall check
                if (terrainMap[y][x+6+i] == 0 && terrainMap[y][x+5] != -1) {
                    for (int j = 0; j < (sm.length/5)+i; j++) {
                        Node temp = new Node(new Coordinates(x+5+k, y+j));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        temp = new Node(new Coordinates(x+5+k, y-j));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //South wall check
                if (terrainMap[y+6+i][x] == 0 && terrainMap[y+5][x] != -1) {
                    for (int j = 0; j < (sm.length/5)+i; j++) {
                        //East wall
                        Node temp = new Node(new Coordinates(x+j, y+5+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        temp = new Node(new Coordinates(x-j, y+5+k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //West wall check
                if ((x-6-i > -1) && terrainMap[y][x-6-i] == 0 && terrainMap[y][x-5] != -1) {
                    for (int j = 0; j < (sm.length/5)+i; j++) {
                        Node temp = new Node(new Coordinates(x-5-k, y+j));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        temp = new Node(new Coordinates(x-5-k, y-j));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
                //North wall check
                if (y-6-i > -1 && terrainMap[y-6-i][x] == 0 && terrainMap[y-5][x] != -1) {
                    for (int j = 0; j < (sm.length/5)+i; j++) {
                        //East wall
                        Node temp = new Node(new Coordinates(x+j, y-5-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                        temp = new Node(new Coordinates(x-j, y-5-k));
                        path = search(graph, current, temp);
                        if (path != null) {
                            System.out.println("Target :" + temp);
                            return path;
                        }
                    }
                }
            }
            i++;
        }
        return path;
    }

    @Override
    public String toString(){
        return graph.toString();
    }

    public String arrayToString(){
        String mapsAsStrings = Arrays.deepToString(terrainMap);
        return mapsAsStrings;
    }


    /*
    ASTAR MOVED HERE
     */
    public LinkedList<Edge> search(GraphRepresentation graph, Node source, Node dist) {
        Map<Node, Node> parents = new HashMap<>();
        Map<Node, Integer> distances = new HashMap<>();
        Set<Node> visited = new HashSet<>();
        LinkedList<Edge> result = new LinkedList<>();

        PriorityQueue<Node> queue = new PriorityQueue<>(1, new Comparator<Node>() {
            public int compare(Node x, Node y){
                double distX = distances.get(x) + heuristic(x, dist);
                double distY = distances.get(y) + heuristic(y, dist);
                if (distX > distY)
                    return 1;
                else if (distY > distX)
                    return -1;
                else {
                    if(x.getCoord().getY() < y.getCoord().getY())
                        return -1;
                    if (x.getCoord().getX() > y.getCoord().getX() &&
                            Math.abs(x.getCoord().getY()-y.getCoord().getY()) == 1)
                        return 1;
                    else
                        return -1;
                }

            }
        });

        distances.put(source, 0);
        parents.put(source, null);
        queue.add(source);

        while (!queue.isEmpty()) {
            Node temp = queue.poll();

            if (temp.equals(dist)) {
                break;
            }

            for(Node node:graph.neighbors(temp)) {
                if (!visited.contains(node)) {
                    parents.put(node, temp);
                    distances.put(node, distances.get(temp) + 1);
                    visited.add(node);
                    queue.add(node);
                }
                else {
                    int newDistance = distances.get(temp) + 1;
                    if (distances.get(node) > newDistance) {
                        distances.remove(node);
                        parents.remove(node);
                        distances.put(node, newDistance);
                        parents.put(node, temp);
                        queue.add(node);
                    }
                }

            }
        }

        Node currentNode = dist;
        //If there is no path, return null
        if (!parents.containsKey(currentNode)){
            return null;
        }

        while (!currentNode.equals(source)) {
            Node parent = parents.get(currentNode);

            if (parent != null) {
                result.add(new Edge(parent, currentNode, graph.distance(parent, currentNode)));
            }

            currentNode = parents.get(currentNode);
        }

        Collections.reverse(result);

        return result;
    }


    public double heuristic(Node current, Node end){
        Coordinates curXY = current.getCoord();
        Coordinates endXY = end.getCoord();
        double doubleX = Math.pow((double)(curXY.getX() - endXY.getX()),2);
        double doubleY = Math.pow((double)(curXY.getY() - endXY.getY()),2);
        return Math.sqrt(doubleX + doubleY);
    }

    public ArrayList<String> getMoves(LinkedList<Edge> edges){
        ArrayList<String> moves = new ArrayList();
        if (edges == null){
            return moves;
        }
        for (int i = 0; i<edges.size(); i++) {
            Node from = edges.get(i).getFrom();
            Node to = edges.get(i).getTo();
            if (from.getCoord().getX() < to.getCoord().getX())
                moves.add("E");
            else if (from.getCoord().getX() > to.getCoord().getX())
                moves.add("W");
            else if (from.getCoord().getY() < to.getCoord().getY())
                moves.add("S");
            else
                moves.add("N");
        }
        return moves;

    }

}
