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

