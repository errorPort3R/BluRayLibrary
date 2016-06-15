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

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public ArrayList<Movie> getPageList()
    {
        return pageList;
    }

    public void setPageList(ArrayList<Movie> pageList)
    {
        this.pageList = pageList;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    public int getTotalPages()
    {
        return totalPages;
    }

    public void setTotalPages(int totalPages)
    {
        this.totalPages = totalPages;
    }

    public boolean isFirstpage()
    {
        return firstpage;
    }

    public void setFirstpage(boolean firstpage)
    {
        this.firstpage = firstpage;
    }

    public boolean isLastpage()
    {
        return lastpage;
    }

    public void setLastpage(boolean lastpage)
    {
        this.lastpage = lastpage;
    }

    public int getEditPageId()
    {
        return editPageId;
    }

    public void setEditPageId(int editPageId)
    {
        this.editPageId = editPageId;
    }
}
