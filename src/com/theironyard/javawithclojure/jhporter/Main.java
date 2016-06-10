package com.theironyard.javawithclojure.jhporter;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Main
{
    static ArrayList<Movie> movieArchive = new ArrayList<>();
    static String MOVIE_FILE_LOCATION = "movies.txt";
    static String USER_FILE_LOCATION = "users.txt";
    static HashMap<String, String> users;
    static ArrayList<Movie> pageList;
    static int currentPage = 1;
    static int totalPages;
    static boolean firstpage = true;
    static boolean lastpage = false;


    public static void main(String[] args)
    {
        Spark.staticFileLocation("/public");
        Spark.init();

        try
        {
            loadMovieArchive(MOVIE_FILE_LOCATION);
            loadUsers(USER_FILE_LOCATION);
        }
        catch (Exception e)
        {}

        Spark.get(
                "/",
                (request, response) ->
                {




                    HashMap h = new HashMap();
                    h.put("current-page-list",pageList);
                    h.put("firstpage", firstpage);
                    h.put("lastpage", lastpage);
                    return new ModelAndView(h, "home.html");
                },
                new MustacheTemplateEngine()

        );
        Spark.post(
                "/select-page",
                (request, response ) ->
                {
                    int chosenPage=currentPage;
                    String pageStr = request.queryParams("pageselected");
                    if(pageStr.equals("next"))
                    {
                        currentPage++;
                    }
                    else if (pageStr.equals("previous"))
                    {
                        currentPage--;
                    }
                    else if (pageStr.isEmpty() || Integer.valueOf(pageStr)>totalPages ||Integer.valueOf(pageStr)<1)
                    {

                    }
                    else
                    {
                        chosenPage = Integer.valueOf(pageStr);
                        currentPage = chosenPage;
                    }
                    if (currentPage == 1)
                    {
                        firstpage = true;
                    }
                    else
                    {
                        firstpage= false;
                    }

                    if (currentPage == totalPages)
                    {
                        lastpage = true;
                    }
                    else
                    {
                        lastpage= false;
                    }
                    response.redirect("/");
                    return "";
                }
        );





    }

    public static void loadMovieArchive(String fileLoc) throws FileNotFoundException {
        movieArchive = new ArrayList<>();
        ArrayList<String> actors;
        File f = new File(fileLoc);
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(f);
        } catch (FileNotFoundException e) {

        }
        fileScanner.nextLine();
        while (fileScanner.hasNext())
        {
            actors = new ArrayList<>();
            String line = fileScanner.nextLine();
            String[] fields = line.split(",");
            for (int i = 6; i<fields.length; i++)
            {
                actors.add(fields[i]);
            }
            Movie movie= new Movie(fields[4],actors,fields[3],Integer.valueOf(fields[2]),Integer.valueOf(fields[1]),Integer.valueOf(fields[0]), fields[5]);
            movieArchive.add(movie);
        }
        fileScanner.close();
    }

    //saveOrder: rating, releaseYear, minutesRunTime, director, title, owner, actor, actor, actor....
    //Constructor Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating)

    public static void saveMovieArchive(String fileLoc)
    {
        try
        {
            PrintWriter output = new PrintWriter(fileLoc, "UTF-8");
            for (Movie m : movieArchive)
            {
                output.printf("%d,%d,%d,%s,%s,%s", m.rating, m.releaseYear, m.minutesRuntime, m.director,m.title,m.owner);
                for(int i = 0; i<m.actors.size();i++)
                {
                    if (i==(m.actors.size()-1))
                    {
                        output.printf("%s\n", m.actors.get(i));
                    }
                    else
                    {
                        output.printf("%s,", m.actors.get(i));
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
        }
        catch (FileNotFoundException e)
        {

        }
        fileScanner.nextLine();
        while (fileScanner.hasNext())
        {
            String line = fileScanner.nextLine();
            String[] fields = line.split(",");
            users.put(fields[0],fields[1]);
        }
    }

    public static void saveUsers(String fileLoc)
    {
        try {
            PrintWriter output = new PrintWriter(fileLoc, "UTF-8");
            Iterator iterator = users.keySet().iterator();
            while(iterator.hasNext())
            {
                String key = (String) iterator.next();
                output.printf("%s,%s\n", key, users.get(key));
            }
        }
        catch (Exception e)
        {

        }
    }
}
