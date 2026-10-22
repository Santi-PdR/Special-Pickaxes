package io.github.santipdr.specialpickaxes.artifact;

/** Pixel-to-cell mapping shared by the compact pick-copy grid and regression tests. */
public final class ManualChoiceGrid {
    private ManualChoiceGrid() {}

    /** Returns the row-major cell index, or -1 when the point is outside the grid. */
    public static int indexAt(int localX,int localY,int pixelScroll,int columns,int cellWidth,int rowHeight){
        if(localX<0||localY<0||pixelScroll<0||columns<=0||cellWidth<=0||rowHeight<=0)return -1;
        int column=localX/cellWidth;
        if(column>=columns)return -1;
        return ((pixelScroll+localY)/rowHeight)*columns+column;
    }
}
