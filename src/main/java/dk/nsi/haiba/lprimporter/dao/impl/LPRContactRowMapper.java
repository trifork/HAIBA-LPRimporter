/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.haiba.lprimporter.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

class LPRContactRowMapper implements RowMapper<Administration> {
	
	private static Log log = new Log(Logger.getLogger(LPRContactRowMapper.class));

	private static final String DIAGNOSIS = "DIA";
	private static final String PROCEDURE = "PRO";
	
	@Override
	public Administration mapRow(ResultSet rs, int rowNum) throws SQLException {
		
			Administration adm = new Administration();
			
			adm.setRecordNumber(rs.getLong("v_recnum"));
			adm.setSygehusCode(rs.getString("c_sgh"));
			adm.setAfdelingsCode(rs.getString("c_afd"));
			adm.setCpr(rs.getString("v_cpr"));
			adm.setPatientType(rs.getInt("c_pattype"));
			Timestamp tsIn = rs.getTimestamp("d_inddto");
			if(tsIn != null) {
				adm.setIndlaeggelsesDatetime(new Date(tsIn.getTime()));
			}
			Timestamp tsOut = rs.getTimestamp("d_uddto");
			if(tsOut != null) {
				adm.setUdskrivningsDatetime(new Date(tsOut.getTime()));
			}
			
			String type = rs.getString("v_type");
			if(type != null) {
				if(DIAGNOSIS.equalsIgnoreCase(type)) {
					LPRDiagnose d = new LPRDiagnose();
					d.setRecordNumber(rs.getLong("v_recnum"));
					d.setDiagnoseCode(rs.getString("c_kode"));
					d.setTillaegsDiagnose(rs.getString("c_tilkode"));
					d.setDiagnoseType(rs.getString("c_kodeart"));
					adm.addLprDiagnose(d);
				} else if(PROCEDURE.equalsIgnoreCase(type)) {
					LPRProcedure p = new LPRProcedure();
					
					p.setRecordNumber(rs.getLong("v_recnum"));
					p.setProcedureCode(rs.getString("c_kode"));
					p.setTillaegsProcedureCode(rs.getString("c_tilkode"));
					p.setProcedureType(rs.getString("c_kodeart"));
					p.setSygehusCode(rs.getString("c_psgh"));
					p.setAfdelingsCode(rs.getString("c_pafd"));
					Timestamp ts = rs.getTimestamp("d_pdto");
					if(ts != null) {
						p.setProcedureDatetime(new Date(ts.getTime()));
					}
					adm.addLprProcedure(p);
				} else {
					log.error("Unknown type for Diagnosis or Procedure ["+type+"]");
				}
			}
			return adm;
	}
}
