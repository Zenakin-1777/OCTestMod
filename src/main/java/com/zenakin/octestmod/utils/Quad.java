package com.zenakin.octestmod.utils;

public class Quad<W, X, Y, Z> extends Triple<W, X, Y> {
    private Z four = null;
    public Quad(W one, X two, Y three, Z four) {
        super(one, two, three);
        this.four = four;
    }
    public Z getFour() {return four;}
    public void setFour(Z four) {this.four = four;}
}
