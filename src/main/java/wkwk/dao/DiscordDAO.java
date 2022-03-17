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
            String sql = "SELECT " + BotDataParameters.BOT_TOKEN + " FROM " + DAOParameters.TABLE_BOT_DATA;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) token = rs.getString(BotDataParameters.BOT_TOKEN);
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
            String sql = "SELECT " + ServerPropertyParameters.PREFIX + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY + " WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) prefix = rs.getString(ServerPropertyParameters.PREFIX);
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
            String sql = "SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY + " WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                dataList.setServer(serverid);
                dataList.setPrefix(rs.getString(ServerPropertyParameters.PREFIX));
                dataList.setMentioncal(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID));
                dataList.setFstchannel(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID));
                dataList.setVoicecate(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID));
                dataList.setTextcate(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID));
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
            if ("v".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.VOICE_CATEGORY_ID + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            else if ("t".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.TEXT_CATEGORY_ID + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            else if ("f".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.FIRST_CHANNEL_ID + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            else if ("p".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.PREFIX + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
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
            String sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.FIRST_CHANNEL_ID + " = ?," + ServerPropertyParameters.TEXT_CATEGORY_ID + " = ?," + ServerPropertyParameters.VOICE_CATEGORY_ID + " = ?," + ServerPropertyParameters.MENTION_CHANNEL_ID + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_SERVER_PROPERTY + " (" + ServerPropertyParameters.SERVER_ID + "," + ServerPropertyParameters.PREFIX + ") VALUES(?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.setString(2, ServerPropertyParameters.DEFAULT_PREFIX);
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_SERVER_PROPERTY + " WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE + " WHERE " + MentionMessageParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE me,ro FROM " + DAOParameters.TABLE_REACT_MESSAGE + " AS me LEFT JOIN " + DAOParameters.TABLE_REACT_ROLE +
                    " AS ro ON ro." + ReactRoleParameters.MESSAGE_ID + " = me." + ReactMessageParameters.MESSAGE_ID + " WHERE me." +
                    ReactMessageParameters.SERVER_ID + " = ?";
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_TEMP_CHANNEL + " VALUES (?,?,?,0,0)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, list.getVoiceID());
            prestmt.setString(2, list.getTextID());
            prestmt.setString(3, list.getServerID());
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
            if ("v".equals(select))
                sql = "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.VOICE_CHANNEL_ID + " = ?";
            else if ("t".equals(select))
                sql = "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, channelid);
            ResultSet rs = prestmt.executeQuery();
            list = new ChannelList();
            while (rs.next()) {
                list.setServerID(rs.getString(TempChannelsParameters.SERVER_ID));
                list.setVoiceID(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID));
                list.setTextID(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID));
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
            String sql = "SELECT DISTINCT " + TempChannelsParameters.VOICE_CHANNEL_ID + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID));
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
            if ("v".equals(select))
                sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.VOICE_CHANNEL_ID + " = ?";
            else if ("t".equals(select))
                sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
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
            String sql = "SELECT " + TempChannelsParameters.HIDE_BY + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) sw = rs.getInt(TempChannelsParameters.HIDE_BY);
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
                String sql = "UPDATE " + DAOParameters.TABLE_TEMP_CHANNEL + " SET " + TempChannelsParameters.HIDE_BY + " = ? WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
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
            String sql = "SELECT " + TempChannelsParameters.LOCK_BY + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) sw = rs.getInt(TempChannelsParameters.LOCK_BY);
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
                String sql = "UPDATE " + DAOParameters.TABLE_TEMP_CHANNEL + " SET " + TempChannelsParameters.LOCK_BY + " = ? WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_MENTION_MESSAGE + " VALUES (?,?,?)";
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
            String sql = "SELECT " + MentionMessageParameters.MESSAGE_ID + " FROM " + DAOParameters.TABLE_MENTION_MESSAGE + " WHERE " + MentionMessageParameters.TEXT_CHANNEL_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) list.getMessages().add(rs.getString(MentionMessageParameters.MESSAGE_ID));
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
            String sql = "SELECT DISTINCT " + TempChannelsParameters.TEXT_CHANNEL_ID + " FROM " + DAOParameters.TABLE_MENTION_MESSAGE;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.getTextID().add(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID));
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID + " = ?";
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
            String sql = "SELECT " + ServerPropertyParameters.MENTION_CHANNEL_ID + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY + " WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) mentionid = rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID);
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
            String sql = "SELECT " + ServerPropertyParameters.SERVER_ID + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) serveridlist.add(rs.getString(ServerPropertyParameters.SERVER_ID));
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
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_MESSAGE + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            prestmt.setString(2, textid);
            prestmt.setString(3, messageid);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE + " WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
                prestmt2 = con.prepareStatement(sql2);
                prestmt2.setString(1, serverid);
                ResultSet rs = prestmt2.executeQuery();
                ReactionRoleRecord record = new ReactionRoleRecord();
                while (rs.next()) {
                    record.setServerID(serverid);
                    record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID));
                    record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID));
                }
                String sql3 = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE + " WHERE " + ReactRoleParameters.MESSAGE_ID + " = " + record.getMessageID();
                Statement stmt = con.createStatement();
                stmt.execute(sql3);
                sql = "UPDATE " + DAOParameters.TABLE_REACT_MESSAGE + " SET " + ReactMessageParameters.TEXT_CHANNEL_ID + " = ?," + ReactMessageParameters.MESSAGE_ID + " = ? WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
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
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE + " WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverid);
                record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID));
                record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID));
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
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_ROLE + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, messageid);
            prestmt.setString(2, roleid);
            prestmt.setString(3, emoji);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                sql = "UPDATE " + DAOParameters.TABLE_REACT_ROLE + " SET " + ReactRoleParameters.ROLE_ID + " = ? WHERE " + ReactRoleParameters.MESSAGE_ID + " = ? AND " + ReactRoleParameters.EMOJI + " = ?";
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
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE + " WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverid);
                record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID));
                record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID));
            }
            String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_ROLE + " WHERE " + ReactRoleParameters.MESSAGE_ID + " = ?";
            prestmt2 = con.prepareStatement(sql2);
            prestmt2.setString(1, record.getMessageID());
            ResultSet rs2 = prestmt2.executeQuery();
            while (rs2.next()) {
                record.getEmoji().add(rs2.getString(ReactRoleParameters.EMOJI));
                record.getRoleID().add(rs2.getString(ReactRoleParameters.ROLE_ID));
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE + " WHERE " + ReactRoleParameters.EMOJI + " = ? AND " + ReactRoleParameters.MESSAGE_ID + " = ?";
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
    public void setMentionChannel(String textchanel , String serverid) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_SERVER_PROPERTY + " (" + ServerPropertyParameters.MENTION_CHANNEL_ID + ") VALUES (?) WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textchanel);
            prestmt.setString(2, serverid);
            prestmt.execute();
        }catch (SQLIntegrityConstraintViolationException e) {
            String sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY + " SET " + ServerPropertyParameters.MENTION_CHANNEL_ID + " = ? WHERE " + ServerPropertyParameters.SERVER_ID + " = ?";
            try {
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, textchanel);
                prestmt.setString(2, serverid);
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
}