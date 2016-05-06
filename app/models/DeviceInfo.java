package models;


import com.avaje.ebean.*;
import controllers.Dashboard;
import play.libs.F;
import play.mvc.QueryStringBindable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The persistent class for the device_info database table.
 */
@Entity
@Table(name = "device_info")
public class DeviceInfo extends Model {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    public int id;

    @Column(name = "device_token", nullable = false, length = 200)
    public String deviceToken;

    @Column(name = "device_type", nullable = false, length = 10)
    public String deviceType;

    @Column(length = 100)
    public String username;

    @Column(name = "phone_number", nullable = false)
    public String password;

    @Column(nullable = false, length = 20)
    public String role;

    public DeviceInfo() {
    }

    public static Model.Finder<Long, DeviceInfo> find = new Model.Finder<Long, DeviceInfo>(DeviceInfo.class);

    public static DeviceInfo findByDeviceToken(String deviceToken) {
        return find.where().eq("deviceToken", deviceToken).findUnique();
    }

    public static class DeviceTypeToken implements QueryStringBindable<DeviceTypeToken>, Serializable {
        public String token = "";
        public String type = "";

        public DeviceTypeToken() {
        }

        public DeviceTypeToken(String type, String token) {
            this.type = type;
            this.token = token;
        }

        @Override
        public F.Option<DeviceTypeToken> bind(String key, Map<String, String[]> data) {
            if (data.containsKey(key + ".type") && data.containsKey(key + ".token")) {
                type = data.get(key + ".type")[0];
                token = data.get(key + ".token")[0];
                return F.Option.Some(this);
            } else {
                return F.Option.None();
            }
        }

        @Override
        public String unbind(String key) {
            return key + ".type=" + type + "&" + key + ".token=" + token;
        }

        @Override
        public String javascriptUnbind() {
            return "function(k,v) {\n" +
                    "    return encodeURIComponent(k+'.type')+'='+v.type+'&'+encodeURIComponent(k+'.token')+'='+v.token;\n" +
                    "}";
        }
    }

    public static Map<DeviceTypeToken, Boolean> makeTokenMap(Dashboard.DeviceTokenForm tokenForm) {
        Map<DeviceTypeToken, Boolean> tokenMap = new HashMap<DeviceTypeToken, Boolean>();
        for (DeviceInfo deviceInfo : find.all()) {
            tokenMap.put(new DeviceTypeToken(deviceInfo.deviceType, deviceInfo.deviceToken), (tokenForm != null && tokenForm.device_tokens.contains(deviceInfo.deviceToken)));
        }
        return tokenMap;
    }


    public static PagedList<DeviceInfo> page(int page, int pageSize, String sortBy, String order, String filter) {
        //if(page==0){page=page+1;}
        return find.where().ilike("device_type", "%" + filter + "%")
                .orderBy(sortBy + " " + order)
                .findPagedList((page), pageSize);
    }
}