package wkwk.dao;

import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.*;
import wkwk.parameter.record.*;

import java.sql.*;
import java.util.ArrayList;

public class DiscordDAO extends DAOBase {


    public void serverLeaveAllDataDelete(String serverId) {
        this.open();
        Statement stmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE " + BotSendMessageParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE " + DeleteTimesParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE " + LoggingParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " WHERE " + NamePresetParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE " + ReactMessageParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
            stmt = null;
            sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.SERVER_ID.getParameter() + " = " + serverId;
            stmt = con.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
    }

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

    public ServerDataRecord TempGetData(String serverId) throws SystemException, DatabaseException {
        this.open();
        prestmt = null;
        ServerDataRecord dataList = new ServerDataRecord();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                dataList.setServerId(serverId);
                dataList.setMentionChannelId(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter()));
                dataList.setFstChannelId(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter()));
                dataList.setVoiceCategoryId(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter()));
                dataList.setTextCategoryId(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter()));
                dataList.setTempBy(rs.getBoolean(ServerPropertyParameters.TEMP_BY.getParameter()));
                dataList.setTextBy(rs.getBoolean(ServerPropertyParameters.TEXT_BY.getParameter()));
                dataList.setStereoTyped(rs.getString((ServerPropertyParameters.STEREOTYPED.getParameter())));
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
            else if ("m".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("tempBy".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.TEMP_BY.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("txtBy".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.TEXT_BY.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("size".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.DEFAULT_SIZE.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("stereo".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.STEREOTYPED.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("defName".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.DEFAULT_NAME.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            else if ("p".equals(idx))
                sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET PREFIX = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
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

    public void TempDataUpData(ServerDataRecord data) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "UPDATE " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " SET " + ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter() + " = ?," + ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter() + " = ?," + ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter() + " = ?," + ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter() + " = ? WHERE " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, data.getFstChannelId());
            prestmt.setString(2, data.getTextCategoryId());
            prestmt.setString(3, data.getVoiceCategoryId());
            prestmt.setString(4, data.getMentionChannelId());
            prestmt.setString(5, data.getServerId());
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
            String sql = "INSERT INTO " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " (" + ServerPropertyParameters.SERVER_ID.getParameter() + " ) VALUES(?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Server);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void TempSetChannelList(ChannelRecord list) throws DatabaseException {
        this.open();
        prestmt = null;
        try {
            String sql = "INSERT INTO " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " VALUES (?,?,?,0,0)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, list.getVoiceId());
            prestmt.setString(2, list.getTextId());
            prestmt.setString(3, list.getServerId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ChannelRecord TempGetChannelList(String channelId, String select) throws DatabaseException {
        this.open();
        prestmt = null;
        ChannelRecord list = null;
        try {
            String sql = switch (select) {
                case "v" ->
                        "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.VOICE_CHANNEL_ID.getParameter() + " = ?";
                case "t" ->
                        "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE " + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
                default -> null;
            };
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, channelId);
            ResultSet rs = prestmt.executeQuery();
            list = new ChannelRecord();
            while (rs.next()) {
                list.setServerId(rs.getString(TempChannelsParameters.SERVER_ID.getParameter()));
                list.setVoiceId(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID.getParameter()));
                list.setTextId(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID.getParameter()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
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

    public MentionMessageRecord getMentionMessage(String textId) {
        this.open();
        MentionMessageRecord list = new MentionMessageRecord();
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
                    record.setServerId(serverId);
                    record.setTextChannelId(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                    record.setMessageId(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
                }
                String sql3 = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = " + record.getMessageId();
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
                record.setServerId(serverId);
                record.setTextChannelId(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
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
                record.setServerId(serverId);
                record.setTextChannelId(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
            }
            String sql2 = "SELECT * FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE " + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql2);
            prestmt.setString(1, record.getMessageId());
            ResultSet rs2 = prestmt.executeQuery();
            while (rs2.next()) {
                record.getEmoji().add(rs2.getString(ReactRoleParameters.EMOJI.getParameter()));
                record.getRoleId().add(rs2.getString(ReactRoleParameters.ROLE_ID.getParameter()));
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

    public TweetAPIRecord getAutoTweetApis() {
        this.open();
        TweetAPIRecord apis = new TweetAPIRecord();
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

    public void addDeleteMessage(DeleteMessageRecord message) {
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

    public ArrayList<DeleteMessageRecord> getDeleteMessage(String date) {
        ArrayList<DeleteMessageRecord> list = new ArrayList<>();
        this.open();
        String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.DELETE_TIME.getParameter() + " < ?) AS MESSAGE_CHECK";
        String sql2 = "SELECT * FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.DELETE_TIME.getParameter() + " < ?";
        try {
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, date);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("MESSAGE_CHECK") == 1) {
                    try {
                        prestmt = con.prepareStatement(sql2);
                        prestmt.setString(1, date);
                        ResultSet rsx = prestmt.executeQuery();
                        while (rsx.next()) {
                            DeleteMessageRecord message = new DeleteMessageRecord();
                            message.setMessageId(rsx.getString(DeleteMessagesParameters.MESSAGE_ID.getParameter()));
                            message.setChannelId(rsx.getString(DeleteMessagesParameters.TEXT_CHANNEL_ID.getParameter()));
                            list.add(message);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
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

    public void deleteMessage(String select, String id) {
        this.open();
        PreparedStatement preStmt3 = null;
        try {
            if (select.equalsIgnoreCase("m")) {
                String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.MESSAGE_ID.getParameter() + " = ?";
                preStmt3 = con.prepareStatement(sql);
                preStmt3.setString(1, id);
                preStmt3.execute();
            } else if (select.equalsIgnoreCase("s")) {
                String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.SERVER_ID.getParameter() + " = ?";
                preStmt3 = con.prepareStatement(sql);
                preStmt3.setString(1, id);
                preStmt3.execute();
            } else if (select.equalsIgnoreCase("c")) {
                String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE " + DeleteMessagesParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
                preStmt3 = con.prepareStatement(sql);
                preStmt3.setString(1, id);
                preStmt3.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(preStmt3);
        }
    }

    public void deleteMessage(String select, Long id) {
        deleteMessage(select, Long.toString(id));
    }

    public void addLogging(ArrayList<LoggingRecord> records) {
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

    public ArrayList<LoggingRecord> getLogging(String select, String id) {
        this.open();
        prestmt = null;
        ArrayList<LoggingRecord> records = new ArrayList<>();
        switch (select) {
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

    public ArrayList<ServerDataRecord> getNoSlashCommandServer() {
        this.open();
        ArrayList<ServerDataRecord> servers = new ArrayList<>();
        prestmt = null;
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE PREFIX = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, ">");
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                ServerDataRecord dataList = new ServerDataRecord();
                dataList.setServerId(rs.getString(ServerPropertyParameters.SERVER_ID.getParameter()));
                dataList.setMentionChannelId(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter()));
                dataList.setFstChannelId(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter()));
                dataList.setVoiceCategoryId(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter()));
                dataList.setTextCategoryId(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter()));
                dataList.setTempBy(rs.getBoolean(ServerPropertyParameters.TEMP_BY.getParameter()));
                dataList.setTextBy(rs.getBoolean(ServerPropertyParameters.TEXT_BY.getParameter()));
                dataList.setStereoTyped(rs.getString((ServerPropertyParameters.STEREOTYPED.getParameter())));
                dataList.setDefaultSize(rs.getString(ServerPropertyParameters.DEFAULT_SIZE.getParameter()));
                dataList.setDefaultName(rs.getString(ServerPropertyParameters.DEFAULT_NAME.getParameter()));
                servers.add(dataList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return servers;
    }

    public boolean getNoSlashCommandServer(String serverId) {
        this.open();
        prestmt = null;
        boolean nos = false;
        try {
            String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE PREFIX = ? AND " + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?) AS NO_CHECK";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, ">");
            prestmt.setString(2, serverId);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("NO_CHECK") == 1) {
                    nos = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return nos;
    }

    public void addBotSendMessage(BotSendMessageRecord record) {
        this.open();
        try {
            prestmt = null;
            String sql = "INSERT INTO " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " VALUES (?,?,?,?)";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServetId());
            prestmt.setString(2, record.getMessageId());
            prestmt.setString(3, record.getChannelId());
            prestmt.setString(4, record.getUserId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public BotSendMessageRecord getBotSendMessage(String MessageID) {
        BotSendMessageRecord record = new BotSendMessageRecord();
        record.setMessageId("NULL");
        this.open();
        prestmt = null;
        try {
            String sql = "SELECT EXISTS(SELECT * FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE " + BotSendMessageParameters.MESSAGE_ID.getParameter() + " = ?) AS MESSAGE_CHECK";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, MessageID);
            ResultSet rs = prestmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("MESSAGE_CHECK") == 1) {
                    sql = "SELECT * FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE " + BotSendMessageParameters.MESSAGE_ID.getParameter() + " = ?";
                    prestmt = con.prepareStatement(sql);
                    prestmt.setString(1, MessageID);
                    ResultSet resultSet = prestmt.executeQuery();
                    while (resultSet.next()) {
                        record.setServetId(resultSet.getString(BotSendMessageParameters.SERVER_ID.getParameter()));
                        record.setChannelId(resultSet.getString(BotSendMessageParameters.CHANNEL_ID.getParameter()));
                        record.setMessageId(resultSet.getString(BotSendMessageParameters.MESSAGE_ID.getParameter()));
                        record.setUserId(resultSet.getString(BotSendMessageParameters.USER_ID.getParameter()));

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
        return record;
    }

    public void deleteBotSendMessage(String select, String Id) {
        this.open();
        prestmt = null;
        try {
            String sql = null;
            switch (select) {
                case "channel" -> sql = "DELETE FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE " + BotSendMessageParameters.CHANNEL_ID.getParameter() + " = ?";
                case "message" -> sql = "DELETE FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE " + BotSendMessageParameters.MESSAGE_ID.getParameter() + " = ?";
            }
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Id);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public void deleteMentionMessage(String select, String Id) {
        this.open();
        prestmt = null;
        try {
            String sql = null;
            switch (select) {
                case "channel" -> sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
                case "message" -> sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE " + MentionMessageParameters.MESSAGE_ID.getParameter() + " = ?";
            }
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, Id);
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }
}