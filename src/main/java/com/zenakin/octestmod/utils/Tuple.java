package com.zenakin.octestmod.utils;

public class Tuple<Y, Z> {
    private Y one = null;
    private Z two = null;

    public Tuple(Y one, Z two) {
        this.one = one;
        this.two = two;
    }

    public Y getOne() {return one;}
    public Z getTwo() {return two;}
    public void setOne(Y one) {this.one = one;}
    public void setTwo(Z two) {this.two = two;}
}