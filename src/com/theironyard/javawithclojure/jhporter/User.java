package com.theironyard.javawithclojure.jhporter;

import java.util.ArrayList;

/**
 * Created by jeffryporter on 6/13/16.
 */
public class User
{
    String name;
    ArrayList<Movie> pageList;
    int currentPage;
    int totalPages;
    boolean firstpage;
    boolean lastpage;
    boolean showAddForm;
    boolean showEditForm;
    boolean signedIn;
    int editPageId;

    public User(String name)
    {
        this.name = name;
        currentPage=1;
        firstpage = true;
        showAddForm=false;
        showEditForm = false;
        signedIn = false;
    }
}
