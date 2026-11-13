package io.github.santipdr.specialpickaxes.artifact;

/** Splits one hard inspection allowance without rounding a two-type scan above its cap. */
record ProjectileScanBudget(int vanilla,int optional) {
    static ProjectileScanBudget split(int total,boolean separateOptionalType){
        int available=Math.max(0,total);
        if(!separateOptionalType)return new ProjectileScanBudget(available,0);
        int optional=available/2;
        return new ProjectileScanBudget(available-optional,optional);
    }
}
