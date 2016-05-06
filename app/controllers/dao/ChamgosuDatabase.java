package controllers.dao;

import play.db.DB;
import play.mvc.Controller;
import javax.inject.Inject;

import play.mvc.Controller;
import play.db.NamedDatabase;
import play.db.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by 609108084 on 30/04/2016.
 */
public class ChamgosuDatabase extends Controller{

    public enum Role{
        ADMIN,MEMBER,UNKNOWN
    }

    @Inject
    @NamedDatabase("chamgosu")
    Database db;

    public Role checkLogin(String user){
        Role role = Role.UNKNOWN;
        Connection connection = db.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select id from %s where id='%s'";
        try{
            stmt = connection.createStatement();
            String WIZ_ADMIN = "wiz_admin";
            System.out.println(String.format(sql, WIZ_ADMIN, user));
            rs = stmt.executeQuery(String.format(sql, WIZ_ADMIN, user));
            while (rs.next()) {
                String id = rs.getString("id");
                if ((null != id && !id.isEmpty())) {
                    role = Role.ADMIN;
                    break;
                }
            }
        }
        catch(SQLException se){
            se.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return role;
    }

    public Role checkLogin(String user, String password){
        boolean flag = false;
        Role role = Role.UNKNOWN;
        Connection connection = db.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        int update = 0;
        try {
            stmt = connection.createStatement();
            String sql = "select id,passwd from %s where id='%s' and passwd='%s'";
            String updateMemberVisitSql = "update %s set visit = visit+1 , visit_time = now() where id='%s'";
            String updateAdminVisitSql = "update %s set last = now() where id = '%s'";
            String WIZ_MEMBER = "wiz_member";
            System.out.println(String.format(sql, WIZ_MEMBER, user, password));
            rs = stmt.executeQuery(String.format(sql, WIZ_MEMBER, user, password));
            while (rs.next()) {
                String id = rs.getString("id");
                String pass = rs.getString("passwd");
                if ((null != id && !id.isEmpty()) && (null != pass && !pass.isEmpty())) {
                    flag = true;
                    role = Role.MEMBER;
                    System.out.println(String.format(updateMemberVisitSql, WIZ_MEMBER, user));
                    update = stmt.executeUpdate(String.format(updateMemberVisitSql, WIZ_MEMBER, user));
                    System.out.println(String.format("visit count for: %s updated in %s. number of rows updated are: %d",user,WIZ_MEMBER,update));
                    break;
                }
            }
            if (!flag) {
                String WIZ_ADMIN = "wiz_admin";
                System.out.println(String.format(sql, WIZ_ADMIN, user, password));
                rs = stmt.executeQuery(String.format(sql, WIZ_ADMIN, user, password));
                while (rs.next()) {
                    String id = rs.getString("id");
                    String pass = rs.getString("passwd");
                    if ((null != id && !id.isEmpty()) && (null != pass && !pass.isEmpty())) {
                        flag = true;
                        role = Role.ADMIN;
                        System.out.println(String.format(updateAdminVisitSql, WIZ_ADMIN, user));
                        update = stmt.executeUpdate(String.format(updateAdminVisitSql, WIZ_ADMIN, user));
                        System.out.println(String.format("visit count for: %s updated in %s. number of rows updated are: %d",user,WIZ_ADMIN,update));
                        break;
                    }
                }
            }
        }
        catch (SQLException se){
            se.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            try {
                rs.close();
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return role;
    }
}
