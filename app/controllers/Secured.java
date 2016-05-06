package controllers;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        System.out.println("Get User name from Secured: "+ctx.session().get("username"));
        return ctx.session().get("username");
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        System.out.println("User not authorised");
        return redirect(routes.Application.index());
    }
}
