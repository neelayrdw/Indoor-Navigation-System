package com.example.indoornavigation;


import java.util.Collections;
import java.util.Vector;

/**
 *
 * @author Ravee
 */
public class Djikstra {

    public int N;
    public int weight[][];
    public int start;
    int dist[];
    boolean visited[];
    public final int INF = 99999;
    public int currNode;
    public int paths[];

    public Djikstra() {
        ;
    }

    public Vector<Integer> getPath(int end) {
        Vector<Integer> vPath = new Vector<Integer>();
        while (end != -1) {
            if (end == start) {
                vPath.add(end);
                break;
            }
            vPath.add(end);
            end = paths[end];
        }
        Collections.reverse(vPath);
        return vPath;
    }

    public void find() {
        dist = new int[N];
        visited = new boolean[N];
        for (int i = 0; i < N; i++) {
            dist[i] = INF;
            visited[i] = false;
        }
        dist[start] = 0;

        paths = new int[N];
        for (int i = 0; i < N; i++) {
            paths[i] = -1;
        }
        currNode = start;
        do {
            updateDistances();
            //System.out.println("CURR NODE: " + currNode);
        } while (getMinNode());
        for (int i = 0; i < N; i++) {
            System.out.println(paths[i] + " ");
        }
    }

    public boolean getMinNode() {
        boolean found = false;
        int minDist = -1;
        for (int i = 0; i < N; i++) {
            if (visited[i] == false) {
                minDist = dist[i];
                currNode = i;
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }

        for (int i = 0; i < N; i++) {
            if (!visited[i]) {
                if (minDist > dist[i]) {
                    minDist = dist[i];
                    currNode = i;
                }
            }
        }
        return true;
    }

    public void updateDistances() {

        for (int to = 0; to < N; to++) {
            if (to == currNode) {
                continue;
            }
            if (visited[to]) {
                continue;
            }
            if (weight[currNode][to] == -1) {
                continue;
            }
            if (dist[to] > dist[currNode] + weight[currNode][to]) {
                dist[to] = dist[currNode] + weight[currNode][to];
                System.out.println("From " + currNode + " TO " + to);
                paths[to] = currNode;
            }
        }
        visited[currNode] = true;
    }
}
