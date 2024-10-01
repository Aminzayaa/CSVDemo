package com.example.CSVDemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String member;
    private Double sep24;
    private Double oct24;
    private Double nov24;
    private Double dec24;
    private Double jan25;
    private Double feb25;
    private Double mar25;

    public Member() {}

    public Member(String member, Double sep24, Double oct24, Double nov24, Double dec24, Double jan25, Double feb25, Double mar25) {
        this.member = member;
        this.sep24 = sep24;
        this.oct24 = oct24;
        this.nov24 = nov24;
        this.dec24 = dec24;
        this.jan25 = jan25;
        this.feb25 = feb25;
        this.mar25 = mar25;
    }

    // Getters and setters
    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public Double getSep24() {
        return sep24;
    }

    public void setSep24(Double sep24) {
        this.sep24 = sep24;
    }

    public Double getOct24() {
        return oct24;
    }

    public void setOct24(Double oct24) {
        this.oct24 = oct24;
    }

    public Double getNov24() {
        return nov24;
    }

    public void setNov24(Double nov24) {
        this.nov24 = nov24;
    }

    public Double getDec24() {
        return dec24;
    }

    public void setDec24(Double dec24) {
        this.dec24 = dec24;
    }

    public Double getJan25() {
        return jan25;
    }

    public void setJan25(Double jan25) {
        this.jan25 = jan25;
    }

    public Double getFeb25() {
        return feb25;
    }

    public void setFeb25(Double feb25) {
        this.feb25 = feb25;
    }

    public Double getMar25() {
        return mar25;
    }

    public void setMar25(Double mar25) {
        this.mar25 = mar25;
    }
}
