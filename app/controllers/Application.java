package controllers;

import controllers.dao.ChamgosuDatabase;
import models.DeviceInfo;
import models.Response;
import play.Logger;
import play.i18n.Messages;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import static play.data.Form.form;

public class Application extends Controller {


    private static DeviceInfo deviceInfo = null;

    public Application(){
}

    @Inject
    ChamgosuDatabase chamgosuDatabase;

    public static Result GO_HOME = redirect(
            routes.Application.index()
    );


    public static Result GO_DASHBOARD = redirect(
            routes.Dashboard.listTokens(0, "username", "asc", "")
    );

    public Result index() {
        // Check that the email matches a confirmed user before we redirect
        String username = ctx().session().get("username");
        if ( username != null) {
            System.out.println("username is: "+  username);
            ChamgosuDatabase.Role role = chamgosuDatabase.checkLogin(username);
            if(role.equals(ChamgosuDatabase.Role.ADMIN)){
                System.out.println("just before routing to dashboard: "+role );
                routes.Dashboard.listTokens(0, "username", "asc", "");
            }
            else {
                Logger.debug("Clearing invalid session credentials");
                session().clear();
            }

        }

        return ok(index.render(form(Login.class)));
    }

    public static class Login{
        @Constraints.Required
        public String username;
        @Constraints.Required
        public String password;
    }

    public static class CreateTokenRequest{

        @Constraints.Required
        @Constraints.MinLength(2)
        public String username;

        @Constraints.Required
        public String password;

        @Constraints.Required
        @Constraints.MinLength(2)
        public String type;

        @Constraints.Required
        @Constraints.MinLength(2)
        public String role;

        @Constraints.MaxLength(200)
        @Constraints.MinLength(64)
        @Constraints.Required
        public String token;

    }

    public Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(index.render(loginForm));
        }
        Application.Login login = loginForm.get();
        ChamgosuDatabase.Role role = chamgosuDatabase.checkLogin(login.username, login.password);
        if(role.equals(ChamgosuDatabase.Role.ADMIN)){
            flash("success", Messages.get("signin.success"));
            System.out.println("user logged in: " + login.username);
            session("username", loginForm.get().username);
            return GO_DASHBOARD;
        }
        else{
            flash("failure", Messages.get("signin.failure"));
            //return GO_HOME;
            return forbidden(index.render(loginForm));

        }
    }
    /**
     * Add the content-type json to response
     *
     * @param httpResponse httpResponse
     *
     * @return Result
     */
    public static Result jsonResult(Result httpResponse) {
        response().setContentType("application/json; charset=utf-8");
        return httpResponse;
    }


    public Result createDeviceToken(){
        Form<Application.CreateTokenRequest> createDeviceTokenForm = form(Application.CreateTokenRequest.class).bindFromRequest();
        if (createDeviceTokenForm.hasErrors()) {
            return getResultAsJsonResponse("Failure",null,createDeviceTokenForm.errorsAsJson());
        }
        Application.CreateTokenRequest createTokenRequest = createDeviceTokenForm.get();
        ChamgosuDatabase.Role role = chamgosuDatabase.checkLogin(createTokenRequest.username,createTokenRequest.password);
            if(role.equals(ChamgosuDatabase.Role.UNKNOWN)){
                return getResultAsJsonResponse("Failure",null,"incorrect user name or password");
            }
        deviceInfo = getByDeviceToken(createTokenRequest.token);
        deviceInfo.deviceToken = createTokenRequest.token;
        deviceInfo.deviceType = createTokenRequest.type;
        deviceInfo.username = createTokenRequest.username;
        deviceInfo.password = createTokenRequest.password;
        deviceInfo.role = role.name();//createTokenRequest.role;
        deviceInfo.save();
        return getResultAsJsonResponse("success", deviceInfo, "device registration successful");
    }

    private static Result getResultAsJsonResponse(String status, Object data, Object message) {
        return Application.jsonResult(ok(play.libs.Json.toJson(Response.responseBuilder.aresponse().
                withStatus(Messages.get(status)).
                withData(data).
                withMessage(message).build())));
    }

    private static DeviceInfo getByDeviceToken(String device_token) {
        deviceInfo = DeviceInfo.findByDeviceToken(device_token);
        if(null == deviceInfo){
            deviceInfo = new DeviceInfo();
        }
        return deviceInfo;
    }

    public Result listTokens(int page, String sortBy, String order, String filter){
    return null;
    }

}
