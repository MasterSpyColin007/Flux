package com.example.flux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LegacyPostSchemaRepair implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(LegacyPostSchemaRepair.class);

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public LegacyPostSchemaRepair(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			if (hasColumn("posts", "createdAt")) {
				jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN createdAt timestamp NULL");
				logger.info("Relaxed legacy posts.createdAt column so posts can be written to created_at.");
			}
		} catch (DataAccessException | SQLException ex) {
			logger.warn("Could not repair legacy posts.createdAt column.", ex);
		}
	}

	private boolean hasColumn(String tableName, String columnName) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			return hasColumn(metaData, tableName, columnName)
				|| hasColumn(metaData, tableName.toUpperCase(), columnName)
				|| hasColumn(metaData, tableName.toLowerCase(), columnName);
		}
	}

	private boolean hasColumn(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
		try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
			return columns.next();
		}
	}
}
