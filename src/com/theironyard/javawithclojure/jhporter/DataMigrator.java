package com.theironyard.javawithclojure.jhporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jeffryporter on 6/14/16.
 */
public class DataMigrator
{

    public static ArrayList<Movie> movieArchive;
    public static void loadMovieArchive(String fileLoc)
    {
        movieArchive = new ArrayList<>();
        ArrayList<String> actors;
        File f = new File(fileLoc);
        Scanner fileScanner;
        try {
            fileScanner = new Scanner(f);

            while (fileScanner.hasNext())
            {
                actors = new ArrayList<>();
                String line = fileScanner.nextLine();
                String[] fields = line.split("\\|");
                for (int i = 6; i<fields.length; i++)
                {
                    actors.add(fields[i]);
                }
                Movie movie= new Movie(fields[4],actors,fields[3],Integer.valueOf(fields[2]),Integer.valueOf(fields[1]),Integer.valueOf(fields[0]), fields[5]);
                movieArchive.add(movie);
            }
            fileScanner.close();
        } catch (FileNotFoundException e)
        {

        }
    }

    //saveOrder: rating, releaseYear, minutesRunTime, director, title, owner, actor, actor, actor....
    //Constructor Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating, String owner)

    public static void saveMovieArchive(String fileLoc)
    {
        try
        {
            PrintWriter output = new PrintWriter(fileLoc);
            for (Movie m : movieArchive)
            {
                output.printf("%d|%d|%d|%s|%s|%s|", m.rating, m.releaseYear, m.minutesRuntime, m.director,m.title,m.owner);
                for(int i = 0; i<m.actors.size();i++)
                {
                    if (i==(m.actors.size()-1))
                    {
                        output.printf("%s\n", m.actors.get(i));
                    }
                    else
                    {
                        output.printf("%s|", m.actors.get(i));
                    }
                }
            }
            output.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadUsers(String fileLoc)
    {
        users = new HashMap<>();
        ArrayList<String> actors;
        File f = new File(fileLoc);
        Scanner fileScanner = null;
        try
        {
            fileScanner = new Scanner(f);

            fileScanner.nextLine();
            while (fileScanner.hasNext())
            {
                String line = fileScanner.nextLine();
                String[] fields = line.split("\\|");
                users.put(fields[0],fields[1]);
                userMap.put(fields[0],new User(fields[0]));
            }
        }
        catch (FileNotFoundException e)
        {

        }
    }

    public static void saveUsers(String fileLoc)
    {
        try {
            PrintWriter output = new PrintWriter(fileLoc);

            Iterator iterator = users.keySet().iterator();
            while(iterator.hasNext())
            {
                String key = (String) iterator.next();
                output.printf("%s|%s\n", key, users.get(key));
            }
            output.close();
        }
        catch (Exception e)
        {

        }
    }

    public static int findMovie(int identity)
    {
        int location= -1;
        for(int i=0;i<movieArchive.size();i++)
        {
            if(movieArchive.get(i).id == identity)
            {
                location = i;
                i=movieArchive.size();
            }
        }
        return location;
    }
}
