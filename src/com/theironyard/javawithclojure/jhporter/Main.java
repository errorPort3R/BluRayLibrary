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
    static HashMap<String, User> userMap = new HashMap<>();



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
                    double numOfEntries = movieArchive.size();
                    int totalPages = (int)Math.ceil(numOfEntries/MOVIES_PER_PAGE);
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user;
                    if (username == null||username.isEmpty())
                    {
                       user = new User(username);
                        user.signedIn = false;
                    }
                    else
                    {
                        user = userMap.get(username);
                        user.signedIn = true;
                    }

                    //populate pageList
                    user.pageList = new ArrayList<>();
                    if (totalPages>user.currentPage)
                    {
                        for (int i = ((user.currentPage - 1) * MOVIES_PER_PAGE); i < (user.currentPage * MOVIES_PER_PAGE); i++)
                        {
                            user.pageList.add(movieArchive.get(i));
                        }
                    }
                    else
                    {
                        for (int i = ((user.currentPage - 1) * MOVIES_PER_PAGE); i < movieArchive.size(); i++)
                        {
                            user.pageList.add(movieArchive.get(i));
                        }
                    }
                    for(int i = 0;i<user.pageList.size();i++)
                    {
                        if (username != null)
                        {
                            if (user.pageList.get(i).owner.equals(username))
                            {
                                user.pageList.get(i).canDelete = true;
                            }
                                else
                            {
                                user.pageList.get(i).canDelete = false;
                            }
                        }
                        else
                        {
                            user.pageList.get(i).canDelete = false;
                        }
                    }

                    HashMap h = new HashMap();
                    h.put("totalpages",totalPages);
                    h.put("pagenumber", user.currentPage);
                    h.put("current-page-list",user.pageList);
                    h.put("firstpage", user.firstpage);
                    h.put("lastpage", user.lastpage);
                    h.put("show-movie-form",user.showAddForm);
                    h.put("signed-in", user.signedIn);
                    if (username!=null)
                    {
                        h.put("current-user", username);
                    }
                    return new ModelAndView(h, "home.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/next-page",
                (request, response ) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    user.currentPage++;
                    if (user.currentPage == 1)
                    {
                        user.firstpage = true;
                    }
                    else
                    {
                        user.firstpage= false;
                    }

                    if (user.currentPage == user.totalPages)
                    {
                        user.lastpage = true;
                    }
                    else
                    {
                        user.lastpage = false;
                    }
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/previous-page",
                (request, response ) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);


                    user.currentPage--;
                    if (user.currentPage == 1)
                    {
                        user.firstpage = true;
                    }
                    else
                    {
                        user.firstpage= false;
                    }

                    if (user.currentPage == user.totalPages)
                    {
                        user.lastpage = true;
                    }
                    else
                    {
                        user.lastpage = false;
                    }

                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/selectpage",
                (request, response ) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    int chosenPage=user.currentPage;
                    String pageStr = request.queryParams("pageselected");
                    if (pageStr.isEmpty() || Integer.valueOf(pageStr)>user.totalPages ||Integer.valueOf(pageStr)<1)
                    {
                        response.redirect("/");
                        return"";
                    }
                    else
                    {
                        chosenPage = Integer.valueOf(pageStr);
                        user.currentPage = chosenPage;
                    }
                    if (user.currentPage == 1)
                    {
                        user.firstpage = true;
                    }
                    else
                    {
                        user.firstpage= false;
                    }

                    if (user.currentPage == user.totalPages)
                    {
                        user.lastpage = true;
                    }
                    else
                    {
                        user.lastpage= false;
                    }

                    session.attribute("username", username);

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
                    User user;

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
                        user = new User(username);
                        user.signedIn=true;
                    }
                    else if (users.get(username).equals(password))
                    {
                        session.attribute("username", username);
                        user = userMap.get(username);
                        user.signedIn = true;
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
                    User user = userMap.get(username);
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
                    user.showAddForm = false;

                    session.attribute("username", username);
                    response.redirect("/");
                    return"";
                }
        );
        Spark.get(
                "/movie",
                (request, response) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    int identity;
                    int spotInList=-1;
                    if (request.queryParams("show-edit") != null)
                    {
                        user.showEditForm = Boolean.valueOf(request.queryParams("show-edit"));
                    }
                    if (request.queryParams("id") != null)
                    {
                        identity = Integer.valueOf(request.queryParams("id"));
                        user.editPageId = identity;
                    }
                    else
                    {
                        identity = user.editPageId;
                    }
                    Movie movie = null;
                    spotInList = findMovie(identity);
                    movie = movieArchive.get(spotInList);

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
                        m.put("can-edit", movie.canDelete);
                        m.put("show-edit-form",user.showEditForm);
                    }

                    session.attribute("username", username);
                    return new ModelAndView(m, "movie.html");
                },
                new MustacheTemplateEngine()

        );
        Spark.post(
                "/edit-movie",
                (request, response) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

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
                    Movie newMovie = new Movie(title, actors, director, runtime, year, rating, username);
                    movieArchive.remove(spotInList);
                    user.editPageId = newMovie.id;
                    user.showEditForm = false;
                    movieArchive.add(newMovie);
                    Collections.sort(movieArchive);
                    saveMovieArchive(MOVIE_FILE_LOCATION);

                    session.attribute("username", username);
                    response.redirect("/");
                    return"";
                }
        );

        Spark.post(
                "/toggle-add",
                (request, response) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    user.showAddForm = !user.showAddForm;

                    session.attribute("username", username);
                    response.redirect("/");
                    return"";
                }
        );
        Spark.post(
                "/toggle-edit",
                (request, response) ->
                {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    user.showEditForm = !user.showEditForm;

                    session.attribute("username", username);
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
                    String username = session.attribute("username");
                    User user = userMap.get(username);

                    int identity = Integer.valueOf(request.queryParams("id"));
                    int spotInList = -1;
                    spotInList = findMovie(identity);
                    movieArchive.remove(spotInList);
                    Collections.sort(movieArchive);
                    saveMovieArchive(MOVIE_FILE_LOCATION);

                    session.attribute("username", username);
                    response.redirect("/");
                    return"";
                }
        );
        Spark.post(
                "/home",
                (request, response) ->
                {

                    response.redirect("/");
                    return"";
                }
        );
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
