package com.tq.staybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;

import javax.persistence.JoinColumn;

import javax.persistence.ManyToOne;

import javax.persistence.Table;

import javax.persistence.Id;

// javax.persistence
//.Id 是给「数据库实体」用的
// org.springframework.data.annotation.Id 是给「Spring Data 对象映射」用的

import java.io.Serializable;

/**
 * 1. Go to the com.laioffer.staybooking.model package, create the StayImage class.
 * 2. The content for StayImage class is simple,
     * we need the URL as the link of the image after you upload it to GCS,
     * and the stay field as a foreign key.
 */

@Entity
@Table(name = "stay_image")
public class StayImage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String url;

    @ManyToOne
    @JoinColumn(name = "stay_id")
    @JsonIgnore
    private Stay stay;

    public StayImage(){};

    public StayImage(String url, Stay stay){
        this.url = url;
        this.stay = stay;
    }

    public String getUrl(){
        return url;
    }
    public StayImage setUrl(String url){
        this.url = url;
        return this;
    }

    public Stay getStay(){
        return stay;
    }
    public StayImage setStay(Stay stay){
        this.stay = stay;
        return this;
    }
}
