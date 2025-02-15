package dev.apollo.artisly.models;



import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @param id                    The ID of the user
 * @param email                 The email of the user
 * @param password              The password of the user
 * @param firstName             The first name of the user
 * @param lastName              The last name of the user
 * @param phoneNumber           The phone number of the user
 * @param banned                Whether the user is banned or not
 * @param created               The date the user was created
 * @param updated               The date the user was last updated
 * @param emailVerified         The date the user verified their email, null if not verified yet
 * @param nomorKTP              The nomorKTP of the user
 */

public record User(UUID id, String email, String password, String firstName, String lastName, String phoneNumber, boolean banned, LocalDate created, LocalDate updated, Optional<LocalDate> emailVerified, Optional<String> nomorKTP, boolean admin) {

    public JSONObject toJSON()
    {
        JSONObject userJson = new JSONObject();
        userJson.put("id", id);
        userJson.put("email", email);
        userJson.put("first_name", firstName);
        userJson.put("last_name", lastName);
        userJson.put("phone_number", phoneNumber);
        userJson.put("banned", banned);
        userJson.put("created", created.toString());
        userJson.put("updated", updated.toString());
        userJson.put("email_verified", emailVerified.isPresent() ? true : false);
        userJson.put("nomor_ktp", nomorKTP.orElse("null"));
        userJson.put("admin", admin);
        //userJson.put("interests", serializeTags(interestedIns));
        return userJson;
    }

    public JSONObject toJSONRestricted()
    {
        JSONObject userJson = new JSONObject();
        userJson.put("id", id);
        userJson.put("email", email);
        userJson.put("first_name", firstName);
        userJson.put("last_name", lastName);
        userJson.put("banned", banned);
        userJson.put("created", created.toString());
        userJson.put("updated", updated.toString());
        userJson.put("email_verified", emailVerified.isPresent() ? true : false);
        userJson.put("admin", admin);
        //userJson.put("interests", serializeTags(interestedIns));
        return userJson;
    }

    public String serialize() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(id.toString()).append(",")
                    .append(URLEncoder.encode(email, StandardCharsets.UTF_8.toString())).append(",")
                    .append(URLEncoder.encode(password, StandardCharsets.UTF_8.toString())).append(",")
                    .append(URLEncoder.encode(firstName, StandardCharsets.UTF_8.toString())).append(",")
                    .append(URLEncoder.encode(lastName, StandardCharsets.UTF_8.toString())).append(",")
                    .append(URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8.toString())).append(",")
                    .append(banned).append(",")
                    .append(created.toString()).append(",")
                    .append(updated.toString()).append(",")
                    .append(URLEncoder.encode(emailVerified.map(LocalDate::toString).orElse(""), StandardCharsets.UTF_8.toString())).append(",")
                    .append(URLEncoder.encode(nomorKTP.orElse(""), StandardCharsets.UTF_8.toString())).append(",")
                    .append(admin);
            //      .append(serializeTags(interestedIns));
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    public static User deserialize(String serializedUser) {
        try {
            String[] userParts = serializedUser.split(",");
            return new User(UUID.fromString(userParts[0]),
                    URLDecoder.decode(userParts[1], StandardCharsets.UTF_8.toString()),
                    URLDecoder.decode(userParts[2], StandardCharsets.UTF_8.toString()),
                    URLDecoder.decode(userParts[3], StandardCharsets.UTF_8.toString()),
                    URLDecoder.decode(userParts[4], StandardCharsets.UTF_8.toString()),
                    URLDecoder.decode(userParts[5], StandardCharsets.UTF_8.toString()),
                    Boolean.parseBoolean(userParts[6]),
                    LocalDate.parse(userParts[7]),
                    LocalDate.parse(userParts[8]),
                    userParts[9].equals("") ? Optional.empty() : Optional.of(LocalDate.parse(URLDecoder.decode(userParts[9], StandardCharsets.UTF_8.toString()))),
                    userParts[10].equals("") ? Optional.empty() : Optional.of(URLDecoder.decode(userParts[10], StandardCharsets.UTF_8.toString())),
                    Boolean.parseBoolean(userParts[11]));
            //      deserializeTags(userParts[12]));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    public List<String> deserializeTags(String string)
    {
        return List.of(string.split(","));
    }

    public String serializeTags(List<String> tags)
    {
        return String.join(",", tags);
    }

    /*
    public static List<String> interestedInTags()
    {
        return interestedIns;
    }

    /*

     */

}
