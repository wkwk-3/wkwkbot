package wkwk.dao;

import wkwk.parameter.*;
import wkwk.parameter.record.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ReloadDAO extends DAOBase {

    public ArrayList<BotSendMessageRecord> getAllBotSendMessage() {
        this.open();
        Statement stmt = null;
        ArrayList<BotSendMessageRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                BotSendMessageRecord record = new BotSendMessageRecord();
                record.setServetId(rs.getString(BotSendMessageParameters.SERVER_ID.getParameter()));
                record.setChannelId(rs.getString(BotSendMessageParameters.CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(BotSendMessageParameters.MESSAGE_ID.getParameter()));
                record.setUserId(rs.getString(BotSendMessageParameters.USER_ID.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteBotSendMessage(BotSendMessageRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_BOT_SEND_MESSAGES.getParameter() + " WHERE "
                    + BotSendMessageParameters.SERVER_ID.getParameter() + " = ? AND "
                    + BotSendMessageParameters.MESSAGE_ID.getParameter() + " = ? AND "
                    + BotSendMessageParameters.CHANNEL_ID.getParameter() + " = ? AND "
                    + BotSendMessageParameters.USER_ID.getParameter() + " = ?";
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

    public ArrayList<DeleteMessageRecord> getAllDeleteMessage() {
        this.open();
        Statement stmt = null;
        ArrayList<DeleteMessageRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                DeleteMessageRecord record = new DeleteMessageRecord();
                record.setServerId(rs.getString(DeleteMessagesParameters.SERVER_ID.getParameter()));
                record.setChannelId(rs.getString(DeleteMessagesParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(DeleteMessagesParameters.MESSAGE_ID.getParameter()));
                record.setDeleteTime(rs.getString(DeleteMessagesParameters.DELETE_TIME.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteDeleteMessage(DeleteMessageRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_MESSAGES.getParameter() + " WHERE "
                    + DeleteMessagesParameters.SERVER_ID.getParameter() + " = ? AND "
                    + DeleteMessagesParameters.MESSAGE_ID.getParameter() + " = ? AND "
                    + DeleteMessagesParameters.TEXT_CHANNEL_ID.getParameter() + " = ? AND "
                    + DeleteMessagesParameters.DELETE_TIME.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getMessageId());
            prestmt.setString(3, record.getChannelId());
            prestmt.setString(4, record.getDeleteTime());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<DeleteTimeRecord> getAllDeleteTime() {
        this.open();
        Statement stmt = null;
        ArrayList<DeleteTimeRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                DeleteTimeRecord record = new DeleteTimeRecord();
                record.setServerId(rs.getString(DeleteTimesParameters.SERVER_ID.getParameter()));
                record.setTextChannelId(rs.getString(DeleteTimesParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setDeleteTime(rs.getInt(DeleteTimesParameters.DELETE_TIME.getParameter()));
                record.setTimeUnit(rs.getString(DeleteTimesParameters.TIME_UNIT.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteDeleteTime(DeleteTimeRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_DELETE_TIMES.getParameter() + " WHERE "
                    + DeleteTimesParameters.SERVER_ID.getParameter() + " = ? AND "
                    + DeleteTimesParameters.TEXT_CHANNEL_ID.getParameter() + " = ? AND "
                    + DeleteTimesParameters.DELETE_TIME.getParameter() + " = ? AND "
                    + DeleteTimesParameters.TIME_UNIT.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getTextChannelId());
            prestmt.setInt(3, record.getDeleteTime());
            prestmt.setString(4, record.getTimeUnit());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<LoggingRecord> getAllLogging() {
        this.open();
        Statement stmt = null;
        ArrayList<LoggingRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_LOGGING.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LoggingRecord record = new LoggingRecord();
                record.setServerId(rs.getString(LoggingParameters.SERVER_ID.getParameter()));
                record.setChannelId(rs.getString(LoggingParameters.CHANNEL_ID.getParameter()));
                record.setTargetChannelId(rs.getString(LoggingParameters.TARGET_CHANNEL_ID.getParameter()));
                record.setLogType(rs.getString(LoggingParameters.LOG_TYPE.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteLogging(LoggingRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_LOGGING.getParameter() + " WHERE "
                    + LoggingParameters.SERVER_ID.getParameter() + " = ? AND "
                    + LoggingParameters.CHANNEL_ID.getParameter() + " = ? AND "
                    + LoggingParameters.TARGET_CHANNEL_ID.getParameter() + " = ? AND "
                    + LoggingParameters.LOG_TYPE.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getChannelId());
            prestmt.setString(3, record.getTargetChannelId());
            prestmt.setString(4, record.getLogType());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<MentionMessageRecord> getAllMentionMessage() {
        this.open();
        Statement stmt = null;
        ArrayList<MentionMessageRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MentionMessageRecord record = new MentionMessageRecord();
                record.setServerId(rs.getString(MentionMessageParameters.SERVER_ID.getParameter()));
                record.setTextChannelId(rs.getString(MentionMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(MentionMessageParameters.MESSAGE_ID.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteMentionMessage(MentionMessageRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_MENTION_MESSAGE.getParameter() + " WHERE "
                    + MentionMessageParameters.SERVER_ID.getParameter() + " = ? AND "
                    + MentionMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ? AND "
                    + MentionMessageParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getTextChannelId());
            prestmt.setString(3, record.getMessageId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<NamePresetRecord> getAllNamePreset() {
        this.open();
        Statement stmt = null;
        ArrayList<NamePresetRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                NamePresetRecord record = new NamePresetRecord();
                record.setServerId(rs.getString(NamePresetParameters.SERVER_ID.getParameter()));
                record.setName(rs.getString(NamePresetParameters.NAME.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteNamePreset(NamePresetRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_NAME_PRESET.getParameter() + " WHERE "
                    + NamePresetParameters.SERVER_ID.getParameter() + " = ? AND "
                    + NamePresetParameters.NAME.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getName());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<ReactMessageRecord> getAllReactMessage() {
        this.open();
        Statement stmt = null;
        ArrayList<ReactMessageRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ReactMessageRecord record = new ReactMessageRecord();
                record.setServerId(rs.getString(ReactMessageParameters.SERVER_ID.getParameter()));
                record.setTextChannelId(rs.getString(ReactMessageParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setMessageId(rs.getString(ReactMessageParameters.MESSAGE_ID.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteReactMessage(ReactMessageRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_REACT_MESSAGE.getParameter() + " WHERE "
                    + ReactMessageParameters.SERVER_ID.getParameter() + " = ? AND "
                    + ReactMessageParameters.TEXT_CHANNEL_ID.getParameter() + " = ? AND "
                    + ReactMessageParameters.MESSAGE_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getTextChannelId());
            prestmt.setString(3, record.getMessageId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<ReactRoleRecord> getAllReactRole() {
        this.open();
        Statement stmt = null;
        ArrayList<ReactRoleRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ReactRoleRecord record = new ReactRoleRecord();
                record.setServerId(rs.getString(ReactRoleParameters.SERVER_ID.getParameter()));
                record.setMessageId(rs.getString(ReactRoleParameters.MESSAGE_ID.getParameter()));
                record.setRoleId(rs.getString(ReactRoleParameters.ROLE_ID.getParameter()));
                record.setEmoji(rs.getString(ReactRoleParameters.EMOJI.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteReactRole(ReactRoleRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_REACT_ROLE.getParameter() + " WHERE "
                    + ReactRoleParameters.SERVER_ID.getParameter() + " = ? AND "
                    + ReactRoleParameters.MESSAGE_ID.getParameter() + " = ? AND "
                    + ReactRoleParameters.ROLE_ID.getParameter() + " = ? AND "
                    + ReactRoleParameters.EMOJI.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.setString(2, record.getMessageId());
            prestmt.setString(3, record.getRoleId());
            prestmt.setString(4, record.getEmoji());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<ServerDataRecord> getAllServerProperty() {
        this.open();
        Statement stmt = null;
        ArrayList<ServerDataRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ServerDataRecord record = new ServerDataRecord();
                record.setServerId(rs.getString(ServerPropertyParameters.SERVER_ID.getParameter()));
                record.setMentionChannelId(rs.getString(ServerPropertyParameters.MENTION_CHANNEL_ID.getParameter()));
                record.setFstChannelId(rs.getString(ServerPropertyParameters.FIRST_CHANNEL_ID.getParameter()));
                record.setVoiceCategoryId(rs.getString(ServerPropertyParameters.VOICE_CATEGORY_ID.getParameter()));
                record.setTextCategoryId(rs.getString(ServerPropertyParameters.TEXT_CATEGORY_ID.getParameter()));
                record.setTempBy(rs.getBoolean(ServerPropertyParameters.TEMP_BY.getParameter()));
                record.setTextBy(rs.getBoolean(ServerPropertyParameters.TEXT_BY.getParameter()));
                record.setDefaultSize(rs.getString(ServerPropertyParameters.DEFAULT_SIZE.getParameter()));
                record.setStereoTyped(rs.getString(ServerPropertyParameters.STEREOTYPED.getParameter()));
                record.setDefaultName(rs.getString(ServerPropertyParameters.DEFAULT_NAME.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteServerProperty(ServerDataRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_SERVER_PROPERTY.getParameter() + " WHERE "
                    + ServerPropertyParameters.SERVER_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getServerId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }

    public ArrayList<ChannelRecord> getAllTempChannel() {
        this.open();
        Statement stmt = null;
        ArrayList<ChannelRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ChannelRecord record = new ChannelRecord();
                record.setServerId(rs.getString(TempChannelsParameters.SERVER_ID.getParameter()));
                record.setVoiceId(rs.getString(TempChannelsParameters.VOICE_CHANNEL_ID.getParameter()));
                record.setTextId(rs.getString(TempChannelsParameters.TEXT_CHANNEL_ID.getParameter()));
                record.setHideBy(rs.getBoolean(TempChannelsParameters.HIDE_BY.getParameter()));
                record.setLockBy(rs.getBoolean(TempChannelsParameters.LOCK_BY.getParameter()));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(stmt);
        }
        return records;
    }

    public void deleteTempChannel(ChannelRecord record) {
        this.open();
        prestmt = null;
        try {
            String sql = "DELETE FROM " + DAOParameters.TABLE_TEMP_CHANNEL.getParameter() + " WHERE "
                    + TempChannelsParameters.VOICE_CHANNEL_ID.getParameter() + " = ? AND "
                    + TempChannelsParameters.TEXT_CHANNEL_ID.getParameter() + " = ?";
            prestmt = con.prepareStatement(sql);
            prestmt.setString(1, record.getVoiceId());
            prestmt.setString(2, record.getTextId());
            prestmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(prestmt);
        }
    }
}
