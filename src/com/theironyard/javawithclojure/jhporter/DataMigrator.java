//package com.theironyard.javawithclojure.jhporter;
//
//import org.h2.tools.Server;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.Scanner;
//
///**
// * Created by jeffryporter on 6/14/16.
// */
//
//
//public class DataMigrator
//{
//
//    static ArrayList<Movie> movieArchive;
//
//    public static void main(String[] args) throws SQLException
//    {
//
//        Server.createWebServer().start();
//        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
//        Statement stmt = conn.createStatement();
//        stmt.execute("DROP TABLE movies");
//        stmt.execute("CREATE TABLE IF NOT EXISTS movies (id IDENTITY, title VARCHAR, actors VARCHAR, director VARCHAR, minutes_runtime INT, release_year INT, rating INT, user_id INT)");
//        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
//
//
//        ArrayList<User> users = new ArrayList<>();
//        loadMovieArchive("movies.txt");
//
//        for (Movie movie : movieArchive)
//        {
//            insertMovie(conn ,movie);
//        }
//
//
//    }
//
//    public static void loadMovieArchive(String fileLoc)
//    {
//        movieArchive = new ArrayList<>();
//        ArrayList<String> actors;
//        File f = new File(fileLoc);
//        Scanner fileScanner;
//        try
//        {
//            fileScanner = new Scanner(f);
//
//            while (fileScanner.hasNext())
//            {
//                actors = new ArrayList<>();
//                String line = fileScanner.nextLine();
//                String[] fields = line.split("\\|");
//                for (int i = 6; i < fields.length; i++)
//                {
//                    actors.add(fields[i]);
//                }
//                Movie movie = new Movie(fields[4], actors, fields[3], Integer.valueOf(fields[2]), Integer.valueOf(fields[1]), Integer.valueOf(fields[0]), Integer.valueOf(fields[5]));
//                movieArchive.add(movie);
//            }
//            fileScanner.close();
//        } catch (FileNotFoundException e)
//        {
//
//        }
//    }
//
//    public static void insertMovie(Connection conn, Movie movie) throws SQLException
//    {
//        String actorsStr = "";
//        PreparedStatement stmt = conn.prepareStatement("INSERT INTO movies VALUES(NULL,?,?,?,?,?,?,?)");
//        stmt.setString(1,movie.title);
//        for (int i =0;i<movie.actors.size();i++)
//        {
//            if (i == movie.actors.size()-1)
//            {
//                actorsStr = String.format("%s%s",actorsStr, movie.actors.get(i));
//            }
//            else
//            {
//                actorsStr = String.format("%s%s|",actorsStr, movie.actors.get(i));
//            }
//        }
//        stmt.setString(2,actorsStr);
//        stmt.setString(3,movie.director);
//        stmt.setInt(4,movie.minutesRuntime);
//        stmt.setInt(5,movie.releaseYear);
//        stmt.setInt(6,movie.rating);
//        stmt.setInt(7,movie.userId);
//        stmt.execute();
//    }
//}
//
//    //saveOrder: rating, releaseYear, minutesRunTime, director, title, owner, actor, actor, actor....
//    //Constructor Movie(String title, ArrayList<String> actors, String director, int minutesRuntime, int releaseYear, int rating, String owner)
//
////    public static void saveMovieArchive(String fileLoc)
////    {
////        try
////        {
////            PrintWriter output = new PrintWriter(fileLoc);
////            for (Movie m : movieArchive)
////            {
////                output.printf("%d|%d|%d|%s|%s|%s|", m.rating, m.releaseYear, m.minutesRuntime, m.director,m.title,m.owner);
////                for(int i = 0; i<m.actors.size();i++)
////                {
////                    if (i==(m.actors.size()-1))
////                    {
////                        output.printf("%s\n", m.actors.get(i));
////                    }
////                    else
////                    {
////                        output.printf("%s|", m.actors.get(i));
////                    }
////                }
////            }
////            output.close();
////        }
////        catch(Exception e)
////        {
////            e.printStackTrace();
////        }
////    }
//
////    public static void loadUsers(String fileLoc)
////    {
////        users = new HashMap<>();
////        ArrayList<String> actors;
////        File f = new File(fileLoc);
////        Scanner fileScanner = null;
////        try
////        {
////            fileScanner = new Scanner(f);
////
////            fileScanner.nextLine();
////            while (fileScanner.hasNext())
////            {
////                String line = fileScanner.nextLine();
////                String[] fields = line.split("\\|");
////                users.put(fields[0],fields[1]);
////                userMap.put(fields[0],new User(fields[0]));
////            }
////        }
////        catch (FileNotFoundException e)
////        {
////
////        }
////    }
////
////    public static void saveUsers(String fileLoc)
////    {
////        try {
////            PrintWriter output = new PrintWriter(fileLoc);
////
////            Iterator iterator = users.keySet().iterator();
////            while(iterator.hasNext())
////            {
////                String key = (String) iterator.next();
////                output.printf("%s|%s\n", key, users.get(key));
////            }
////            output.close();
////        }
////        catch (Exception e)
////        {
////
////        }
////    }
////
////    public static int findMovie(int identity)
////    {
////        int location= -1;
////        for(int i=0;i<movieArchive.size();i++)
////        {
////            if(movieArchive.get(i).id == identity)
////            {
////                location = i;
////                i=movieArchive.size();
////            }
////        }
////        return location;
////    }
////}
