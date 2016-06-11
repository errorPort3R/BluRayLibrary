package com.theironyard.javawithclojure.jhporter;

import java.util.ArrayList;


/**
 * Created by jeffryporter on 6/9/16.
 */

//saveOrder: rating, releaseYear, minutesRunTime, director, title, owner, actor, actor, actor....
//Constructor order: Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating)
public class Movie implements Comparable
{
    String title;
    ArrayList<String> actors;
    String director;
    int minutesRuntime;
    int releaseYear;
    int rating;
    String owner;
    int id;
    static int sessionUniqueId = 10110;
    boolean canDelete;

    public Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating, String owner) {
        this.title = title;
        this.actors = actors;
        this.director = director;
        this.minutesRuntime = minutesRuntime;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.owner = owner;
        id = sessionUniqueId;
        sessionUniqueId++;

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

