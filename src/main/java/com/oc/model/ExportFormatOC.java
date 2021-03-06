package com.oc.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExportFormatOC extends AbstractOcModel{

	private Long id;

	private String name;

	private String description;

	private String mime_type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMime_type() {
		return mime_type;
	}

	public void setMime_type(String mime_type) {
		this.mime_type = mime_type;
	}

	public void generate(ResultSet rs) throws SQLException {
		setId(rs.getLong(1));
		setName(rs.getString(2));
		setDescription(rs.getString(3));
		setMime_type(rs.getString(4));
	}

	public static ExportFormatOC getEF(ResultSet rs) throws SQLException {
		ExportFormatOC r = new ExportFormatOC();
		r.generate(rs);
		return r;
	}
	
	public String getQueryInsert() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO export_format values(");
		sb.append(id);
		sb.append(", '");
		sb.append(name);
		sb.append("',");
		sb.append("'");
		sb.append(description);
		sb.append("',");
		sb.append("'");
		sb.append(mime_type);
		sb.append("'");
		sb.append(")");
		return sb.toString();
	}
}
