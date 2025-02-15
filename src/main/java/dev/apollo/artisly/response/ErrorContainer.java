package dev.apollo.artisly.response;

import org.json.simple.JSONObject;

public class ErrorContainer {

    private String parameter;
    private String error;

    public ErrorContainer(String parameter, String error) {
        this.parameter = parameter;
        this.error = error;
    }

    public String getParameter() {
        return parameter;
    }

    public String getError() {
        return error;
    }

    public JSONObject toJson()
    {
        JSONObject object = new JSONObject();
        object.put("error", error);
        object.put("parameter", parameter);
        return object;
    }
}
