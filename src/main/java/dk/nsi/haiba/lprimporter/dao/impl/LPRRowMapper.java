package dk.nsi.haiba.lprimporter.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.model.lpr.Administration;

class LPRRowMapper implements RowMapper<Administration> {
	@Override
	public Administration mapRow(ResultSet rs, int rowNum) throws SQLException {
		
			Administration adm = new Administration();
			
			adm.setCpr(rs.getString("cpr"));
			
			// TODO implement all 
			return adm;
	}
}
