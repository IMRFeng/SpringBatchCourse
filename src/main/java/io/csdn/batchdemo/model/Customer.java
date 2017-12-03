package io.csdn.batchdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Zhantao Feng.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer implements Serializable {
    private int id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String address;
    private String city;
    private String country;
    private String state;
    private String zip;
    private String phone1;
    private String phone2;
    private String email;
    private String web;
}
