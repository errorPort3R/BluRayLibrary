package com.theironyard.javawithclojure.jhporter;

import java.util.ArrayList;


/**
 * Created by jeffryporter on 6/9/16.
 */

//saveOrder: rating, releaseYear, minutesRunTime, director, title, owner, actor, actor, actor....
//Constructor order: Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating)
public class Movie implements Comparable
{
    int id;
    String title;
    ArrayList<String> actors;
    String director;
    int minutesRuntime;
    int releaseYear;
    int rating;
    int userId;
    boolean canDelete;


    public Movie()
    {
    }

    public Movie(int id, String title, String actors, String director, int minutesRuntime, int releaseYear, int rating, int userId) {
        this.id = id;
        this.title = title;
        this.actors = new ArrayList<>();
        String[] fields = actors.split("\\|");
        for (String actor : fields)
        {
            this.actors.add(actor);
        }
        this.director = director;
        this.minutesRuntime = minutesRuntime;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.userId = userId;

    }

    public Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating, int userId)
    {
        this.title = title;
        this.actors = actors;
        this.director = director;
        this.minutesRuntime = minutesRuntime;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.userId = userId;
    }

    public Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating)
    {
        this.title = title;
        this.actors = actors;
        this.director = director;
        this.minutesRuntime = minutesRuntime;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public ArrayList<String> getActors()
    {
        return actors;
    }

    public void setActors(ArrayList<String> actors)
    {
        this.actors = actors;
    }

    public String getDirector()
    {
        return director;
    }

    public void setDirector(String director)
    {
        this.director = director;
    }

    public int getMinutesRuntime()
    {
        return minutesRuntime;
    }

    public void setMinutesRuntime(int minutesRuntime)
    {
        this.minutesRuntime = minutesRuntime;
    }

    public int getReleaseYear()
    {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear)
    {
        this.releaseYear = releaseYear;
    }

    public int getRating()
    {
        return rating;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete)
    {
        this.canDelete = canDelete;
    }

    @Override
    public int compareTo(Object o)
    {
        Movie movie = (Movie) o;
        int tryOne = this.title.compareTo(movie.title);
        if (tryOne == 0)
        {
            if(this.releaseYear > movie.releaseYear)
            {
                tryOne=-1;
            }
            else if(this.releaseYear < movie.releaseYear)
            {
                tryOne = 1;
            }
            else
            {
                tryOne=0;
            }
        }
        return tryOne;
    }
}

