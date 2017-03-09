package com.theironyard.novauc;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void insertRestaurant(String restName, Boolean restTasty, int restNumWaiters) throws SQLException {
        //PreparedStatement pstmt = conn.prepareStatement("INSERT INTO restaurants VALUES (NULL, ?, ?, ?)");
        PreparedStatement ps = getConnection().prepareStatement
                ("INSERT INTO restaurants (restName, restTasty, restNumWaiters) VALUES (?, ?, ?)");
        User user = currentUser();
        ps.setString(1,restName);
        ps.setBoolean(2,restTasty);
        ps.setInt(3,restNumWaiters);
        ps.execute();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:./main");
    }

    public static void createTables() throws SQLException {
        Statement stated = getConnection().createStatement();
        stated.execute("CREATE TABLE IF NOT EXISTS restaurants (id IDENTITY, restName VARCHAR, restTasty BOOLEAN, restNumWaiters INT )");
        stated.execute("CREATE TABLE IF NOT EXISTS user (id IDENTITY , userName VARCHAR, password VARCHAR)");
    }

    public static User currentUser() {
        return new User(1, "billyray");
    }

    public static HashMap<String, User> accountInfo = new HashMap<>();
    public static ArrayList<Restaurant> restAL = new ArrayList<>();

    public static void main(String[] args) throws SQLException {

        Spark.init();
        Server.createWebServer().start();
        createTables();

        //TODO set restAL to accept all three html fields, not just the first

        Spark.get("/", ((request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");
            HashMap userActivity = new HashMap();
            if(!accountInfo.containsKey(name)) {
                return new ModelAndView(userActivity,"index.html");
                }
            else {
                  userActivity.put("entries", restAL);
                  userActivity.put("userName", name);
                  return new ModelAndView(userActivity, "index.html");
                  }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post("/login", (request, response) -> {
            String name = request.queryParams("userName");
            String password = request.queryParams("passwordLogin");
            Session session = request.session();

            if(accountInfo.containsKey(name)) {
                if(password.equals(accountInfo.get(name).getPassword())) {
                    session.attribute("userName", name);
                }
            }
            else {
                session.attribute("userName", name);
                accountInfo.put(name, new User(name, password));
            }
            response.redirect("/");
            return "";
        });

        Spark.post("/create-restaurant", ((request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");

            String restName = request.queryParams("restName");
            Boolean restTasty = Boolean.getBoolean(request.queryParams("restTasty"));
            int restNumWaiters = Integer.valueOf(request.queryParams("restNumWaiters"));

            Restaurant entryObj = new Restaurant(restName, restTasty, restNumWaiters);
            restAL.add(entryObj);

            System.out.println(entryObj.toString());

            insertRestaurant(restName, restTasty,restNumWaiters);

            response.redirect("/");
            return "";
        }));

        Spark.post("/edit-restaurant", (request, response) -> {
            String editor = request.queryParams("editMessageT");

            int edit = Integer.valueOf(request.queryParams("messID"));

            Restaurant entrance = null;
            for (Restaurant picker : restAL) {
                if (/*picker.getId()*/ 1 ==  edit) {
                    entrance = picker;
                    break;
                }
            }
            if (entrance != null && editor != null){
                //entrance.setText(editor);
            }
            response.redirect("/");
            return "";
        });

        /*
        Spark.get("/anotherplace/:id", ((request, response) -> {
                    String idJunk = request.params("id");
                    HashMap whatever = new HashMap();
                    whatever.put("id",idJunk);
                    return new ModelAndView(whatever, "anotherplace.html");
                }), new MustacheTemplateEngine()
        );

       */

        Spark.post("/delete-restaurant", (request, response) -> {
            int delete = Integer.valueOf(request.queryParams("messDel"));
            Restaurant entrance = new Restaurant();
            for (Restaurant picker : restAL) {
                if (/*picker.getId()*/ 1 ==  delete) {
                    entrance = picker;
                }
            }
            restAL.remove(entrance);
            response.redirect("/");
            return "";
        });

        Spark.post("/logout", ((request, response) -> {
            Session session = request.session();
            session.invalidate();
            response.redirect("/");
            return "";
        }));
    }
}

