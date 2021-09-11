package com.miaoshaproject.mq;

import java.util.HashSet;
import java.util.Set;

public class solution {

        class UF{
            int count1 = 0;
            int count0 = 0;
            int[] parent;
            UF(int[][] grid){
                int m = grid.length;
                int n = grid[0].length;
                int num = grid.length*grid[0].length;
                parent = new int[num];
                for(int i = 0; i<m; i++){
                    for(int j = 0; j<n; j++){
                        parent[i*m+j] = i*m+j;
                        if(grid[i][j]==1) count1++;
                        else count0++;
                    }
                }
            }
            int find(int target){
                int root = target;
                while(parent[root] != root){

                    root = parent[root];
                }
                //parent[target] = root;
                return root;

            }
            void union(int i, int j, int value){
                int rooti = find(i);
                int rootj = find(j);
                if(rooti == rootj) return;
                parent[rooti] = rootj;
                if(value == 1) count1--;
                else count0--;
                return;

            }
        }
        public int closedIsland(int[][] grid) {
            int m = grid.length;
            int n = grid[0].length;
            UF uf = new UF(grid);
            //修改dirs数组，这样，只会遍历右侧和下方元素：
            int[][] dirs = new int[][]{ {0, 1}, {1, 0}};
            for(int i = 0; i<m; i++){
                for(int j = 0; j<n; j++){
                    for(int[] dir: dirs){
                        int x = i+dir[0];
                        int y = j+dir[1];
                        if(x<0 || x>=m || y<0 || y>=n || grid[x][y] != grid[i][j]) continue;
                        uf.union(i*n+j, x*n+y, grid[i][j]);
                    }
                }

            }
            int component0 = uf.count0;
            //System.out.print(component0);
            Set<Integer> set = new HashSet<>();
            for(int j =0; j<n; j++){
                if(grid[0][j]==0) {
                    set.add(uf.find(j));
                    System.out.print("0 and j is "+j+"\n");
                    //System.out.print(set.size()+"\n");
                }
                if(grid[m-1][j] == 0) {
                    set.add(uf.find((m-1)*n + j));
                    if(j == 4) System.out.print(uf.find((m-1)*n + j)+"\n");
                    System.out.print("m-1 and j is "+j+"\n");
                    //System.out.print(set.size()+"\n");
                }
            }
            for(int i = 1; i<m-1; i++){

                if(grid[i][0]==0) set.add(uf.find(i*n));
                if(grid[i][n-1] == 0) set.add(uf.find(i*n + n-1));

            }
            return component0- set.size();



        }

    public static void main(String[] args) {
        int[][] input = new int[][]{{0,0,1,0,0},{0,1,0,1,0},{0,1,1,1,0}};
        solution s = new solution();
        s.closedIsland(input);
    }


}
