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
package dk.nsi.haiba.lprimporter.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO.CheckStructure;
import dk.nsi.haiba.lprimporter.email.EmailSender;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

public class ClassificationCheckHelper {
    private static Log log = new Log(Logger.getLogger(ClassificationCheckHelper.class));

    @Autowired
    ClassificationCheckDAO classificationCheckDAO;

    @Autowired
    EmailSender emailSender;

    public void checkClassifications(Collection<Administration> collection) {
        List<CheckStructure> sygehusCheckStructures = new ArrayList<ClassificationCheckDAO.CheckStructure>();
        List<CheckStructure> diagnoseCheckStructures = new ArrayList<ClassificationCheckDAO.CheckStructure>();
        List<CheckStructure> procedureCheckStructures = new ArrayList<ClassificationCheckDAO.CheckStructure>();

        for (Administration administration : collection) {
            String sygehusCode = administration.getSygehusCode();
            String afdelingsCode = administration.getAfdelingsCode();

            CheckStructureImpl csi = new CheckStructureImpl(sygehusCode, afdelingsCode, "sygehuskode", "afdelingskode",
                    "Klass_SHAK");
            sygehusCheckStructures.add(csi);

            List<LPRDiagnose> lprDiagnoses = administration.getLprDiagnoses();
            for (LPRDiagnose lprDiagnose : lprDiagnoses) {
                String diagnoseCode = lprDiagnose.getDiagnoseCode();
                String tillaegsDiagnose = lprDiagnose.getTillaegsDiagnose();
                csi = new CheckStructureImpl(diagnoseCode, tillaegsDiagnose, "Diagnoseskode", "tillaegskode",
                        "klass_diagnoser");
                diagnoseCheckStructures.add(csi);
            }
            List<LPRProcedure> lprProcedures = administration.getLprProcedures();
            for (LPRProcedure lprProcedure : lprProcedures) {
                String procedureCode = lprProcedure.getProcedureCode();
                String tillaegsProcedureCode = lprProcedure.getTillaegsProcedureCode();
                csi = new CheckStructureImpl(procedureCode, tillaegsProcedureCode, "procedurekode", "tillaegskode",
                        "klass_procedurer");
                procedureCheckStructures.add(csi);
            }
        }
        Collection<CheckStructure> newSygehusClassifications = classificationCheckDAO
                .checkClassifications(sygehusCheckStructures);
        Collection<CheckStructure> newProcedureCheckClassifications = classificationCheckDAO
                .checkClassifications(procedureCheckStructures);
        Collection<CheckStructure> newDiagnoseCheckClassifications = classificationCheckDAO
                .checkClassifications(diagnoseCheckStructures);

        if (!newSygehusClassifications.isEmpty() || !newProcedureCheckClassifications.isEmpty()
                || !newDiagnoseCheckClassifications.isEmpty()) {
            log.debug("send email about new sygehuse=" + newSygehusClassifications.size() + " or new procedure="
                    + newProcedureCheckClassifications.size() + " or new diagnose="
                    + newDiagnoseCheckClassifications.size());
            emailSender.send(newSygehusClassifications, newProcedureCheckClassifications,
                    newDiagnoseCheckClassifications);
        }
    }

    public void checkClassifications(List<Indlaeggelse> admissions) {
        // XXX
        log.error("not implemented");
    }

    public static class CheckStructureImpl implements CheckStructure {
        private String aCode;
        private String aSecondaryCode;
        private String aCodeClasificationColumnName;
        private String aSecondaryCodeClasificationColumnName;
        private String aClassificationTableName;

        public CheckStructureImpl(String code, String secondaryCode, String codeClasificationColumnName,
                String secondaryCodeClasificationColumnName, String classificationTableName) {
            aCode = code;
            aSecondaryCode = secondaryCode;
            aCodeClasificationColumnName = codeClasificationColumnName;
            aSecondaryCodeClasificationColumnName = secondaryCodeClasificationColumnName;
            aClassificationTableName = classificationTableName;
        }

        @Override
        public String getCode() {
            return aCode;
        }

        @Override
        public String getSecondaryCode() {
            return aSecondaryCode;
        }

        @Override
        public String getCodeClasificationColumnName() {
            return aCodeClasificationColumnName;
        }

        @Override
        public String getSecondaryCodeClasificationColumnName() {
            return aSecondaryCodeClasificationColumnName;
        }

        @Override
        public String getClassificationTableName() {
            return aClassificationTableName;
        }

        @Override
        public String toString() {
            return "CheckStructureImpl [aCode=" + aCode + ", aSecondaryCode=" + aSecondaryCode
                    + ", aCodeClasificationColumnName=" + aCodeClasificationColumnName
                    + ", aSecondaryCodeClasificationColumnName=" + aSecondaryCodeClasificationColumnName
                    + ", aClassificationTableName=" + aClassificationTableName + "]";
        }
    }
}
