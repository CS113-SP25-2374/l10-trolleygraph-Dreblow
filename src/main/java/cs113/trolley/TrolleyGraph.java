package cs113.trolley;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javafx.scene.paint.Color;

// ********** Graph Construction ********** //
class TrolleyGraph {
    private final List<TrolleyStation> stations;
    private final List<TrolleyRoute> routes;

    public TrolleyGraph() {
        stations = new ArrayList<>();
        routes = new ArrayList<>();
    }

    // Add a new station (node) to the graph
    public void addStation(String name, int x, int y) {
        // Make sure to check if a station with the same name already exists
        if (getStationByName(name) == null) {
            stations.add(new TrolleyStation(name, x, y));
        }
    }

    // Get a station by its name
    public TrolleyStation getStationByName(String name) {
        for (TrolleyStation station : stations) {
            if (station.getName().equals(name)) {
                return station;
            }
        }
        return null;
    }

    // Get all station names
    public Set<String> getStationNames() {
        Set<String> names = new HashSet<>();
        for (TrolleyStation station : stations) {
            names.add(station.getName());
        }
        return names;
    }

    // Add a new route (edge) between two stations
    public void addRoute(String fromStation, String toStation, int weight, Color color) {
        // Make sure both stations exist before adding the route
        if (getStationByName(fromStation) != null && getStationByName(toStation) != null) {
            routes.add(new TrolleyRoute(fromStation, toStation, weight, color));
            routes.add(new TrolleyRoute(toStation, fromStation, weight, color));
        }
    }

    // Get all stations
    public List<TrolleyStation> getStations() {
        return stations;
    }

    // Get all routes
    public List<TrolleyRoute> getRoutes() {
        return routes;
    }

    // ********** Adjacency Lists ********** //
    public List<String> getAdjacentStations(String stationName) {
        List<String> adjacent = new ArrayList<>();

        for (TrolleyRoute route : routes) {
            if (route.getFromStation().equals(stationName)) {
                adjacent.add(route.getToStation());
            }
        }
    
        return adjacent;
    }

    // Get the weight of a route between two stations
    public int getRouteWeight(String fromStation, String toStation) {
        TrolleyStation fromTrollyStation = this.getStationByName(fromStation);
        if (fromTrollyStation == null) {
            return -1;
        }
        TrolleyStation toTrollyStation = this.getStationByName(toStation);
        if (toTrollyStation == null) {
            return -1;
        }

        int dx = toTrollyStation.getX() - fromTrollyStation.getX();
        int dy = toTrollyStation.getY() - fromTrollyStation.getY();

        return (int) Math.round(Math.sqrt(dx * dx + dy * dy));
    }

    // ********** Breadth First Search (BFS) ********** //
    // Big-O
    // Each station (node) is added to the queue and visited once = O(V)
    // Each route (edge) is examined once when visiting its connected station = O(E)
    // So the total time complexity is O(V + E)
    public List<String> breadthFirstSearch(String startStation, String endStation) {
        Map<String,String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();

        queue.add(startStation);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(endStation)) {
                return reconstructPath(parentMap, startStation, endStation);
            }
            visited.add(current);

            List<String> neighbors = getAdjacentStations(current);
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }

        return null; // No path found
    }

    // ********** Depth First Search (DFS) ********** //
    // Big-O
    // Every vertex once = O(V)
    // Every edge once = O(E)
    // Total Time Complexity is O(V + E)
    public List<String> depthFirstSearch(String startStation, String endStation) {
        Set<String> visited = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();
    
        boolean found = dfsRecursive(startStation, endStation, visited, parentMap);
    
        return found ? reconstructPath(parentMap, startStation, endStation) : null;
    }
    
    private boolean dfsRecursive(String current, String endStation, Set<String> visited, Map<String, String> parentMap) {
        if (current.equals(endStation)) {
            return true;
        }
    
        visited.add(current);
    
        for (String neighbor : getAdjacentStations(current)) {
            if (!visited.contains(neighbor)) {
                parentMap.put(neighbor, current);
                if (dfsRecursive(neighbor, endStation, visited, parentMap)) {
                    return true;
                }
            }
        }
    
        return false;
    }

    // ********** Dijkstra's Algorithm ********** //
    public List<String> dijkstra(String startStation, String endStation) {
        // todo: Implement Dijkstra's Algorithm
        Map<String,String> parentMap = new HashMap<>();
        int[] distances = new int[stations.size()];

        for (int i = 0; i < distances.length; i++) {
            distances[i] = Integer.MAX_VALUE;
        }

        PriorityQueue<DNode> pq = new PriorityQueue<>();
        pq.add(new DNode(startStation, 0));

        while(!pq.isEmpty()) {
            DNode node = pq.poll();
            if (node.name.equals(endStation)) {
                return reconstructPath(parentMap, startStation, endStation);
            }
            TrolleyStation station = getStationByName(node.name);
            int i = stations.indexOf(station);
            int distance = distances[i];

            List<String> neighbors = getAdjacentStations(node.name);
            for ( String neighbor : neighbors) {
                TrolleyStation nstation = getStationByName(neighbor);
                int j = stations.indexOf(nstation);
                int old_distance = distances[j];
                int weight = getRouteWeight(node.name, neighbor);

                if (old_distance > distance + weight) {
                    parentMap.put(neighbor, node.name);
                    distances[j] = distance + weight;
                    DNode dnode = new DNode(neighbor, distances[j]);
                    pq.remove(dnode);
                    pq.add(dnode);
                }
            }
        }
        
        return null; // No path found
    }

    class DNode implements Comparable<DNode> {
        String name;
        int distance;

        DNode(String name, int distance) {
            this.name = name;
            this.distance = distance;
        }

        @Override
        public int compareTo(DNode arg0) {
            return 0;
        }
        
    }

    // Helper method to reconstruct the path from start to end using the parent map
    private List<String> reconstructPath(Map<String, String> parentMap, String start, String end) {
        List<String> path = new ArrayList<>();
        String current = end;
    
        while (current != null) {
            path.add(0, current);
            if (current.equals(start)) break;
            current = parentMap.get(current);
        }
    
        return path.get(0).equals(start) ? path : null;
    }
}