package com.gnome.gnome.monsters.movements;

import com.gnome.gnome.game.GameController;
import com.gnome.gnome.monsters.Monster;

import java.util.*;

public class FollowingMovement implements MovementStrategy {

    static class Pos  implements Comparable<Pos> {
        int x, y;
        Pos parent;
        int startCost;          // Cost from start to this node
        int toEndCost;          // Heuristic cost to end
        int f;          // Total cost (g + h)
        public Pos(int x, int y){
            this.x = x;
            this.y = y;
        }
        @Override
        public int compareTo(Pos other) {
            return Integer.compare(this.f, other.f);
        }

        // Nodes are equal if they have same coordinates
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pos node = (Pos) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static int map[][];



    @Override
    public void move(Monster monster) {
        int playerX = 0, playerY = 0;


        if (GameController.getGameController() != null) {
            playerX = GameController.getGameController().getPlayer().getX();
            playerY = GameController.getGameController().getPlayer().getY();

            map = GameController.getGameController().getBaseMap();

        }
        else{
            //Game is not started??? SHOULD NOT HAPPEN
            return;
        }
        Pos target = new Pos(playerX, playerY); //GET FROM PLAYER
        Pos current = new Pos(monster.getX(), monster.getY());

        List<Pos> path = findPath( current, target);

        Pos next = path.size() > 1 ? path.get(1) : null;

        if (next == null) {
            // System.out.println("No path found");
            return;
        }

        monster.setPosition(next.x, next.y);
    }

    private static List<Pos> findPath(Pos start, Pos end ) {
        int rows = map.length, cols = map[0].length;

        int[][] path = new int[rows][cols];

        for (int i = 0; i < path[0].length; i++) {
            int[] row = path[i];
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        path[start.x][start.y] = 0;

        start.startCost = 0;
        start.toEndCost = CostHeuristically(start, end);
        start.f = start.startCost + start.toEndCost;

        PriorityQueue<Pos> frontier = new PriorityQueue<>();
        frontier.add(start);

        // Movement directions (up, down, left, right)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!frontier.isEmpty()) {
            Pos current = frontier.poll();

            // Check if we've reached the destination
            if (current.x == end.x && current.y == end.y) {
                return reconstructPath(current);
            }

            // Skip if better path already found
            if (current.startCost > path[current.x][current.y]) continue;


            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // Validate grid bounds
                if (newX < 0 || newX >= rows || newY < 0 || newY >= cols)
                    continue;

                // Check for block
                if (map[newX][newY] == 1) //TODO ADD other types of blocks
                    continue;

                int tentativeG = current.startCost + 1;

                // Update if better path found
                if (tentativeG < path[newX][newY]) {
                    Pos neighbor = new Pos(newX, newY);
                    neighbor.parent = current;
                    neighbor.startCost = tentativeG;
                    neighbor.toEndCost = CostHeuristically(neighbor, end);
                    neighbor.f = neighbor.startCost + neighbor.toEndCost;

                    path[newX][newY] = tentativeG;
                    frontier.add(neighbor);
                }
            }
        }

        List<Pos> emptyPath = new ArrayList<>();
        return emptyPath;
    }

    // Manhattan distance heuristic (for 4-directional movement)
    private static int CostHeuristically(Pos a, Pos b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // Reconstruct path by following parent pointers
    private static List<Pos> reconstructPath(Pos last) {
        LinkedList<Pos> path = new LinkedList<>();
        Pos current = last;
        while (current != null) {
            path.addFirst(current);
            current = current.parent;
        }
        return path;
    }
}
