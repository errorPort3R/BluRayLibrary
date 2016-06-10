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
        return this.title.compareTo(movie.title);
    }
}

