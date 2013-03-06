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
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.model.lpr.Administration;

class LPRContactRowMapper extends LPRRowMapper implements RowMapper<Administration> {
	
	@Override
	public Administration mapRow(ResultSet rs, int rowNum) throws SQLException {
		
			Administration adm = new Administration();
			
			adm.setRecordNumber(rs.getLong("k_recnum"));
			adm.setSygehusCode(rs.getString("c_sgh"));
			adm.setAfdelingsCode(rs.getString("c_afd"));
			adm.setCpr(rs.getString("v_cpr"));
			adm.setPatientType(rs.getInt("c_pattype"));

			Date in = rs.getDate("d_inddto");
			int inHour = rs.getInt("v_indtime");
			adm.setIndlaeggelsesDatetime(addHoursToDate(in,  inHour));
			
			Date out = rs.getDate("d_uddto");
			int outHour = rs.getInt("v_udtime");
			adm.setUdskrivningsDatetime(addHoursToDate(out,  outHour));
			return adm;
	}
}
