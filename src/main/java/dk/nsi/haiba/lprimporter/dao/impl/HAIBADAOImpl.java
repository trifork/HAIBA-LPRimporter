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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;

public class HAIBADAOImpl implements HAIBADAO {

	@Autowired
	@Qualifier("haibaJdbcTemplate")
	JdbcTemplate jdbc;

	// TODO - select SQL from the chosen dialect

	@Override
	public void saveIndlaeggelsesForloeb(List<Indlaeggelse> indlaeggelser) throws DAOException {

		for (Indlaeggelse indlaeggelse : indlaeggelser) {
			
			final String sql = "INSERT INTO Indlaeggelser (CPR, Sygehuskode, Afdelingskode, Indlaeggelsesdato, Indlaeggelsestidspunkt, Udskrivningsdato, Udskrivningstidspunkt) "
					+"VALUES (?,?,?,?,?,?,?)";
			final Object[] args = new Object[] {
					indlaeggelse.getCpr(), 
					indlaeggelse.getSygehusCode(),
					indlaeggelse.getAfdelingsCode(),
					"2010-12-13" /* TODO */,
					"0" /* TODO*/,
					"2010-12-14" /* TODO */,
					"0" /* TODO*/};
			
			KeyHolder keyHolder = new GeneratedKeyHolder();

	        jdbc.update(new PreparedStatementCreator() {
	            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
	                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
	                for (int i = 0; i < args.length; i++) {
	                    ps.setObject(i + 1, args[i]);
	                }
	                return ps;
	            }
	        }, keyHolder);

	        int key = keyHolder.getKey().intValue();
	        System.out.println("Generated Key: "+key);
		}
		
		
	}

}
