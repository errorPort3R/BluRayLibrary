import com.theironyard.javawithclojure.jhporter.Main;
import com.theironyard.javawithclojure.jhporter.Movie;
import com.theironyard.javawithclojure.jhporter.User;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by jeffryporter on 6/15/16.
 */
public class MainTest
{


    Connection startConnection() throws SQLException
    {
        Connection conn = DriverManager.getConnection(("jdbc:h2:mem:test"));
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void TestUserInsertAndSelect() throws SQLException
    {

        Connection conn = startConnection();
        Main.insertUser(conn,"Alice", "12345");
        Main.insertUser(conn,"Bob", "54321");
        User userA = Main.selectUser(conn, "Alice");
        conn.close();
        assertTrue(userA.getPassword().equals("12345"));
    }

    @Test
    public void testMovieInsertSelectAndDelete() throws SQLException
    {
        Connection conn = startConnection();
        ArrayList<String> actors = new ArrayList<>();
        Main.insertUser(conn,"Alice", "12345");
        Main.insertUser(conn,"Bob", "54321");
        actors.add("Gene Hackman");
        actors.add("BobNewhart");
        actors.add("Arnold Schwarzenegger");
        Movie movie = new Movie("The Right-On Stuff",actors, "Michael Bay", 123,1978, 3, 1);
        Main.insertMovie(conn, movie);
        Movie newMovie = new Movie();
        newMovie =Main.selectMovie(conn, 1);
        System.out.println();
        //insert and select assertion
        assertTrue(newMovie.getTitle().equals(movie.getTitle()));

        Main.deleteMovie(conn, 1);
        Movie newerMovie = Main.selectMovie(conn, 1);
        conn.close();
        assertTrue(newerMovie == null);
    }

    @Test
    public void testSelectMovies() throws SQLException
    {
        Connection conn = startConnection();
        ArrayList<String> actors = new ArrayList<>();
        Main.insertUser(conn,"Alice", "12345");
        Main.insertUser(conn,"Bob", "54321");
        actors.add("Gene Hackman");
        actors.add("BobNewhart");
        actors.add("Arnold Schwarzenegger");
        Movie movie = new Movie("The Right-On Stuff",actors, "Michael Bay", 123,1978, 3, 1);
        Main.insertMovie(conn, movie);

        actors.add("Liv Tyler");
        actors.add("Grace Kelly");
        actors.add("Channing Tatum");
        Movie newMovie = new Movie("Finding Dory's Half coulsin, Earl",actors, "J.J. Abrams", 98,2016, 3, 2);
        Main.insertMovie(conn, newMovie);
        Movie newerMovie = new Movie();
        newerMovie = Main.selectMovie(conn, 1);
        Movie newererMovie = new Movie();
        newererMovie = Main.selectMovie(conn, 2);
        System.out.println();
        conn.close();
        //insert and select assertion
        assertTrue(newerMovie.getTitle().equals(movie.getTitle()));
        assertTrue(newMovie.getTitle().equals(newererMovie.getTitle()));

    }

    @Test
    public void testUpdateMovies() throws SQLException
    {
        Connection conn = startConnection();
        ArrayList<String> actors = new ArrayList<>();
        Main.insertUser(conn,"Alice", "12345");
        Main.insertUser(conn,"Bob", "54321");
        actors.add("Gene Hackman");
        actors.add("BobNewhart");
        actors.add("Arnold Schwarzenegger");
        Movie movie = new Movie("The Right-On Stuff",actors, "Michael Bay", 123,1978, 3, 1);
        Main.insertMovie(conn, movie);
        Movie firstMovie = Main.selectMovie(conn, 1);
        actors.add("Liv Tyler");
        actors.add("Grace Kelly");
        actors.add("Channing Tatum");
        Movie newMovie = new Movie("Finding Dory's Half cousin, Earl",actors, "J.J. Abrams", 98,2016, 3, 1);
        newMovie.setId(1);
        Main.updateMovie(conn, newMovie);
        Movie newerMovie = Main.selectMovie(conn, 1);

        conn.close();
        assertTrue(newerMovie.getTitle().equals("Finding Dory's Half cousin, Earl"));
        assertTrue(firstMovie.getTitle().equals("The Right-On Stuff"));

    }
}
