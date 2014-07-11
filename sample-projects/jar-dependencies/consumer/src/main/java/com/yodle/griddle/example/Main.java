package com.yodle.griddle.example;
import com.yodle.griddle.example.Dependent;
import com.yodle.griddle.example.Base;

public class Main {
    public static void main(String[] args) {
        System.out.println("Able to see class " + Dependent.class);
        System.out.println("Able to see class " + Base.class);
    }
}