package io.github.santipdr.specialpickaxes.artifact;

/** Allocation-free priority arithmetic. Independent of registries and world state. */
final class ExcavationOrder {
    enum Style { NEAREST, SEISMIC, TOP_DOWN, BASE_UP, DEEP, VERTICAL }
    private ExcavationOrder(){}

    static double score(Style style,double x,double y,double z,double lookX,double lookY,double lookZ){
        // Keep operation order identical to Vec3.dot and Vec3.lengthSqr.
        double depth=Math.max(0,x*lookX+y*lookY+z*lookZ),distance=x*x+y*y+z*z;
        return switch(style){
            case SEISMIC -> depth*depth*4+distance*.1;
            case TOP_DOWN -> distance*.15-y*12;
            case BASE_UP -> distance*.15+y*12;
            case DEEP -> distance-depth*depth*.8;
            case VERTICAL -> distance*.5-Math.abs(y)*3;
            case NEAREST -> distance;
        };
    }
}
