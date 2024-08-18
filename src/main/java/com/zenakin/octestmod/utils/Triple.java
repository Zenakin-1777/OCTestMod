package com.zenakin.octestmod.utils;

public class Triple<X, Y, Z> extends Double<X, Y> {
    private Z three = null;

    public Triple(X one, Y two, Z three) {
        super(one, two);
        this.three = three;
    }

    public Z getThree() {return three;}
    public void setThree(Z three) { this.three = three;}



}
