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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.status.ImportStatus.Outcome;

public class LPRDAOComposite implements LPRDAO {
    private static Log log = new Log(Logger.getLogger(LPRDAOComposite.class));
    public static final int SSI_DB = 1;
    public static final int MINIPAS_DB = 2;

    @Autowired
    @Qualifier(value = "ssiLPRDAO")
    private LPRDAO ssiLPRDAO;

    @Autowired
    @Qualifier(value = "minipasLPRDAO")
    private LPRDAO minipasLPRDAO;

    @Override
    public List<String> getCPRnumbersFromDeletedContacts(long syncId) throws DAOException {
        log.error("getCPRnumbersFromDeletedContacts(syncId) not supported");
        return null;
    }

    public static String getDbIdText(int dbId) {
        String returnValue = "NA";
        if (dbId == SSI_DB) {
            returnValue = "LPR";
        } else if (dbId == MINIPAS_DB) {
            returnValue = "MINIPAS";
        }
        return returnValue;
    }

    @Override
    public List<String> getCPRnumberBatch(int batchsize) throws DAOException {
        List<String> returnValue = ssiLPRDAO.getCPRnumberBatch(batchsize);
        if (batchsize > returnValue.size()) {
            returnValue.addAll(minipasLPRDAO.getCPRnumberBatch(batchsize - returnValue.size()));
        }
        return returnValue;
    }

    @Override
    public List<Administration> getContactsByCPR(String CPR) throws DAOException {
        List<Administration> returnValue = new ArrayList<Administration>();
        List<Administration> contactsByCPR = ssiLPRDAO.getContactsByCPR(CPR);
        for (Administration administration : contactsByCPR) {
            administration.setLprReference(new LPRReference(SSI_DB, administration.getRecordNumber()));
        }
        returnValue.addAll(contactsByCPR);
        contactsByCPR = minipasLPRDAO.getContactsByCPR(CPR);
        for (Administration administration : contactsByCPR) {
            administration.setLprReference(new LPRReference(MINIPAS_DB, administration.getRecordNumber()));
        }
        returnValue.addAll(contactsByCPR);
        return returnValue;
    }

    @Override
    public void updateImportTime(LPRReference lprReference, Outcome outcome) {
        if (lprReference.getDbId() == SSI_DB) {
            ssiLPRDAO.updateImportTime(lprReference, outcome);
        } else if (lprReference.getDbId() == MINIPAS_DB) {
            minipasLPRDAO.updateImportTime(lprReference, outcome);
        }
    }

    @Override
    public boolean hasUnprocessedCPRnumbers() {
        boolean returnValue = ssiLPRDAO.hasUnprocessedCPRnumbers() || minipasLPRDAO.hasUnprocessedCPRnumbers();
        return returnValue;
    }

    /**
     * returns 0 if either database is not ready or 1 if both are ready. this is not the proper sync id to be used for
     * further queries
     */
    @Override
    public long isdatabaseReadyForImport() {
        long isdatabaseReadyForImport = ssiLPRDAO.isdatabaseReadyForImport();
        if (isdatabaseReadyForImport != 0) {
            isdatabaseReadyForImport = minipasLPRDAO.isdatabaseReadyForImport() != 0 ? 1 : 0;
        }
        return isdatabaseReadyForImport;
    }

    @Override
    public List<String> getCPRnumbersFromDeletedContacts() throws DAOException {
        Set<String> returnValue = new HashSet<String>();
        returnValue.addAll(minipasLPRDAO.getCPRnumbersFromDeletedContacts());
        returnValue.addAll(ssiLPRDAO.getCPRnumbersFromDeletedContacts());
        return new ArrayList<String>(returnValue);
    }
}
