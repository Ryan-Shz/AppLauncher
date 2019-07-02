package com.ryan.github.launcher.task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

/**
 * Created by Ryan
 * at 2019/7/1
 */
public class TaskGraph {

    //顶点数
    private int mVertexCount;
    //邻接表
    private List<Integer>[] mVertexArr;

    @SuppressWarnings("unchecked")
    public TaskGraph(int vertexCount) {
        mVertexCount = vertexCount;
        mVertexArr = new ArrayList[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            mVertexArr[i] = new ArrayList<>();
        }
    }

    /**
     * 添加边
     *
     * @param u from
     * @param v to
     */
    public void addEdge(int u, int v) {
        mVertexArr[u].add(v);
    }

    /**
     * 拓扑排序
     */
    public Vector<Integer> topologicalSort() {
        int inDegrees[] = new int[mVertexCount];
        for (int i = 0; i < mVertexCount; i++) { // 初始化所有点的入度数量
            ArrayList<Integer> temp = (ArrayList<Integer>) mVertexArr[i];
            for (int node : temp) {
                inDegrees[node]++;
            }
        }
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < mVertexCount; i++) { // 找出所有入度为0的点
            if (inDegrees[i] == 0) {
                queue.add(i);
            }
        }
        int cnt = 0;
        Vector<Integer> topOrder = new Vector<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topOrder.add(u);
            for (int node : mVertexArr[u]) { // 找到该点（入度为0）的所有邻接点
                if (--inDegrees[node] == 0) { // 把这个点的入度减一，如果入度变成了0，那么添加到入度0的队列里
                    queue.add(node);
                }
            }
            cnt++;
        }
        if (cnt != mVertexCount) { // 检查是否有环，理论上拿出来的点的次数和点的数量应该一致，如果不一致，说明有环
            throw new IllegalStateException("Exists a cycle in the graph");
        }
        return topOrder;
    }

}