package wkwk.dao;

import wkwk.*;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.paramater.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class DiscordDAO extends DAOBase {

    public String BotGetToken() throws DatabaseException, SystemException {
        this.open();
        String token = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT " + BotDataParameters.BOT_TOKEN.getParameter() + " FROM " + DAOParameters.TABLE_BOT_DATA.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) token = rs.getString(BotDataParameters.BOT_TOKEN.getParameter());
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
            String sql = "SELECT " + ServerPropertyParameters.PREFIX.getParameter() + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) prefix = rs.getString(ServerPropertyParameters.PREFIX.getParameter());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return prefix;
    }

    public ServerDataList TempGetData(String serverId) throws SystemException, DatabaseException {
        this.open();
        prestmt = null;
        ServerDataList dataList = new ServerDataList();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                dataList.setServer(serverId);
                dataList.setPrefix(rs.getString(ServerPropertyParameters.PREFIX.getParameter()));
                dataList.setMentioncal(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter()));
                dataList.setFstchannel(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter()));
                dataList.setVoicecate(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter()));
                dataList.setTextcate(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter()));
                dataList.setTempBy(rs.getBoolean(ServerPropertyParameters.TEMP_BY.getParameter()));
                dataList.setTextBy(rs.getBoolean(ServerPropertyParameters.TEXT_BY.getParameter()));
                dataList.setStereotyped(rs.getString((ServerPropertyParameters.STEREOTYPED.getParameter())));
                dataList.setDefaultSize(rs.getString(ServerPropertyParameters.DEFAULT_SIZE.getParameter()));
                dataList.setDefaultName(rs.getString(ServerPropertyParameters.DEFAULT_NAME.getParameter()));
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
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("t".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("f".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("p".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.PREFIX.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("m".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("tmpby".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.TEMP_BY.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("txtby".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.TEXT_BY.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("size".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.DEFAULT_SIZE.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("stereo".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.STEREOTYPED.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("defname".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.DEFAULT_NAME.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
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
            String sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter() + " = ?," + ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter() + " = ?," + ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter() + " = ?," + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " (" + ServerPropertyParameters.SERVER_ID.getParameter() + "," + ServerPropertyParameters.PREFIX.getParameter() + ") VALUES(?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.setString(2, ServerPropertyParameters.DEFAULT_PREFIX.getParameter());
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            String messageId = null;
            sql = "SELECT " + ReactMessageParameters.MESSAGE_ID.getParameter() + " FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            ResultSet r = prestmt.executeQuery();
            while (r.next()){
                messageId = r.getString(ReactMessageParameters.MESSAGE_ID.getParameter());
            }
            sql = "DELETE FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
            sql = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, messageId);
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " VALUES (?,?,?,0,0)";
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
                sql = "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.VOICE_CHANNEL_ID.getParameter() + " = ?";
            else if ("t".equals(select))
                sql = "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, channelid);
            ResultSet rs = prestmt.executeQuery();
            list = new ChannelList();
            while (rs.next()) {
                list.setServerID(rs.getString(TempChannelsParameters.SERVER_ID.getParameter()));
                list.setVoiceID(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID.getParameter()));
                list.setTextID(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID.getParameter()));
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
            String sql = "SELECT DISTINCT " + TempChannelsParameters.VOICE_CHANNEL_ID.getParameter() + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID.getParameter()));
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
                sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.VOICE_CHANNEL_ID.getParameter() + " = ?";
            else if ("t".equals(select))
                sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
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

    public boolean GetChannelHide(String id) {
        this.open();
        prestmt = null;
        boolean sw = false;
        try {
            String sql = "SELECT " + TempChannelsParameters.HIDE_BY.getParameter() + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) sw = rs.getBoolean(TempChannelsParameters.HIDE_BY.getParameter());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return sw;
    }

    public void UpdateChannelHide(String id, boolean num) {
        this.open();
        prestmt = null;
        try {
            String sql = "UPDATE " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " SET " + TempChannelsParameters.HIDE_BY.getParameter() + " = ? WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setBoolean(1, num);
            prestmt.setString(2, id);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public boolean GetChannelLock(String id) {
        this.open();
        prestmt = null;
        boolean sw = false;
        try {
            String sql = "SELECT " + TempChannelsParameters.LOCK_BY.getParameter() + " FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, id);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) sw = rs.getBoolean(TempChannelsParameters.LOCK_BY.getParameter());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return sw;
    }

    public void UpdateChannelLock(String id, boolean num) {
        this.open();
        prestmt = null;
        try {
            String sql = "UPDATE " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " SET " + TempChannelsParameters.LOCK_BY.getParameter() + " = ? WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setBoolean(1, num);
            prestmt.setString(2, id);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void addMentionMessage(String textid, String messageid, String serverid) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " VALUES (?,?,?)";
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
            String sql = "SELECT " + MentionMessageParameters.MESSAGE_ID.getParameter() + " FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) list.getMessages().add(rs.getString(MentionMessageParameters.MESSAGE_ID.getParameter()));
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
            String sql = "SELECT DISTINCT " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.getTextID().add(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID.getParameter()));
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
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
            String sql = "SELECT " + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) mentionid = rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter());
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
            String sql = "SELECT " + ServerPropertyParameters.SERVER_ID.getParameter() + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) serveridlist.add(rs.getString(ServerPropertyParameters.SERVER_ID.getParameter()));
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
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            prestmt.setString(2, textid);
            prestmt.setString(3, messageid);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
                prestmt2 = con.prepareStatement(sql2);
                prestmt2.setString(1, serverid);
                ResultSet rs = prestmt2.executeQuery();
                ReactionRoleRecord record = new ReactionRoleRecord();
                while (rs.next()) {
                    record.setServerID(serverid);
                    record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                    record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
                }
                String sql3 = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = " + record.getMessageID();
                Statement stmt = con.createStatement();
                stmt.execute(sql3);
                sql = "UPDATE " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " SET " + ReactMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ?," + ReactMessageParameters.MESSAGE_ID.getParameter() + " = ? WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
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
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverid);
                record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
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
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, messageid);
            prestmt.setString(2, roleid);
            prestmt.setString(3, emoji);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                sql = "UPDATE " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " SET " + ReactRoleParameters.ROLE_ID.getParameter() + " = ? WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ? AND " + ReactRoleParameters.EMOJI.getParameter() + " = ?";
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
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverid);
                record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
            }
            String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt2 = con.prepareStatement(sql2);
            prestmt2.setString(1, record.getMessageID());
            ResultSet rs2 = prestmt2.executeQuery();
            while (rs2.next()) {
                record.getEmoji().add(rs2.getString(ReactRoleParameters.EMOJI.getParameter()));
                record.getRoleID().add(rs2.getString(ReactRoleParameters.ROLE_ID.getParameter()));
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
            String sql = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.EMOJI.getParameter() + " = ? AND " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ?";
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
    public String GetServerCount() {
        this.open();
        String count = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT COUNT(" + ServerPropertyParameters.SERVER_ID.getParameter() + ") AS SERVERCOUNT FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) count = String.format("%,d", rs.getInt("SERVERCOUNT"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return count;
    }

    public String GetVoiceCount() {
        this.open();
        String count = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT COUNT(" + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + ") AS VOICECOUNT FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) count = String.format("%,d", rs.getInt("VOICECOUNT"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return count;
    }

    public void addNamePreset(String serverid,String name) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " VALUES (?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverid);
            prestmt.setString(2, name);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<String> GetNamePreset(String serverId) {
        this.open();
        ArrayList<String> names = new ArrayList<>();
        prestmt = null;
        try {
            String sql = "SELECT " + NamePresetParameters.NAME.getParameter() + " FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " WHERE " + NamePresetParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(NamePresetParameters.NAME.getParameter()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return names;
    }
    public void deleteNamePreset(String serverId,String name) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " WHERE " + NamePresetParameters.SERVER_ID.getParameter() + " = ? AND " + NamePresetParameters.NAME.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            prestmt.setString(2, name);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public TweetAPIList getAutoTweetApis(){
        this.open();
        TweetAPIList apis = new TweetAPIList();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            String sql = "SELECT * FROM " + DAOParameters.TABLE_BOT_DATA.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                apis.setApi(rs.getString(BotDataParameters.API_KEY.getParameter()));
                apis.setApiSecret(rs.getString(BotDataParameters.API_SECRET_KEY.getParameter()));
                apis.setToken(rs.getString(BotDataParameters.ACCESS_TOKEN.getParameter()));
                apis.setTokenSecret(rs.getString(BotDataParameters.ACCESS_TOKEN_SECRET.getParameter()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return apis;
    }
}