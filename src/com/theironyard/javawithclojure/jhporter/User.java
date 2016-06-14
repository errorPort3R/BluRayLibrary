package com.theironyard.javawithclojure.jhporter;

import java.util.ArrayList;

/**
 * Created by jeffryporter on 6/13/16.
 */
public class User
{
    String name;
    ArrayList<Movie> pageList;
    int currentPage = 1;
    int totalPages;
    boolean firstpage = true;
    boolean lastpage;
    boolean showAddForm=false;
    boolean showEditForm = false;
    boolean signedIn = false;
    int editPageId;

    public User(String name)
    {
        this.name = name;
    }
}
