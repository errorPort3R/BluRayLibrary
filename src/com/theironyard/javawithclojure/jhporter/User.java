package com.theironyard.javawithclojure.jhporter;

import java.util.ArrayList;

/**
 * Created by jeffryporter on 6/13/16.
 */
public class User
{

    int id;
    String name;
    String password;
    ArrayList<Movie> pageList;
    int currentPage;
    int totalPages;
    boolean firstpage;
    boolean lastpage;
    int editPageId;

    public User(int id, String name, String password)
    {
        this.id = id;
        this.name = name;
        this.password = password;
        currentPage=1;
        firstpage = true;
    }
}
