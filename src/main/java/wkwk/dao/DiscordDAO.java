package wkwk.dao;

import wkwk.*;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
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
                dataList.setMentionChannel(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter()));
                dataList.setFstChannel(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter()));
                dataList.setVoiceCategory(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter()));
                dataList.setTextCategory(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter()));
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
            prestmt.setString(1, data.getFstChannel());
            prestmt.setString(2, data.getTextCategory());
            prestmt.setString(3, data.getVoiceCategory());
            prestmt.setString(4, data.getMentionChannel());
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
            while (r.next()) {
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

    public ChannelList TempGetChannelList(String channelId, String select) throws DatabaseException {
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
            prestmt.setString(1, channelId);
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

    public ArrayList<String> TempVoiceIds() {
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

    public void TempDeleteChannelList(String voiceId, String select) throws DatabaseException {
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
                prestmt.setString(1, voiceId);
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

    public void addMentionMessage(String textId, String messageId, String serverId) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " VALUES (?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(3, textId);
            prestmt.setString(2, messageId);
            prestmt.setString(1, serverId);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public MentionList getMentionMessage(String textId) {
        this.open();
        MentionList list = new MentionList();
        prestmt = null;
        try {
            String sql = "SELECT " + MentionMessageParameters.MESSAGE_ID.getParameter() + " FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textId);
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

    public void deleteMentions(String textId) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, textId);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public String getMentionChannel(String serverId) {
        this.open();
        String mentionId = null;
        prestmt = null;
        try {
            String sql = "SELECT " + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) mentionId = rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return mentionId;
    }

    public ArrayList<String> getServerList() {
        Statement stmt = null;
        ArrayList<String> serverIdList = new ArrayList<>();
        this.open();
        try {
            String sql = "SELECT " + ServerPropertyParameters.SERVER_ID.getParameter() + " FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) serverIdList.add(rs.getString(ServerPropertyParameters.SERVER_ID.getParameter()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return serverIdList;
    }

    public void setReactMessageData(String serverId, String textId, String messageId) {
        this.open();
        this.prestmt = null;
        String sql;
        try {
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " VALUES (?,?,?)";
            this.prestmt = con.prepareStatement(sql);
            this.prestmt.setString(1, serverId);
            this.prestmt.setString(2, textId);
            this.prestmt.setString(3, messageId);
            this.prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
                prestmt = con.prepareStatement(sql2);
                prestmt.setString(1, serverId);
                ResultSet rs = prestmt.executeQuery();
                ReactionRoleRecord record = new ReactionRoleRecord();
                while (rs.next()) {
                    record.setServerID(serverId);
                    record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                    record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
                }
                String sql3 = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = " + record.getMessageID();
                Statement stmt = con.createStatement();
                stmt.execute(sql3);
                sql = "UPDATE " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " SET " + ReactMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ?," + ReactMessageParameters.MESSAGE_ID.getParameter() + " = ? WHERE " + ReactMessageParameters.SERVER_ID + " = ?";
                this.prestmt = con.prepareStatement(sql);
                this.prestmt.setString(1, textId);
                this.prestmt.setString(2, messageId);
                this.prestmt.setString(3, serverId);
                this.prestmt.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(this.prestmt);
            this.close(prestmt);
        }
    }

    public ReactionRoleRecord getReactMessageData(String serverId) {
        this.open();
        prestmt = null;
        ReactionRoleRecord record = null;
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverId);
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

    public void setReactRoleData(String serverId, String messageId, String roleId, String emoji) {
        this.open();
        prestmt = null;
        String sql;
        try {
            sql = "INSERT INTO " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " VALUES (?,?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, messageId);
            prestmt.setString(2, roleId);
            prestmt.setString(3, emoji);
            prestmt.setString(4, serverId);
            prestmt.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                sql = "UPDATE " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " SET " + ReactRoleParameters.ROLE_ID.getParameter() + " = ? WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ? AND " + ReactRoleParameters.EMOJI.getParameter() + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, roleId);
                prestmt.setString(2, messageId);
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

    public ReactionRoleRecord getReactAllData(String serverId) {
        this.open();
        this.prestmt = null;
        ReactionRoleRecord record = null;
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = ?";
            this.prestmt = con.prepareStatement(sql);
            this.prestmt.setString(1, serverId);
            ResultSet rs = this.prestmt.executeQuery();
            record = new ReactionRoleRecord();
            while (rs.next()) {
                record.setServerID(serverId);
                record.setTextChannelID(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageID(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
            }
            String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql2);
            prestmt.setString(1, record.getMessageID());
            ResultSet rs2 = prestmt.executeQuery();
            while (rs2.next()) {
                System.out.println(rs2.getString(ReactRoleParameters.EMOJI.getParameter()));
                record.getEmoji().add(rs2.getString(ReactRoleParameters.EMOJI.getParameter()));
                record.getRoleID().add(rs2.getString(ReactRoleParameters.ROLE_ID.getParameter()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(this.prestmt);
            this.close(prestmt);
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
            String sql = "SELECT COUNT(" + ServerPropertyParameters.SERVER_ID.getParameter() + ") AS SERVER_COUNT FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) count = String.format("%,d", rs.getInt("SERVER_COUNT"));
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
            String sql = "SELECT COUNT(" + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + ") AS VOICE_COUNT FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) count = String.format("%,d", rs.getInt("VOICE_COUNT"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return count;
    }

    public void addNamePreset(String serverId, String name) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " VALUES (?,?)";
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

    public void deleteNamePreset(String serverId, String name) {
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

    public void deleteNamePreset(String serverId) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " WHERE " + NamePresetParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public TweetAPIList getAutoTweetApis() {
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

    public boolean addDeleteTimes(DeleteTimeRecord record) {
        this.open();
        prestmt = null;
        boolean exc = false;
        String unit = record.getTimeUnit();
        if (unit.equals("s") || unit.equals("m") || unit.equals("h") || unit.equals("d") || unit.equals("S") || unit.equals("M") || unit.equals("H") || unit.equals("D") && record.getDeleteTime() > 0) {
            try {
                String sql = "INSERT INTO " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " VALUES (?,?,?,?)";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, record.getServerId());
                prestmt.setString(2, record.getTextChannelId());
                prestmt.setInt(3, record.getDeleteTime());
                prestmt.setString(4, record.getTimeUnit());
                prestmt.execute();
                exc = true;
            } catch (SQLIntegrityConstraintViolationException e) {
                String sql = "UPDATE " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " SET " + DeleteTimesParameters.DELETE_TIME.getParameter() + " = ?," + DeleteTimesParameters.TIME_UNIT.getParameter() + " = ? WHERE " + DeleteTimesParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
                try {
                    prestmt = con.prepareStatement(sql);
                    prestmt.setInt(1, record.getDeleteTime());
                    prestmt.setString(2, record.getTimeUnit());
                    prestmt.setString(3, record.getTextChannelId());
                    prestmt.execute();
                    exc = true;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.close(prestmt);
            }
        }
        return exc;
    }

    public void removeDeleteTimes(String channelId) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE " + DeleteTimesParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, channelId);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void deleteDeleteTimes(String serverId) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE " + DeleteTimesParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<DeleteTimeRecord> getDeleteTimes(String serverId) {
        ArrayList<DeleteTimeRecord> list = new ArrayList<>();
        this.open();
        prestmt = null;
        try {
            String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE " + DeleteTimesParameters.SERVER_ID.getParameter() + " = ?) AS TIME_CHECK";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("TIME_CHECK") == 1) {
                    sql = "SELECT * FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE " + DeleteTimesParameters.SERVER_ID.getParameter() + " = ?";
                    prestmt = con.prepareStatement(sql);
                    prestmt.setString(1, serverId);
                    ResultSet resultSet = prestmt.executeQuery();
                    while (resultSet.next()) {
                        DeleteTimeRecord record = new DeleteTimeRecord();
                        record.setServerId(resultSet.getString(DeleteTimesParameters.SERVER_ID.getParameter()));
                        record.setTextChannelId(resultSet.getString(DeleteTimesParameters.TEXT_CHANNEL_ID.getParameter()));
                        record.setDeleteTime(resultSet.getInt(DeleteTimesParameters.DELETE_TIME.getParameter()));
                        record.setTimeUnit(resultSet.getString(DeleteTimesParameters.TIME_UNIT.getParameter()));
                        list.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return list;
    }

    public void addDeleteMessage(DeleteMessage message) {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " VALUES (?,?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, message.getServerId());
            prestmt.setString(2, message.getMessageId());
            prestmt.setString(3, message.getDeleteTime());
            prestmt.setString(4, message.getChannelId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<DeleteMessage> getDeleteMessage(String date) { // now
        ArrayList<DeleteMessage> list = new ArrayList<>();
        this.open();
        prestmt = null;


        String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.DELETE_TIME.getParameter() + " = ?) AS MESSAGE_CHECK";
        try {
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, date);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("MESSAGE_CHECK") == 1) {
                    sql = "SELECT * FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.DELETE_TIME.getParameter() + " = ?";
                    prestmt = con.prepareStatement(sql);
                    try {
                        prestmt.setString(1, date);
                        ResultSet rsx = prestmt.executeQuery();
                        while (rsx.next()) {
                            DeleteMessage message = new DeleteMessage();
                            message.setMessageId(rsx.getString(DeleteMessagesParameters.MESSAGE_ID.getParameter()));
                            message.setChannelId(rsx.getString(DeleteMessagesParameters.TEXT_CHANNEL_ID.getParameter()));
                            list.add(message);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteMessage(String select, String id) {
        this.open();
        prestmt = null;
        try {
            if (select.equalsIgnoreCase("m")) {
                String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.MESSAGE_ID.getParameter() + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, id);
                prestmt.execute();
            } else if (select.equalsIgnoreCase("s")) {
                String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.SERVER.getParameter() + " = ?";
                prestmt = con.prepareStatement(sql);
                prestmt.setString(1, id);
                prestmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void addLogging(ArrayList<LoggingRecord> records){
        this.open();
        try {
            for (LoggingRecord record : records) {
                try {
                    prestmt = null;
                    String sql = "INSERT INTO " + DAOParameters.TABLE_LOGGING.getParameter() + " VALUES (?,?,?,?)";
                    prestmt = con.prepareStatement(sql);
                    prestmt.setString(1, record.getServerId());
                    prestmt.setString(2, record.getChannelId());
                    prestmt.setString(3, record.getLogType());
                    prestmt.setString(4, record.getTargetChannelId());
                    prestmt.execute();
                } catch (SQLIntegrityConstraintViolationException ignored) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<LoggingRecord> getLogging(String select ,String id) {
        this.open();
        prestmt = null;
        ArrayList<LoggingRecord> records = new ArrayList<>();
        switch (select){
            case "c":
                String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.TARGET_CHANNEL_ID.getParameter() + " = ?) AS TARGET_CHECK";
                try {
                    prestmt = con.prepareStatement(sql);
                    prestmt.setString(1, id);
                    ResultSet rs = prestmt.executeQuery();
                    while (rs.next()) {
                        if (rs.getInt("TARGET_CHECK") == 1) {
                            sql = "SELECT * FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.TARGET_CHANNEL_ID.getParameter() + " = ?";
                            prestmt = con.prepareStatement(sql);
                            try {
                                prestmt.setString(1, id);
                                ResultSet rsx = prestmt.executeQuery();
                                while (rsx.next()) {
                                    LoggingRecord record = new LoggingRecord();
                                    record.setServerId(rsx.getString(LoggingParameters.SERVER_ID.getParameter()));
                                    record.setChannelId(rsx.getString(LoggingParameters.CHANNEL_ID.getParameter()));
                                    record.setLogType(rsx.getString(LoggingParameters.LOG_TYPE.getParameter()));
                                    record.setTargetChannelId(rsx.getString(LoggingParameters.TARGET_CHANNEL_ID.getParameter()));
                                    records.add(record);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "s":
                sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.SERVER_ID.getParameter() + " = ?) AS SERVER_CHECK";
                try {
                    prestmt = con.prepareStatement(sql);
                    prestmt.setString(1, id);
                    ResultSet rs = prestmt.executeQuery();
                    while (rs.next()) {
                        if (rs.getInt("SERVER_CHECK") == 1) {
                            sql = "SELECT * FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.SERVER_ID.getParameter() + " = ?";
                            prestmt = con.prepareStatement(sql);
                            try {
                                prestmt.setString(1, id);
                                ResultSet rsx = prestmt.executeQuery();
                                while (rsx.next()) {
                                    LoggingRecord record = new LoggingRecord();
                                    record.setServerId(rsx.getString(LoggingParameters.SERVER_ID.getParameter()));
                                    record.setChannelId(rsx.getString(LoggingParameters.CHANNEL_ID.getParameter()));
                                    record.setLogType(rsx.getString(LoggingParameters.LOG_TYPE.getParameter()));
                                    record.setTargetChannelId(rsx.getString(LoggingParameters.TARGET_CHANNEL_ID.getParameter()));
                                    records.add(record);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
        return records;
    }

    public void deleteLogging(String target, String type, String channel) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.TARGET_CHANNEL_ID.getParameter() + " = ? AND " + LoggingParameters.CHANNEL_ID.getParameter() + " = ? AND " + LoggingParameters.LOG_TYPE.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, target);
            prestmt.setString(2, channel);
            prestmt.setString(3, type);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }
    public void deleteLogging(String server) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, server);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }
}