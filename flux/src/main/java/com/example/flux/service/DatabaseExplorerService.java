package com.example.flux.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DatabaseExplorerService {

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public DatabaseExplorerService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<String> listTableNames() {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			String schema = connection.getSchema();

			try (ResultSet tables = metaData.getTables(catalog, schema, "%", new String[] { "TABLE" })) {
				return readTableNames(tables);
			}
		} catch (SQLException ex) {
			throw new IllegalStateException("Unable to read database table metadata.", ex);
		}
	}

	public List<TableSummary> listTableSummaries() {
		return listTableNames().stream()
			.map(tableName -> new TableSummary(tableName, countRows(tableName)))
			.toList();
	}

	public List<Map<String, Object>> getRows(String tableName) {
		String matchedTable = requireKnownTable(tableName);
		return jdbcTemplate.queryForList("select * from " + quoteIdentifier(matchedTable));
	}

	public Map<String, List<Map<String, Object>>> getAllRows() {
		Map<String, List<Map<String, Object>>> database = new LinkedHashMap<>();
		for (String tableName : listTableNames()) {
			database.put(tableName, getRows(tableName));
		}
		return database;
	}

	private long countRows(String tableName) {
		Long count = jdbcTemplate.queryForObject(
			"select count(*) from " + quoteIdentifier(requireKnownTable(tableName)),
			Long.class
		);
		return count == null ? 0 : count;
	}

	private String requireKnownTable(String tableName) {
		String requested = tableName.toLowerCase(Locale.ROOT);
		return listTableNames().stream()
			.filter(knownTable -> knownTable.toLowerCase(Locale.ROOT).equals(requested))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown database table: " + tableName));
	}

	private String quoteIdentifier(String identifier) {
		try (Connection connection = dataSource.getConnection()) {
			String quote = connection.getMetaData().getIdentifierQuoteString();
			if (quote == null || quote.isBlank()) {
				return identifier;
			}
			return quote + identifier.replace(quote, quote + quote) + quote;
		} catch (SQLException ex) {
			throw new IllegalStateException("Unable to quote database identifier.", ex);
		}
	}

	private List<String> readTableNames(ResultSet tables) throws SQLException {
		java.util.ArrayList<String> tableNames = new java.util.ArrayList<>();
		while (tables.next()) {
			tableNames.add(tables.getString("TABLE_NAME"));
		}
		tableNames.sort(String.CASE_INSENSITIVE_ORDER);
		return tableNames;
	}

	public record TableSummary(String name, long rowCount) {
	}
}
