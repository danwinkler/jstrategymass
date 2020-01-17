package com.danwink.strategymass;

import java.util.ArrayList;

public class GridBucket<E> {
    public final ArrayList<E> empty = new ArrayList<>();
    int width, height, depth;
    ArrayList<E>[][][] buckets;

    @SuppressWarnings("unchecked")
    public GridBucket(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        buckets = new ArrayList[depth][height][width];
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buckets[z][y][x] = new ArrayList<E>();
                }
            }
        }
    }

    public ArrayList<E> get(int x, int y, int z) {
        if( x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth ) return empty;
        return buckets[z][y][x];
    }

    public void put(E e, int x, int y, int z) {
        buckets[z][y][x].add(e);
    }

    public void clear() {
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buckets[z][y][x].clear();
                }
            }
        }
    }
}
