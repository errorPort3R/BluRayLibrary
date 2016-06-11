package com.theironyard.javawithclojure.jhporter;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main
{
    static ArrayList<Movie> movieArchive = new ArrayList<>();
    static int MOVIES_PER_PAGE = 20;
    static String MOVIE_FILE_LOCATION = "movies.txt";
    static String USER_FILE_LOCATION = "users.txt";
    static HashMap<String, String> users;
    static ArrayList<Boolean> canDelete;
    static ArrayList<Movie> pageList;
    static int currentPage = 1;
    static int totalPages= 1;
    static boolean firstpage;
    static boolean lastpage;
    static boolean showAddForm=false;
    static boolean showEditForm=false;
    static boolean signedIn = false;



    public static void main(String[] args)
    {
        Spark.staticFileLocation("/public");
        Spark.init();

        loadMovieArchive(MOVIE_FILE_LOCATION);
        loadUsers(USER_FILE_LOCATION);


        Spark.get(
                "/",
                (request, response) ->
                {
                    //get values for page
                    setPageButtonStatus();
                    double numOfEntries = movieArchive.size();
                    int totalPages = (int)Math.ceil(numOfEntries/MOVIES_PER_PAGE);
                    canDelete =  new ArrayList<>();
                    Session session = request.session();
                    String username = session.attribute("username");

                    //build page list
                    if (username == null || username.isEmpty())
                    {
                        signedIn = false;
                    }
                    else
                    {
                        signedIn = true;
                    }
                    //populate pageList
                    pageList = new ArrayList<>();
                    if (totalPages>currentPage)
                    {
                        for (int i = ((currentPage - 1) * MOVIES_PER_PAGE); i < (currentPage * MOVIES_PER_PAGE); i++)
                        {
                            pageList.add(movieArchive.get(i));
                        }
                    }
                    else
                    {
                        for (int i = ((currentPage - 1) * MOVIES_PER_PAGE); i < movieArchive.size(); i++)
                        {
                            pageList.add(movieArchive.get(i));
                        }
                    }
                    for(int i = 0;i<pageList.size();i++)
                    {
                        if (username != null)
                        {
                            if (pageList.get(i).owner.equals(username))
                            {
                                pageList.get(i).canDelete = true;
                            }
                                else
                            {
                                pageList.get(i).canDelete = false;
                            }
                        }
                        else
                        {
                            pageList.get(i).canDelete = false;
                        }
                    }

                    HashMap h = new HashMap();
                    h.put("totalpages",totalPages);
                    h.put("pagenumber", currentPage);
                    h.put("current-page-list",pageList);
                    h.put("firstpage", firstpage);
                    h.put("lastpage", lastpage);
                    h.put("show-movie-form",showAddForm);//add method for this
                    h.put("signed-in", signedIn);//add method for this
                    if (username!=null)  {h.put("current-user", username);}
                    return new ModelAndView(h, "home.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/movie",
                (request, response) ->
                {
                    String idStr = request.queryParams("id");
                    int identity = -1;
                    Movie movie = null;

                     if (idStr != null)
                    {
                        identity = Integer.valueOf(idStr);
                    }
                    if (identity != -1)
                    {
                        int spotInList = findMovie(identity);
                        movie = movieArchive.get(spotInList);
                    }
                    HashMap m = new HashMap();
                    if (movie !=null)
                    {
                        m.put("title", movie.title);
                        m.put("actors", movie.actors);
                        m.put("director", movie.director);
                        m.put("runtime", movie.minutesRuntime);
                        m.put("rating", movie.rating);
                        m.put("year", movie.releaseYear);
                        m.put("id", movie.id);

                    }
                    return new ModelAndView(m, "movie.html");
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
                        //redirect for new information
                    }
                    else
                    {
                        chosenPage = Integer.valueOf(pageStr);
                        currentPage = chosenPage;
                    }
                    setPageButtonStatus();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/login",
                (request, response ) ->
                {
                    Session session = request.session();
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");

                    //start hashmap of users if one isnt loaded
                    if(users == null)
                    {
                        users = new HashMap<String, String>();
                    }

                    if (username.isEmpty() || password.isEmpty())
                    {
                        response.redirect("/");
                        return "";
                    }

                    //validate user/create new user
                    if (users.get(username) == null)
                    {
                        users.put(username,password);
                        saveUsers(USER_FILE_LOCATION);
                        session.attribute("username", username);
                        signedIn=true;
                    }
                    else if (users.get(username).equals(password))
                    {
                        session.attribute("username", username);
                        signedIn = true;
                    }
                    else
                    {
                        System.out.println("Invalid user!");
                    }


                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/add-movie",
                (request, response) ->
                {

                    Session session = request.session();
                    String username = session.attribute("username");
                    ArrayList<String> actors = new ArrayList();
                    String title = request.queryParams("title");
                    String director = request.queryParams("director");
                    String actorsStr = request.queryParams("actors");
                    int rating = Integer.valueOf(request.queryParams("rating"));
                    String runtimeStr = request.queryParams("runtime");
                    String yearStr = request.queryParams("year");
                    int runtime;
                    if (runtimeStr != null && runtimeStr.matches("[0-9]+"))
                    {
                        runtime = Integer.valueOf(runtimeStr);
                    }
                    else
                    {
                        runtime = 0;
                    }
                    int year;
                    if (yearStr != null && yearStr.matches("[0-9]+"))
                    {
                        year = Integer.valueOf(yearStr);
                    }
                    else
                    {
                        year = 0;
                    }
                    String[] fields = actorsStr.split("\n");
                    for(String field:fields)
                    {
                        String cleanField;
                        if (field.charAt(field.length()-1) == '\r')
                        {
                            cleanField = field.substring(0, field.length() - 1);
                        }
                        else
                        {
                            cleanField = field;
                        }
                        actors.add(cleanField);
                    }
                    Movie newMovie = new Movie(title, actors, director, runtime, year, rating, username);
                    movieArchive.add(newMovie);
                    Collections.sort(movieArchive);
                    saveMovieArchive(MOVIE_FILE_LOCATION);
                    showAddForm = false;

                    response.redirect("/");
                    return"";
                }
        );
        Spark.post(
                "/edit-movie",
                (request, response) ->
                {

                    Session session = request.session();
                    String username = session.attribute("username");
                    ArrayList<String> actors = new ArrayList();
                    String title = request.queryParams("title");
                    String director = request.queryParams("director");
                    String actorsStr = request.queryParams("actors");
                    int rating = Integer.valueOf(request.queryParams("rating"));
                    String runtimeStr = request.queryParams("runtime");
                    String yearStr = request.queryParams("year");
                    int identity = Integer.valueOf(request.queryParams("id"));
                    int spotInList = -1;
                    Movie movie = null;
                    spotInList = findMovie(identity);

                    int runtime;
                    if (runtimeStr != null && runtimeStr.matches("[0-9]+"))
                    {
                        runtime = Integer.valueOf(runtimeStr);
                    }
                    else
                    {
                        runtime = 0;
                    }
                    int year;
                    if (yearStr != null && yearStr.matches("[0-9]+"))
                    {
                        year = Integer.valueOf(yearStr);
                    }
                    else
                    {
                        year = 0;
                    }
                    String[] fields = actorsStr.split("\n");
                    for(String field:fields)
                    {
                        String cleanField;
                        if (field.charAt(field.length()-1) == '\r')
                        {
                            cleanField = field.substring(0, field.length() - 1);
                        }
                        else
                        {
                            cleanField = field;
                        }
                        actors.add(cleanField);
                    }
                    Movie newMovie = new Movie(title, actors, director, runtime, rating, year, username);
                    movieArchive.remove(spotInList);
                    movieArchive.add(newMovie);
                    Collections.sort(movieArchive);
                    saveMovieArchive(MOVIE_FILE_LOCATION);
                    showEditForm = false;

                    HashMap m = new HashMap();
                    m.put("title", movie.title);
                    m.put("actors", movie.actors);
                    m.put("director", movie.director);
                    m.put("runtime", movie.minutesRuntime);
                    m.put("rating", movie.rating);
                    m.put("year", movie.releaseYear);
                    m.put("id", movie.id);

                    session.attribute("username", username);
                    response.redirect("/movie");
                    return"";
                }
        );

        Spark.post(
                "/toggle-add",
                (request, response) ->
                {
                    showAddForm = !showAddForm;

                    response.redirect("/");
                    return"";
                }
        );
        Spark.post(
                "/toggle-edit",
                (request, response) ->
                {
                    showEditForm = !showEditForm;

                    response.redirect("/movie");
                    return"";
                }
        );
        Spark.post(
                "/logout",
                (request, response) ->
                {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return"";
                }
        );
        Spark.post(
                "/delete",
                (request, response) ->
                {
                    Session session = request.session();
                    int identity = Integer.valueOf(request.queryParams("id"));
                    int spotInList = -1;
                    spotInList = findMovie(identity);
                    movieArchive.remove(spotInList);
                    Collections.sort(movieArchive);
                    saveMovieArchive(MOVIE_FILE_LOCATION);

                    response.redirect("/");
                    return"";
                }

        );
    }

    public static void setPageButtonStatus()
    {
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
    }

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
            }
        }
        return location;
    }
}
