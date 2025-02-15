package dev.apollo.artisly.authentication;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.UserSessionContainer;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.session.exception.InvalidTokenException;
import dev.apollo.artisly.session.exception.MissingTokenException;
import dev.apollo.artisly.session.exception.UnsignedTokenException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AuthHandler {

    /**
     * Validates and authenticates a user based on the provided token
     * @param ctx   The context of the request
     * @return      The {@link UserSessionContainer} object containing the user's session information
     */
    public static UserSessionContainer authenticateUser(Context ctx)
    {
        InputFilter.validateToken("X-Session-Token", ParamField.HEADER, ctx);
        if (ctx.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(ctx);
            return null;
        }
        String token = ctx.header("X-Session-Token");
        UUID userId;
        try
        {
            userId = Artisly.instance.getSessionManager().validateToken(token);
            try(Connection connection = Artisly.instance.getMySQL().getConnection())
            {
                Optional<User> userOptional = CRUDSUser.readById(connection, userId);
                if (!userOptional.isPresent())
                {
                    throw new UserNotFoundException("We couldn't find your account. Please try logging in again.");
                }
                UserSessionContainer container = new UserSessionContainer(token, userOptional.get());
                ctx.attribute("user", container);
                return container;
            }
        } catch (InvalidTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "INVALID_TOKEN", "The provided token is invalid");
        } catch (UnsignedTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "UNSIGNED_TOKEN", "The provided token is unsigned");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(ctx, 500, "INTERNAL_SERVER_ERROR", "An internal server error occurred");
        } catch (MissingTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "MISSING_TOKEN", "No token was provided");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(ctx, 401, "USER_NOT_FOUND", "We couldn't find your account. Please try logging in again.");
        }
        return null;
    }

    /**
     * Validates and authenticates an admin based on the provided token
     * @param ctx   The context of the request
     * @return      The {@link UserSessionContainer} object containing the user's session information
     */
    public static UserSessionContainer authenticateAdmin(Context ctx)
    {
        InputFilter.validateToken("X-Session-Token", ParamField.HEADER, ctx);
        if (ctx.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(ctx);
            return null;
        }
        String token = ctx.header("X-Session-Token");
        UUID userId;
        try
        {
            userId = Artisly.instance.getSessionManager().validateToken(token);
            try(Connection connection = Artisly.instance.getMySQL().getConnection())
            {
                Optional<User> userOptional = CRUDSUser.readById(connection, userId);
                if (!userOptional.isPresent())
                {
                    throw new UserNotFoundException("We couldn't find your account. Please try logging in again.");
                }
                if (!userOptional.get().admin())
                {
                    // The user exists, but they are not an admin
                    StandarizedResponses.generalFailure(ctx, 401, "NOT_ADMIN", "Anda tidak memilik akses ke sumber daya ini.");
                    return null;
                }
                UserSessionContainer container = new UserSessionContainer(token, userOptional.get());
                ctx.attribute("user", container);
                return container;
            }
        } catch (InvalidTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "INVALID_TOKEN", "The provided token is invalid");
        } catch (UnsignedTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "UNSIGNED_TOKEN", "The provided token is unsigned");
        } catch (SQLException e) {
            e.printStackTrace();
            StandarizedResponses.generalFailure(ctx, 500, "INTERNAL_SERVER_ERROR", "An internal server error occurred");
        } catch (MissingTokenException e) {
            StandarizedResponses.generalFailure(ctx, 401, "MISSING_TOKEN", "No token was provided");
        } catch (UserNotFoundException e) {
            StandarizedResponses.generalFailure(ctx, 401, "USER_NOT_FOUND", "We couldn't find your account. Please try logging in again.");
        }
        return null;
    }

    //No longer used, we relied on the reverse proxy to provide the real IP
    public String restoreIp(Context context)
    {
        if (context.header("CF-Connecting-IP") != null)
        {
            return context.header("CF-Connecting-IP");
        }
        else if (context.header("X-Real-IP") != null)
        {
            return context.header("X-Real-IP");
        }
        else if (context.header("X-Forwarded-For") != null)
        {
            return context.header("X-Forwarded-For");
        }
        else
        {
            return context.ip();
        }
    }


}
