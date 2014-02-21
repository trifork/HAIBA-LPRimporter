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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;

public class ClassificationCheckDAOImpl extends CommonDAO implements ClassificationCheckDAO {
    private static Log log = new Log(Logger.getLogger(ClassificationCheckDAOImpl.class));
    private JdbcTemplate aClassificationJdbc;

    @Value("${jdbc.classificationtableprefix:}")
    String tableprefix;

    public ClassificationCheckDAOImpl(JdbcTemplate classificationJdbc) {
        aClassificationJdbc = classificationJdbc;
    }

    private boolean rowExists(CheckStructure checkStructure, String tableName) {
        String sql = null;
        String secondaryQualifier = "=?";
        String secondaryCode = checkStructure.getSecondaryCode();
        if (secondaryCode == null) {
            secondaryQualifier = " IS NULL";
        }
        if (MYSQL.equals(getDialect())) {
            sql = "SELECT * FROM " + tableName + " WHERE " + checkStructure.getCodeClassificationColumnName()
                    + "=? AND " + checkStructure.getSecondaryCodeClasificationColumnName() + secondaryQualifier
                    + " LIMIT 1";
        } else {
            // MSSQL
            sql = "SELECT TOP 1 * FROM " + tableprefix + tableName + " WHERE "
                    + checkStructure.getCodeClassificationColumnName() + "=? AND "
                    + checkStructure.getSecondaryCodeClasificationColumnName() + secondaryQualifier;
        }

        try {
            Object[] objects = null;
            if (secondaryCode != null) {
                objects = new Object[] { checkStructure.getCode(), secondaryCode };
            } else {
                objects = new Object[] { checkStructure.getCode() };
            }
            SqlRowSet queryForRowSet = aClassificationJdbc.queryForRowSet(sql, objects);
            if (queryForRowSet.first()) {
                return true;
            } else {
                return false;
            }
        } catch (EmptyResultDataAccessException e) {
            // ignore - does not exist
        } catch (RuntimeException e) {
            throw new DAOException("Error testing existence of " + checkStructure, e);
        }
        return false;
    }

    @Override
    public Collection<CheckStructure> checkClassifications(Collection<CheckStructure> checkStructures) {
        Set<CheckStructure> returnValue = new HashSet<CheckStructure>();
        for (CheckStructure checkStructure : checkStructures) {
            if (!rowExists(checkStructure, checkStructure.getClassificationTableName())) {
                returnValue.add(checkStructure);
            }
        }
        return returnValue;
    }
    
    @Override
    public void storeClassifications(Collection<CheckStructure> checkStructures) {
        if (!checkStructures.isEmpty()) {
            for (CheckStructure unknownStructure : checkStructures) {
                String sql = "INSERT INTO " + tableprefix + unknownStructure.getClassificationTableName() + "("
                        + unknownStructure.getCodeClassificationColumnName() + ","
                        + unknownStructure.getSecondaryCodeClasificationColumnName() + ") VALUES (?,?)";
                log.debug("checkClassifications: insert sql=" + sql);
                aClassificationJdbc.update(sql, unknownStructure.getCode(), unknownStructure.getSecondaryCode());
            }
        }
    }
}
