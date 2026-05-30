package edu.ankara.audiometer.domain.model;
public enum Ear { RIGHT, LEFT; public Ear other(){ return this==RIGHT?LEFT:RIGHT; } public String symbol(){return this==RIGHT?"O":"X";} }
