package dev.apollo.artisly.configuration;

import dev.apollo.artisly.Artisly;
import org.ini4j.Ini;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

    public String webserver_address;
    public int webserver_port;
    public int webserver_timeout;
    public int webserver_rate_limit;
    public String webserver_app_name;
    public String webserver_http_server_name;
    public String webserver_host;

    public String mysql_address;
    public int mysql_port;
    public String mysql_database;
    public String mysql_username;
    public String mysql_password;
    public boolean mysql_ssl;

    public String redis_address;
    public int redis_port;
    public String redis_username;
    public String redis_password;

    public String session_pepper;

    public String email_host;
    public int email_port;
    public String email_username;
    public String email_password;

    public String s3_address;
    public String s3_access_key;
    public String s3_secret_key;


    //init
    public Configuration() {

        Artisly.getLogger().info("Initializing configuration...");

        copy(getClass().getResourceAsStream("/config.ini"), System.getProperty("user.dir")+"/config.ini");

        File configFile = new File(System.getProperty("user.dir")+"/config.ini");
        try
        {
            if(!configFile.exists())
            {
                throw new NullPointerException("Config file not found!");
            }
        }
        catch (NullPointerException e)
        {
            Artisly.getLogger().error("An error occurred while loading config! Terminating...");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            Ini ini = new Ini(configFile);
            webserver_address = ini.get("webserver", "address");
            webserver_port = Integer.parseInt(ini.get("webserver", "port"));
            webserver_timeout = Integer.parseInt(ini.get("webserver", "timeout"));
            webserver_rate_limit = Integer.parseInt(ini.get("webserver", "rate_limit"));
            webserver_app_name = ini.get("webserver", "app_name");
            webserver_http_server_name = ini.get("webserver", "http_server_name");
            webserver_host = ini.get("webserver", "host");


            mysql_address = ini.get("mysql", "address");
            mysql_port = Integer.parseInt(ini.get("mysql", "port"));
            mysql_database = ini.get("mysql", "database");
            mysql_username = ini.get("mysql", "username");
            mysql_password = ini.get("mysql", "password");
            mysql_ssl = Boolean.parseBoolean(ini.get("mysql", "ssl"));

            redis_address = ini.get("redis", "address");
            redis_port = Integer.parseInt(ini.get("redis", "port"));
            redis_username = ini.get("redis", "username");
            redis_password = ini.get("redis", "password");

            session_pepper = ini.get("session", "pepper");

            email_host = ini.get("email", "host");
            email_port = Integer.parseInt(ini.get("email", "port"));
            email_username = ini.get("email", "username");
            email_password = ini.get("email", "password");

            s3_address = ini.get("s3", "address");
            s3_access_key = ini.get("s3", "access_key");
            s3_secret_key = ini.get("s3", "secret_key");


        }
        catch (Exception e)
        {
            Artisly.getLogger().error("An error occurred while loading config! Terminating...");
            e.printStackTrace();
            System.exit(1);
        }

        Artisly.getLogger().info("Configuration initialized!");
        Artisly.setLogger(LoggerFactory.getLogger(webserver_app_name));

    }

    private boolean copy(InputStream source , String destination) {
        boolean success = true;

        if (new File(destination).exists()) {
            return false;
        }

        Artisly.getLogger().info("Copying default config to " + destination);

        try {
            Files.copy(source, Paths.get(destination));
        } catch (IOException ex) {
            Artisly.getLogger().error("Failed to copy file" + source + " to " + destination);
            success = false;
        }

        return success;
    }


}
