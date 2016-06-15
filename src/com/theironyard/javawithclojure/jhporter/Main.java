package com.theironyard.javawithclojure.jhporter;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.*;



public class Main
{
    static boolean showAddForm = false;
    static boolean showEditForm = false;
    static boolean signedIn = false;
    static User unknownUser = new User(-1,"anonymous","none");;
    static int MOVIES_PER_PAGE = 20;

    public static void main(String[] args) throws SQLException
    {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS movies (id IDENTITY, title VARCHAR, actors VARCHAR, director VARCHAR, minutes_runtime INT, release_year INT, rating INT, user_id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");

        Spark.staticFileLocation("/public");
        Spark.init();


        Spark.get(
                "/",
                (request, response) ->
                {
                    //get values for page

                    double count=0.0;
                    ResultSet results = stmt.executeQuery("SELECT COUNT (*) AS count FROM movies");
                    if (results.next())
                    {
                        count = (double)results.getInt("count");
                    }
                    int totalPages = (int)Math.ceil(count/MOVIES_PER_PAGE);
                    Session session = request.session();
                    String username = session.attribute("username");

                    User user;
                    if (username == null||username.isEmpty())
                    {
                        user = unknownUser;
                        signedIn = false;
                    }
                    else
                    {
                        user = selectUser(conn, username);
                    }

                    //populate pageList
                    ArrayList<Movie>movieArchive = selectMovies(conn);
                    Collections.sort(movieArchive);
                    user.pageList = new ArrayList<>();
                    if (totalPages>user.currentPage || user.currentPage == 1)
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
                            if (selectUser(conn,username).id == user.pageList.get(i).userId)
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
                    h.put("show-movie-form",showAddForm);
                    h.put("signed-in", signedIn);
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
                    User user = selectUser(conn, username);
                    if (user == null)
                    {
                        user = unknownUser;
                    }

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
                    User user = selectUser(conn, username);
                    if (user == null)
                    {
                        user = unknownUser;
                    }


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
                    User user = selectUser(conn, username);
                    if (user == null)
                    {
                        user = unknownUser;
                    }

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

                    if (username.isEmpty() || password.isEmpty())
                    {
                        signedIn = false;
                        response.redirect("/");
                        return "";
                    }

                    //validate user/create new user
                    if (selectUser(conn, username) == null)
                    {
                        insertUser(conn,username,password);
                        session.attribute("username", username);
                        user = selectUser(conn, username);
                        signedIn = true;
                    }
                    else if (selectUser(conn,username).password.equals(password))
                    {
                        session.attribute("username", username);
                        user = selectUser(conn, username);
                        signedIn = true;
                    }
                    else
                    {
                        System.out.println("Invalid user!");
                        signedIn = false;
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
                    User user = selectUser(conn, username);
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
                    Movie newMovie = new Movie(title, actors, director, runtime, year, rating, selectUser(conn, username).id);
                    insertMovie(conn, newMovie);
                    showAddForm = false;

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
                    User user = selectUser(conn, username);
                    if (user == null)
                    {
                        user = unknownUser;
                    }

                    int identity;
                    if (request.queryParams("show-edit") != null)
                    {
                        showEditForm = Boolean.valueOf(request.queryParams("show-edit"));
                    }
                    if (request.queryParams("id") != null)
                    {
                        identity = Integer.valueOf(request.queryParams("id"));
                        user.editPageId = identity;
                    }
                    else
                    {
                        identity = unknownUser.editPageId;
                    }
                    Movie movie = selectMovie(conn, identity);
                    if (user.id == movie.userId)
                    {
                        movie.canDelete = true;
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
                        m.put("can-edit", movie.canDelete);
                        m.put("show-edit-form",showEditForm);
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
                    User user = selectUser(conn, username);

                    ArrayList<String> actors = new ArrayList();
                    String title = request.queryParams("title");
                    String director = request.queryParams("director");
                    String actorsStr = request.queryParams("actors");
                    int rating = Integer.valueOf(request.queryParams("rating"));
                    String runtimeStr = request.queryParams("runtime");
                    String yearStr = request.queryParams("year");
                    int identity = Integer.valueOf(request.queryParams("id"));
                    Movie movie = selectMovie(conn, identity);

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
                    Movie newMovie = new Movie(title, actors, director, runtime, year, rating, selectUser(conn, username).id);
                    user.editPageId = newMovie.id;
                    showEditForm = false;
                    updateMovie(conn, newMovie);

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
                    User user = selectUser(conn, username);

                    showAddForm = !showAddForm;

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
                    User user = selectUser(conn, username);

                    unknownUser.editPageId = Integer.valueOf(request.queryParams("id"));
                    showEditForm = !showEditForm;

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
                    User user = selectUser(conn, username);

                    int identity = Integer.valueOf(request.queryParams("id"));
                    deleteMovie(conn, identity);

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




    public static void insertMovie(Connection conn, Movie movie) throws SQLException
    {
        String actorsStr = "";
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO movies VALUES(NULL,?,?,?,?,?,?,?)");
        stmt.setString(1,movie.title);
        for (int i =0;i<movie.actors.size();i++)
        {
            if (i == movie.actors.size()-1)
            {
                actorsStr = String.format("%s%s",actorsStr, movie.actors.get(i));
            }
            else
            {
                actorsStr = String.format("%s%s|",actorsStr, movie.actors.get(i));
            }
        }
        stmt.setString(2,actorsStr);
        stmt.setString(3,movie.director);
        stmt.setInt(4,movie.minutesRuntime);
        stmt.setInt(5,movie.releaseYear);
        stmt.setInt(6,movie.rating);
        stmt.setInt(7,movie.userId);
        stmt.execute();
    }

    public static Movie selectMovie(Connection conn, int id) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM movies JOIN users ON movies.user_id = users.id WHERE movies.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next())
        {
            String title = results.getString("movies.title");
            String actors = results.getString("movies.actors");//pipe separated values are changed to an arraylist in the constructor
            String director = results.getString("movies.director");
            int minutesRuntime = results.getInt("movies.minutes_runtime");
            int year = results.getInt("movies.release_year");
            int rating = results.getInt("movies.rating");
            int userId = results.getInt("users.id");
            return new Movie(id, title, actors, director, minutesRuntime, year, rating, userId);
        }
        return null;
    }

    public static ArrayList<Movie> selectMovies(Connection conn) throws SQLException
    {
        ArrayList<Movie> movies=new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM movies INNER JOIN users ON movies.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        while (results.next())
        {
            int id = results.getInt("id");
            String title = results.getString("movies.title");
            String actors = results.getString("movies.actors");//pipe separation values are taken care of in the constructor
            String director = results.getString("movies.director");
            int minutesRuntime = results.getInt("movies.minutes_runtime");
            int year = results.getInt("movies.release_year");
            int rating = results.getInt("movies.rating");
            int userId = results.getInt("users.id");

            Movie movie = new Movie(id, title, actors, director, minutesRuntime, year, rating, userId);
            movies.add(movie);
        }
        return movies;
    }

    public static ArrayList<Movie> selectMoviesByUser(Connection conn, int user_id) throws SQLException
    {
        ArrayList<Movie> movies=null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM movies INNER JOIN users ON movies.user_id = users.id WHERE users.id = ?");
        ResultSet results = stmt.executeQuery();
        while (results.next())
        {
            int id = results.getInt("id");
            String title = results.getString("movies.title");
            String actors = results.getString("movies.actors");//pipe separation values are taken care of in the constructor
            String director = results.getString("movies.director");
            int minutesRuntime = results.getInt("movies.minutes_runtime");
            int year = results.getInt("movies.release_year");
            int rating = results.getInt("movies.rating");
            int userId = results.getInt("users.id");

            Movie movie = new Movie(id, title, actors, director, minutesRuntime, year, rating, userId);
            movies.add(movie);
        }
        return movies;
    }
    //id IDENTITY, title VARCHAR, actors VARCHAR, director VARCHAR, minutes_runtime INT, release_year INT, rating INT, user_id INT

    public static void updateMovie(Connection conn, Movie movie) throws SQLException
    {
        String actorsStr = "";
        PreparedStatement stmt = conn.prepareStatement("UPDATE movies SET title = ?, actors = ?, director = ?, minutes_runtime = ?, release_year = ?, rating = ? WHERE id = ?");
        stmt.setString(1,movie.title);
        for (int i =0;i<movie.actors.size();i++)
        {
            if (i == movie.actors.size()-1)
            {
                actorsStr = String.format("%s%s",actorsStr, movie.actors.get(i));
            }
            else
            {
                actorsStr = String.format("%s%s|",actorsStr, movie.actors.get(i));
            }
        }
        stmt.setString(2,actorsStr);
        stmt.setString(3,movie.director);
        stmt.setInt(4,movie.minutesRuntime);
        stmt.setInt(5,movie.releaseYear);
        stmt.setInt(6,movie.rating);
        stmt.setInt(7,movie.id);
        stmt.execute();
    }

    public static void deleteMovie(Connection conn, int id) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM movies WHERE id = ?");
        stmt.setInt(1,id);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL,?,?)");
        stmt.setString(1,name);
        stmt.setString(2,password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next())
        {
            int id = results.getInt("id");
            name = results.getString("name");
            String password = results.getString("password");
            return new User(id,name,password);
        }
        return null;
    }

}
