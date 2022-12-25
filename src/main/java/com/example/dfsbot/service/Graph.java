package com.example.dfsbot.service;

import java.util.LinkedList;
import java.util.Stack;

import static java.lang.Math.min;

public class Graph {
    private final int V;
    private final LinkedList<Integer>[] adj;
    private final Boolean[] used;
    private final int[] tin;
    private final int[] fup;
    private int timer;

    // Конструктор
    public Graph(int v) {
        V = v;
        adj = new LinkedList[v];
        used = new Boolean[v];
        tin = new int[v];
        fup = new int[v];
        timer = 0;

        for (int i = 0; i < v; ++i)
            adj[i] = new LinkedList<>();
    }

    public void addOrtEdge(int v, int w) {
        adj[v - 1].add(w - 1);
    }

    public void addEdge(int v, int w) {
        addOrtEdge(v, w);
        addOrtEdge(w, v);
    }

    public void addEdge(int v, int w, Boolean isOrt) {
        if (isOrt) addOrtEdge(v, w);
        else addEdge(v, w);
    }

    private void topologicalSortUtil(int v, Stack<Integer> answer) {
        used[v] = true;

        for (Integer i : adj[v])
            if (!used[i])
                topologicalSortUtil(i, answer);

        answer.push(v + 1);
    }

    public String topologicalSort() {
        Stack<Integer> answer = new Stack<>();
        StringBuilder answerMessage = new StringBuilder();

        initUsed();

        for (int i = 0; i < V; i++)
            if (!used[i])
                topologicalSortUtil(i, answer);

        while (!answer.empty()) {
            answerMessage.append(answer.pop()).append(" ");
        }

        return String.valueOf(answerMessage);
    }

    private void initUsed() {
        for (int i = 0; i < V; ++i)
            used[i] = false;
    }

    private void dfs(int v, int p, Stack<String> answer, Boolean flag) {
        used[v] = true;
        tin[v] = fup[v] = timer++;
        int count = 0;

        for (int i = 0; i < adj[v].size(); ++i) {
            int to = adj[v].get(i);

            if (to == p) continue;
            if (used[to]) {
                fup[v] = min(fup[v], tin[to]);
                continue;
            }

            dfs(to, v, answer, flag);

            fup[v] = min(fup[v], fup[to]);

            if (flag) {
                if (fup[to] >= tin[v] && p != -1)
                    answer.add(Integer.toString(v + 1));
            } else if (fup[to] > tin[v])
                answer.add("(" + (v + 1) + ", " + (to + 1) + ")");

            count++;
        }

        if (flag && p == -1 && count > 1) {
            answer.add(Integer.toString(v + 1));
        }
    }

    private String buildAnswer(Stack<String> answer, String s0, String s1, String sMany) {
        if (answer.isEmpty()) {
            return s0;
        }

        int size = answer.size();

        if (size == 1) return s1 + "\n" + answer.pop();

        StringBuilder answerMessage = new StringBuilder(sMany).append(size).append("\n");

        while (!answer.isEmpty()) {
            answerMessage.append(answer.pop()).append("\n");
        }

        return String.valueOf(answerMessage);
    }

    private String findCommonUtil(int t, Boolean flag, String s0, String s1, String sMany) {
        Stack<String> answer = new Stack<>();
        timer = t;

        initUsed();

        for (int i = 0; i < V; ++i)
            if (!used[i])
                dfs(i, -1, answer, flag);

        return buildAnswer(answer, s0, s1, sMany);
    }

    public String findBridges() {
        String s0 = "There is no bridges in graph";
        String s1 = "There is one bridge in graph";
        String sMany = "Number of bridges in graph is ";

        return findCommonUtil(0, false, s0, s1, sMany);
    }

    public String findArticulationPoints() {
        String s0 = "There is no articulation points in graph";
        String s1 = "There is one articulation point in graph";
        String sMany = "Number of articulation points in graph is ";

        return findCommonUtil(1, true, s0, s1, sMany);
    }
}
