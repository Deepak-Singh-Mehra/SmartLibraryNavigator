package backend;

import java.util.*;

public class Graph {
    private Map<String, List<String>> adjacencyList = new HashMap<>();

    public void addShelf(String shelf) { adjacencyList.putIfAbsent(shelf, new ArrayList<>()); }

    public void addEdge(String s1, String s2) {
        adjacencyList.putIfAbsent(s1, new ArrayList<>()); adjacencyList.putIfAbsent(s2, new ArrayList<>());
        if (!adjacencyList.get(s1).contains(s2)) adjacencyList.get(s1).add(s2);
        if (!adjacencyList.get(s2).contains(s1)) adjacencyList.get(s2).add(s1);
    }

    public void addBookShelf(String shelf, String bookId) { addShelf(shelf); }

    public List<String> shortestPath(String start, String target) {
        List<String> empty = new ArrayList<>();
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(target)) return empty;
        Queue<String> q = new LinkedList<>(); Map<String, String> parent = new HashMap<>();
        Set<String> vis = new HashSet<>();
        q.add(start); vis.add(start); parent.put(start, null);
        while (!q.isEmpty()) {
            String cur = q.poll();
            if (cur.equals(target)) break;
            for (String nb : adjacencyList.get(cur)) {
                if (!vis.contains(nb)) { vis.add(nb); parent.put(nb, cur); q.add(nb); }
            }
        }
        if (!parent.containsKey(target)) return empty;
        List<String> path = new ArrayList<>();
        String cur = target;
        while (cur != null) { path.add(cur); cur = parent.get(cur); }
        Collections.reverse(path); return path;
    }
}
