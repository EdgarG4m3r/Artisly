package dev.apollo.artisly;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.configuration.Configuration;
import dev.apollo.artisly.databases.MySQL;
import dev.apollo.artisly.databases.Redis;
import dev.apollo.artisly.external.SMTPClient;
import dev.apollo.artisly.managers.storage.ObjectStorage;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.routes.*;
import dev.apollo.artisly.services.MediaService;
import dev.apollo.artisly.session.SessionManager;
import dev.apollo.artisly.tasks.EmailSenderTask;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Artisly {

    public static Artisly instance;
    private static Logger logger = LoggerFactory.getLogger("Artisly");

    private Configuration configuration;
    private MySQL mySQL;
    private Redis redis;
    private SessionManager sessionManager;

    private Javalin webserver;

    private AuthHandler authHandler;
    private SMTPClient smtpClient;

    private MediaService mediaService;
    private ObjectStorage objectStorage;

    private ScheduledExecutorService emailSender = Executors.newSingleThreadScheduledExecutor();


    public static void main(String[] args) {
        if (instance == null)
        {
            instance = new Artisly();
            instance.start();
        }
        else
        {
            logger.error("Artisly is already running!");
        }
    }

    private void start() {
        logger.info("Starting Artisly...");
        loadConfig();
        loadMySQL();
        loadRedis();
        loadManagers();
        loadTasks();
        loadWebServer();
    }

    private void loadWebServer() {
        logger.info("Starting the API Server...");
        this.authHandler = new AuthHandler();
        this.webserver = Javalin.create(config -> {
            config.requestLogger.http((ctx, executionTimeMs) -> {
                logger.info(ctx.method() + " " + ctx.fullUrl() + " " + ctx.status() + " " + ctx.ip() + " " + executionTimeMs + "ms");
            });
        }).start(configuration.webserver_address, configuration.webserver_port);
        logger.info("Registering routes...");
        webserver.before(ctx -> {

            try
            {
                UUID userId = sessionManager.validateToken(ctx.header("X-Session-Token"));
                try (Jedis jedis = redis.getJedis().getResource()) {
                    String rateLimitKey = "rate_limit:" + userId;


                    if (!jedis.exists(rateLimitKey)) {
                        jedis.setex(rateLimitKey, 60, "1");
                    } else if (Integer.parseInt(jedis.get(rateLimitKey)) < 100) {
                        jedis.incr(rateLimitKey);
                    }
                    else {
                        StandarizedResponses.generalFailure(ctx, 429, "RATE_LIMIT", "You are rate-limited, Try again in a few minutes");
                    }
                }
            }
            catch (Exception e)
            {
                //ignore
            }

        });


        webserver.after(ctx -> {
            //CORS
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
            ctx.header("Access-Control-Allow-Credentials", "true");

            //no cache
            ctx.header("Cache-Control", "no-cache, no-store, must-revalidate");
            ctx.header("Pragma", "no-cache");
            ctx.header("Expires", "0");

        });

        RouteDELETE.registerRoute(webserver);
        RouteGET.registerRoute(webserver);
        RoutePATCH.registerRoute(webserver);
        RoutePOST.registerRoute(webserver);
        RouteDEFAULT.registerRoute(webserver);

        logger.info("API is now running on " + configuration.webserver_address + ":" + configuration.webserver_port);
    }

    private void loadConfig() {
        logger.info("Loading config... Default config (config.ini) will be created if not found.");
        configuration = new Configuration();
        logger.info("Config loaded!");
    }

    private void loadMySQL() {
        logger.info("Loading MySQL...");
        mySQL = new MySQL(
                configuration.mysql_address,
                String.valueOf(configuration.mysql_port),
                configuration.mysql_database,
                configuration.mysql_username,
                configuration.mysql_password,
                configuration.mysql_ssl
        );
        mySQL.connect();
        logger.info("MySQL loaded!");
    }

    private void loadTasks()
    {
        emailSender.scheduleAtFixedRate(new EmailSenderTask(), 0, 10, TimeUnit.SECONDS);
    }

    private void loadRedis() {
        logger.info("Loading Redis...");
        redis = new Redis(
                configuration.redis_address,
                configuration.redis_port,
                configuration.redis_username,
                configuration.redis_password
        );
        redis.connect();
        logger.info("Redis loaded!");
    }

    private void loadManagers() {
        logger.info("Loading managers...");
        sessionManager = new SessionManager(this);
        smtpClient = new SMTPClient(getConfiguration().email_host, getConfiguration().email_port, getConfiguration().email_username, getConfiguration().email_password);
        objectStorage = new ObjectStorage(configuration.s3_address, configuration.s3_access_key, configuration.s3_secret_key);
        mediaService = new MediaService(this);
        logger.info("Managers loaded!");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        Artisly.logger = logger;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public Redis getRedis() {
        return redis;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }


    public SMTPClient getSmtpClient() {
        return smtpClient;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public ObjectStorage getObjectStorage() {
        return objectStorage;
    }

}