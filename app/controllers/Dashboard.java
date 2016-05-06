package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ReconnectPolicy;
import controllers.gcm.Notifier;
import controllers.gcm.exception.ConnectionException;
import controllers.gcm.exception.NoDeviceException;
import controllers.gcm.exception.NoServerApiKeyException;
import controllers.gcm.model.receive.HttpResponseMessage;
import controllers.gcm.model.receive.Message;
import models.DeviceInfo;
import models.PushNotification;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.listtokens;
import views.html.sendpush;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 609108084 on 5/1/2016.
 */
@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {

    public Dashboard() {
    }

    ApnsService service = APNS.newService()
            .withCert(Play.application().classloader().getResource("apns_Certificates.p12").getPath(), "1234")
            .withSandboxDestination()
            .withReconnectPolicy(ReconnectPolicy.Provided.EVERY_HALF_HOUR).asPool(15)
            .build();
    ;

    public static Result GO_HOME = redirect(
            routes.Dashboard.listTokens(0, "device_type", "asc", "")
    );

    public Result listTokens(int page, String sortBy, String order, String filter) {
        Form<Dashboard.DeviceTokenForm> deviceTokenFormForm = Form.form(DeviceTokenForm.class);
        return ok(
                listtokens.render(
                        DeviceInfo.page(page, 10, sortBy, order, filter),
                        sortBy, order, filter, deviceTokenFormForm, DeviceInfo.makeTokenMap(new DeviceTokenForm())
                )
        );
    }

    public static class DeviceTokenForm {
        public List<String> device_tokens = new ArrayList<>();

        public DeviceTokenForm() {
        }
    }

    public Result createPush() {
        Form<PushNotification> notificationForm = Form.form(PushNotification.class);
        Form<Dashboard.DeviceTokenForm> deviceTokenFormForm = Form.form(DeviceTokenForm.class).bindFromRequest();
        return ok(sendpush.render(notificationForm, deviceTokenFormForm));
    }

    public Result sendPush(List<String> tokens) {
        Form<PushNotification> notificationForm = Form.form(PushNotification.class).bindFromRequest();
        Form<Dashboard.DeviceTokenForm> deviceTokenFormForm = Form.form(DeviceTokenForm.class).bindFromRequest();
        for (String token : tokens) {
            JsonNode json = Json.parse(token);
            DeviceInfo.DeviceTypeToken deviceTypeToken = Json.fromJson(json, DeviceInfo.DeviceTypeToken.class);
            System.out.println("values are: " + deviceTypeToken.type + " " + deviceTypeToken.token);
        }
        if (notificationForm.hasErrors()) {
            return badRequest(sendpush.render(notificationForm, deviceTokenFormForm));
        }
        String payload = APNS.newPayload().alertBody(notificationForm.get().message).build();
        for (String token : tokens) {
            JsonNode json = Json.parse(token);
            DeviceInfo.DeviceTypeToken deviceTypeToken = Json.fromJson(json, DeviceInfo.DeviceTypeToken.class);
            switch (deviceTypeToken.type.toLowerCase()) {
                case "android":
                    System.out.println("this is android: " + deviceTypeToken.token);
                    ArrayList<String> devices = new ArrayList<>();
                    devices.add(deviceTypeToken.token);
                    try {
                        HttpResponseMessage response = Notifier.sendGCMMessage("AIzaSyBcQmnyt4GPT_iiDoYAFcRLvzC4kF6ci7c", devices, "", notificationForm.get().message, null, null);
                        for (Message m : response.getResults()) {
                            System.out.println("The error is: " + m.getError() + " " + m.getMessage_id());
                        }
                    } catch (NoDeviceException e) {
                        e.printStackTrace();
                    } catch (NoServerApiKeyException e) {
                        e.printStackTrace();
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                    break;
                case "ios":
                    service.push(deviceTypeToken.token, payload);
                    break;
                default:
                    System.out.println(deviceTypeToken.type + " is not matching to android or ios");
            }
        }
        flash("success", notificationForm.get().message + " Message sent");
        return GO_HOME;
    }
}
