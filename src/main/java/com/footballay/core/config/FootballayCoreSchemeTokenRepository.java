package com.footballay.core.config;

import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.core.log.LogMessage;

import java.util.Date;

/**
 * 더 이상 footballay_core 스키마를 사용하지 않으므로 deprecated 된 클래스
 */
public class FootballayCoreSchemeTokenRepository extends JdbcTokenRepositoryImpl {

    // if you need to use schema, uncomment below lines and comment out TABLE definition
    // private static final String SCHEMA = "footballay_core";
    // private static final String TABLE = SCHEMA + ".persistent_logins";

    private static final String TABLE = "persistent_logins";

    private static final String INSERT_SQL =
            "insert into " + TABLE + " (username, series, token, last_used) values(?,?,?,?)";
    private static final String UPDATE_SQL =
            "update " + TABLE + " set token = ?, last_used = ? where series = ?";
    private static final String SELECT_BY_SERIES_SQL =
            "select username,series,token,last_used from " + TABLE + " where series = ?";
    private static final String DELETE_BY_USERNAME_SQL =
            "delete from " + TABLE + " where username = ?";

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        getJdbcTemplate().update(INSERT_SQL, token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        getJdbcTemplate().update(UPDATE_SQL, tokenValue, lastUsed, series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return getJdbcTemplate().queryForObject(SELECT_BY_SERIES_SQL,
                    (rs, rowNum) -> new PersistentRememberMeToken(
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4)),
                    seriesId);
        } catch (EmptyResultDataAccessException ex) {
            this.logger.debug(LogMessage.format("Querying token for series '%s' returned no results.", seriesId), ex);
        } catch (IncorrectResultSizeDataAccessException ex) {
            this.logger.error(LogMessage.format(
                    "Querying token for series '%s' returned more than one value. Series should be unique",
                    seriesId));
        } catch (DataAccessException ex) {
            this.logger.error("Failed to load token for series " + seriesId, ex);
        }
        return null;
    }

    @Override
    public void removeUserTokens(String username) {
        getJdbcTemplate().update(DELETE_BY_USERNAME_SQL, username);
    }
}
