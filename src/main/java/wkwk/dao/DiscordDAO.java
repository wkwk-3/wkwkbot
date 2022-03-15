package wkwk.dao;

import wkwk.ChannelList;
import wkwk.MentionList;
import wkwk.ReactionRoleRecord;
import wkwk.ServerDataList;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.paramater.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

//───────────────────────────────────────────────────────────
public class DiscordDAO extends DAOBase {


    public String BotGetToken() throws DatabaseException, SystemException {
        this.open();
        String token = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT " + BotParameters.DISCORD_TOKEN + " FROM " + DAOParameters.DATABASE_DATA;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                token = rs.getString(BotParameters.DISCORD_TOKEN);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return token;
    }

    public String BotGetPrefix(String serverid) throws DatabaseException, SystemException, IOException {
        this.open();
        String prefix = null;
        prestmt = null;
        try {
            String sql = "SELECT " + BotParameters.DISCORD_PREFIX + " FROM " + DAOParameters.DATABASE_TEMP + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                prefix = rs.getString(BotParameters.DISCORD_PREFIX);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return prefix;
    }

    public ServerDataList TempGetData(String serverid) throws SystemException, DatabaseException {
        this.open();
        prestmt = null;
        ServerDataList dataList = new ServerDataList();
        try {
            String sql = "SELECT * FROM " + DAOParameters.DATABASE_TEMP + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                dataList.setServer(serverid);
                dataList.setPrefix(rs.getString(BotParameters.DISCORD_PREFIX));
                dataList.setMentioncal(rs.getString(TempParameters.TEMP_MENTION));
                dataList.setFstchannel(rs.getString(TempParameters.TEMP_FIRST));
                dataList.setVoicecate(rs.getString(TempParameters.TEMP_VOICE));
                dataList.setTextcate(rs.getString(TempParameters.TEMP_TEXT));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return dataList;
    }

    public void BotSetDate(String idx, String select, String value) throws SystemException, DatabaseException {
        this.open();
        prestmt = null;
        String sql = null;
        try {
            switch (idx) {
                case "v":
                    sql = "UPDATE " + DAOParameters.DATABASE_TEMP + " SET " + TempParameters.TEMP_VOICE + " = ? WHERE " + TempParameters.TEMP_SERVER + " = ?";
                    break;
                case "t":
                    sql = "UPDATE " + DAOParameters.DATABASE_TEMP + " SET " + TempParameters.TEMP_TEXT + " = ? WHERE " + TempParameters.TEMP_SERVER + " = ?";
                    break;
                case "f":
                    sql = "UPDATE " + DAOParameters.DATABASE_TEMP + " SET " + TempParameters.TEMP_FIRST + " = ? WHERE " + TempParameters.TEMP_SERVER + " = ?";
                    break;
                case "p":
                    sql = "UPDATE " + DAOParameters.DATABASE_TEMP + " SET " + BotParameters.DISCORD_PREFIX + " = ? WHERE " + TempParameters.TEMP_SERVER + " = ?";
                    break;
            }
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, value);
            prestmt.setString(2, select);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void TempDataUpData(ServerDataList data) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "UPDATE " + DAOParameters.DATABASE_TEMP + " SET " + TempParameters.TEMP_FIRST + " = ?," + TempParameters.TEMP_TEXT + " = ?," + TempParameters.TEMP_VOICE + " = ?," + TempParameters.TEMP_MENTION + " = ? WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, data.getFstchannel());
            prestmt.setString(2, data.getTextcate());
            prestmt.setString(3, data.getVoicecate());
            prestmt.setString(4, data.getMentioncal());
            prestmt.setString(5, data.getServer());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void TempNewServer(String Server) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.DATABASE_TEMP + " (" + TempParameters.TEMP_SERVER + "," + BotParameters.DISCORD_PREFIX + ") VALUES(?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.setString(2, TempParameters.TEMP_PREFIX);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void TempDeleteData(String Server) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.DATABASE_TEMP + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.DATABASE_MEN + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.DATABASE_ROLE + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void TempSetChannelList(ChannelList list) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.DATABASE_CHAL + " VALUES (?,?,?,0,0)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, list.getVoiceid());
            prestmt.setString(2, list.getTextid());
            prestmt.setString(3, list.getServerid());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ChannelList TempGetChannelList(String channelid, String select) throws DatabaseException {
        this.open();
        prestmt = null;
        ChannelList list = null;
        try {
            String sql = null;
            switch (select) {
                case "v":
                    sql = "SELECT * FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_VOICE + " = ?";
                    break;
                case "t":
                    sql = "SELECT * FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_TEXT + " = ?";
                    break;
            }
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, channelid);
            ResultSet rs = prestmt.executeQuery();
            list = new ChannelList();
            while (rs.next()) {
                list.setServerid(rs.getString(TempParameters.TEMP_SERVER));
                list.setVoiceid(rs.getString(TempParameters.LIST_VOICE));
                list.setTextid(rs.getString(TempParameters.LIST_TEXT));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return list;
    }

    public ArrayList<String> TempVoiceids() {
        this.open();
        Statement stmt = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            stmt = con.createStatement();
            String sql = "SELECT DISTINCT " + TempParameters.LIST_VOICE + " FROM " + DAOParameters.DATABASE_CHAL;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getString(TempParameters.LIST_VOICE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return list;
    }

    public void TempDeleteChannelList(String voiceid, String select) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = null;
            switch (select) {
                case "v":
                    sql = "DELETE FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_VOICE + " = ?";
                    break;
                case "t":
                    sql = "DELETE FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_TEXT + " = ?";
                    break;
            }
            if (sql != null) {
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, voiceid);
                prestmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public int GetChannelHide(String id) {
        this.open();
        prestmt = null;
        int sw = -1;
        try {
            String sql = "SELECT " + ChannelParameters.CHANNEL_HIDE + " FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_TEXT + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                sw = rs.getInt(ChannelParameters.CHANNEL_HIDE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return sw;
    }

    public void UpdateChannelHide(String id, int num) {
        if (num != -1) {
            this.open();
            prestmt = null;
            try {
                String sql = "UPDATE " + DAOParameters.DATABASE_CHAL + " SET " + ChannelParameters.CHANNEL_HIDE + " = ? WHERE " + TempParameters.LIST_TEXT + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setInt(1, num);
                prestmt.setString(2, id);
                prestmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.close(prestmt);
            }
        }
    }

    public int GetChannelLock(String id) {
        this.open();
        prestmt = null;
        int sw = -1;
        try {
            String sql = "SELECT " + ChannelParameters.CHANNEL_LOCK + " FROM " + DAOParameters.DATABASE_CHAL + " WHERE " + TempParameters.LIST_TEXT + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                sw = rs.getInt(ChannelParameters.CHANNEL_LOCK);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return sw;
    }

    public void UpdateChannelLock(String id, int num) {
        if (num != -1) {
            this.open();
            prestmt = null;
            try {
                String sql = "UPDATE " + DAOParameters.DATABASE_CHAL + " SET " + ChannelParameters.CHANNEL_LOCK + " = ? WHERE " + TempParameters.LIST_TEXT + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setInt(1, num);
                prestmt.setString(2, id);
                prestmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.close(prestmt);
            }
        }
    }

    public void addMentionMessage(String textid, String messageid, String serverid) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.DATABASE_MEN + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(3, textid);
            prestmt.setString(2, messageid);
            prestmt.setString(1, serverid);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public MentionList getMentionMessage(String textid) {
        this.open();
        MentionList list = new MentionList();
        prestmt = null;
        try {
            String sql = "SELECT " + TempParameters.MENTI_MESS + " FROM " + DAOParameters.DATABASE_MEN + " WHERE " + TempParameters.LIST_TEXT + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                list.addMessages(rs.getString(TempParameters.MENTI_MESS));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return list;
    }

    public MentionList getAllMentionText() {
        this.open();
        MentionList list = new MentionList();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT DISTINCT " + TempParameters.LIST_TEXT + " FROM " + DAOParameters.DATABASE_MEN;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.addtextid(rs.getString(TempParameters.LIST_TEXT));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return list;
    }

    public void deleteMentions(String textid) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.DATABASE_MEN + " WHERE " + TempParameters.LIST_TEXT + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textid);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public String getMentionCannel(String serverid) {
        this.open();
        String mentionid = null;
        prestmt = null;
        try {
            String sql = "SELECT " + TempParameters.TEMP_MENTION + " FROM " + DAOParameters.DATABASE_TEMP + " WHERE " + TempParameters.TEMP_SERVER + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                mentionid = rs.getString(TempParameters.TEMP_MENTION);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return mentionid;
    }

    public ArrayList<String> getServerList() {
        Statement stmt = null;
        ArrayList<String> serveridlist = new ArrayList<>();
        this.open();
        try {
            String sql = "SELECT " + TempParameters.TEMP_SERVER + " FROM " + DAOParameters.DATABASE_TEMP;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                serveridlist.add(rs.getString(TempParameters.TEMP_SERVER));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return serveridlist;
    }

    public void setReactMessageData(String serverid, String textid, String messageid) {
        this.open();
        prestmt = null;
        PreparedStatement prestmt2 = null;
        String sql;
        try {
            sql = "INSERT INTO " + RoleParameters.TABLE_MESSAGE + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            prestmt.setString(2, textid);
            prestmt.setString(3, messageid);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                String sql2 = "SELECT * FROM " + RoleParameters.TABLE_MESSAGE + " WHERE " + RoleParameters.ROLE_SERVERID + " = ?";
                prestmt2 = con.prepareStatement(sql2);
                prestmt2.setString(1, serverid);
                ResultSet rs = prestmt2.executeQuery();
                ReactionRoleRecord record = new ReactionRoleRecord();
                while (rs.next()) {
                    record.setServerid(serverid);
                    record.setTextchannelid(rs.getString(RoleParameters.ROLE_TEXTCHANNELID));
                    record.setMessageid(rs.getString(RoleParameters.ROLE_MESSAGEID));
                }
                String sql3 = "DELETE FROM " + RoleParameters.TABLE_ROLE + " WHERE " + RoleParameters.ROLE_MESSAGEID + " = " + record.getMessageid();
                Statement stmt = con.createStatement();
                stmt.execute(sql3);
                sql = "UPDATE " + RoleParameters.TABLE_MESSAGE + " SET " + RoleParameters.ROLE_TEXTCHANNELID + " = ?," + RoleParameters.ROLE_MESSAGEID + " = ? WHERE " + RoleParameters.ROLE_SERVERID + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, textid);
                prestmt.setString(2, messageid);
                prestmt.setString(3, serverid);
                prestmt.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
            this.close(prestmt2);
        }
    }

    public ReactionRoleRecord getReactMessageData(String serverid) {
        this.open();
        prestmt = null;
        ReactionRoleRecord record = null;
        try {
            String sql = "SELECT * FROM " + RoleParameters.TABLE_MESSAGE + " WHERE " + RoleParameters.ROLE_SERVERID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerid(serverid);
                record.setTextchannelid(rs.getString(RoleParameters.ROLE_TEXTCHANNELID));
                record.setMessageid(rs.getString(RoleParameters.ROLE_MESSAGEID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return record;
    }

    public void setReactRoleData(String messageid, String roleid, String emoji) {
        this.open();
        prestmt = null;
        String sql;
        try {
            sql = "INSERT INTO " + RoleParameters.TABLE_ROLE + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, messageid);
            prestmt.setString(2, roleid);
            prestmt.setString(3, emoji);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                sql = "UPDATE " + RoleParameters.TABLE_ROLE + " SET " + RoleParameters.ROLE_ROLEID + " = ? WHERE " + RoleParameters.ROLE_MESSAGEID + " = ? AND " + RoleParameters.ROLE_EMOJI + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, roleid);
                prestmt.setString(2, messageid);
                prestmt.setString(3, emoji);
                prestmt.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ReactionRoleRecord getReactAllData(String serverid) {
        this.open();
        prestmt = null;
        PreparedStatement prestmt2 = null;
        ReactionRoleRecord record = null;
        try {
            String sql = "SELECT * FROM " + RoleParameters.TABLE_MESSAGE + " WHERE " + RoleParameters.ROLE_SERVERID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerid(serverid);
                record.setTextchannelid(rs.getString(RoleParameters.ROLE_TEXTCHANNELID));
                record.setMessageid(rs.getString(RoleParameters.ROLE_MESSAGEID));
            }
            String sql2 = "SELECT * FROM " + RoleParameters.TABLE_ROLE + " WHERE " + RoleParameters.ROLE_MESSAGEID + " = ?";
            prestmt2 = con.prepareStatement(sql2);
            prestmt2.setString(1, record.getMessageid());
            ResultSet rs2 = prestmt2.executeQuery();
            while (rs2.next()) {
                record.addEmoji(rs2.getString(RoleParameters.ROLE_EMOJI));
                record.addRoleid(rs2.getString(RoleParameters.ROLE_ROLEID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
            this.close(prestmt2);
        }
        return record;
    }

    public void deleteRoles(String emoji, String message) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + RoleParameters.TABLE_ROLE + " WHERE " + RoleParameters.ROLE_EMOJI + " = ? AND " + RoleParameters.ROLE_MESSAGEID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, emoji);
            prestmt.setString(2, message);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }
}