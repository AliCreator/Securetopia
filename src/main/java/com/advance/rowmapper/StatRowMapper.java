package com.advance.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.advance.domain.Stats;

public class StatRowMapper implements RowMapper<Stats> {

	@Override
	public Stats mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Stats.builder().totalCustomer(rs.getInt("total_customers")).totalInvoices(rs.getInt("total_invoices"))
				.totalBilled(rs.getDouble("total_billed")).build();
	}

}
