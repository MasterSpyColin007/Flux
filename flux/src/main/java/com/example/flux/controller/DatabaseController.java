package com.example.flux.controller;

import com.example.flux.service.DatabaseExplorerService;
import com.example.flux.service.DatabaseExplorerService.TableSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

	private final DatabaseExplorerService databaseExplorerService;

	public DatabaseController(DatabaseExplorerService databaseExplorerService) {
		this.databaseExplorerService = databaseExplorerService;
	}

	@GetMapping
	public Map<String, List<Map<String, Object>>> getDatabase() {
		return databaseExplorerService.getAllRows();
	}

	@GetMapping("/tables")
	public List<TableSummary> getTables() {
		return databaseExplorerService.listTableSummaries();
	}

	@GetMapping("/tables/{tableName}")
	public List<Map<String, Object>> getTable(@PathVariable String tableName) {
		return databaseExplorerService.getRows(tableName);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
	}
}
