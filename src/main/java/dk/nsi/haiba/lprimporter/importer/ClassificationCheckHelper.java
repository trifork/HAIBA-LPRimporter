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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO.CheckStructure;
import dk.nsi.haiba.lprimporter.email.EmailSender;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.Diagnose;
import dk.nsi.haiba.lprimporter.model.haiba.Indlaeggelse;
import dk.nsi.haiba.lprimporter.model.haiba.Procedure;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.model.lpr.LPRDiagnose;
import dk.nsi.haiba.lprimporter.model.lpr.LPRProcedure;

public class ClassificationCheckHelper {
    private static Log log = new Log(Logger.getLogger(ClassificationCheckHelper.class));

    @Autowired
    ClassificationCheckDAO classificationCheckDAO;

    @Autowired
    EmailSender emailSender;

    private void check(Collection<Wrapper> wrappers) {
        Set<CheckStructure> sygehusCheckStructures = new HashSet<ClassificationCheckDAO.CheckStructure>();
        Set<CheckStructure> diagnoseCheckStructures = new HashSet<ClassificationCheckDAO.CheckStructure>();
        Set<CheckStructure> procedureCheckStructures = new HashSet<ClassificationCheckDAO.CheckStructure>();

        for (Wrapper wrapper : wrappers) {
            String sygehusCode = wrapper.getSygehusCode();
            String afdelingsCode = wrapper.getAfdelingsCode();

            CheckStructureImpl csi = new CheckStructureImpl(sygehusCode, afdelingsCode, "sygehuskode", "afdelingskode",
                    "Anvendt_Klass_SHAK");
            sygehusCheckStructures.add(csi);

            List<Codes> lprDiagnoses = wrapper.getDiagnoseCodes();
            for (Codes lprDiagnose : lprDiagnoses) {
                String diagnoseCode = lprDiagnose.aCode;
                String tillaegsDiagnose = lprDiagnose.aSecondaryCode;
                csi = new CheckStructureImpl(diagnoseCode, tillaegsDiagnose, "Diagnoseskode", "tillaegskode",
                        "Anvendt_klass_diagnoser");
                diagnoseCheckStructures.add(csi);
            }
            List<Codes> lprProcedures = wrapper.getProcedureCodes();
            for (Codes lprProcedure : lprProcedures) {
                String procedureCode = lprProcedure.aCode;
                String tillaegsProcedureCode = lprProcedure.aSecondaryCode;
                csi = new CheckStructureImpl(procedureCode, tillaegsProcedureCode, "procedurekode", "tillaegskode",
                        "Anvendt_klass_procedurer");
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
            // now the email is away. if we should die now, worst case is that this information would be send again on
            // the next run. better this than storing, then die and never make the notification
            classificationCheckDAO.storeClassifications(newSygehusClassifications);
            classificationCheckDAO.storeClassifications(newProcedureCheckClassifications);
            classificationCheckDAO.storeClassifications(newDiagnoseCheckClassifications);
        }
    }

    public void checkClassifications(Administration[] array) {
        check(wrap(array));
    }

    public void checkClassifications(Indlaeggelse[] array) {
        check(wrap(array));
    }

    private Collection<Wrapper> wrap(Administration[] admissions) {
        Collection<Wrapper> returnValue = new ArrayList<ClassificationCheckHelper.Wrapper>();
        for (Administration administration : admissions) {
            returnValue.add(new Wrapper(administration));
        }
        return returnValue;
    }

    private Collection<Wrapper> wrap(Indlaeggelse[] indlaeggelser) {
        Collection<Wrapper> returnValue = new ArrayList<ClassificationCheckHelper.Wrapper>();
        for (Indlaeggelse indlaeggelse : indlaeggelser) {
            returnValue.add(new Wrapper(indlaeggelse));
        }
        return returnValue;
    }

    public static class CheckStructureImpl implements CheckStructure {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((aCode == null) ? 0 : aCode.hashCode());
            result = prime * result + ((aSecondaryCode == null) ? 0 : aSecondaryCode.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CheckStructureImpl other = (CheckStructureImpl) obj;
            if (aCode == null) {
                if (other.aCode != null)
                    return false;
            } else if (!aCode.equals(other.aCode))
                return false;
            if (aSecondaryCode == null) {
                if (other.aSecondaryCode != null)
                    return false;
            } else if (!aSecondaryCode.equals(other.aSecondaryCode))
                return false;
            return true;
        }

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

    public static class Wrapper {
        private Administration aAdministration;
        private Indlaeggelse aIndlaeggelse;

        public Wrapper(Indlaeggelse indlaeggelse) {
            aIndlaeggelse = indlaeggelse;
        }

        public List<Codes> getProcedureCodes() {
            List<Codes> returnValue = null;
            if (aAdministration != null) {
                returnValue = wrap(aAdministration.getLprProcedures().toArray(new LPRProcedure[0]));
            } else {
                returnValue = wrap(aIndlaeggelse.getProcedures().toArray(new Procedure[0]));
            }
            return returnValue;
        }

        private List<Codes> wrap(Procedure[] array) {
            List<Codes> returnValue = new ArrayList<ClassificationCheckHelper.Codes>();
            for (Procedure procedure : array) {
                returnValue.add(new Codes(procedure.getProcedureCode(), procedure.getTillaegsProcedureCode()));
            }
            return returnValue;
        }

        private List<Codes> wrap(LPRProcedure[] array) {
            List<Codes> returnValue = new ArrayList<ClassificationCheckHelper.Codes>();
            for (LPRProcedure procedure : array) {
                returnValue.add(new Codes(procedure.getProcedureCode(), procedure.getTillaegsProcedureCode()));
            }
            return returnValue;
        }

        public List<Codes> getDiagnoseCodes() {
            List<Codes> returnValue = null;
            if (aAdministration != null) {
                returnValue = wrap(aAdministration.getLprDiagnoses().toArray(new LPRDiagnose[0]));
            } else {
                returnValue = wrap(aIndlaeggelse.getDiagnoses().toArray(new Diagnose[0]));
            }
            return returnValue;
        }

        private List<Codes> wrap(Diagnose[] array) {
            List<Codes> returnValue = new ArrayList<ClassificationCheckHelper.Codes>();
            for (Diagnose diagnose : array) {
                returnValue.add(new Codes(diagnose.getDiagnoseCode(), diagnose.getTillaegsDiagnose()));
            }
            return returnValue;
        }

        private List<Codes> wrap(LPRDiagnose[] array) {
            List<Codes> returnValue = new ArrayList<ClassificationCheckHelper.Codes>();
            for (LPRDiagnose diagnose : array) {
                returnValue.add(new Codes(diagnose.getDiagnoseCode(), diagnose.getTillaegsDiagnose()));
            }
            return returnValue;
        }

        public Wrapper(Administration administration) {
            aAdministration = administration;
        }

        public String getAfdelingsCode() {
            String returnValue = null;
            if (aAdministration != null) {
                returnValue = aAdministration.getAfdelingsCode();
            } else {
                returnValue = aIndlaeggelse.getAfdelingsCode();
            }
            return returnValue;
        }

        String getSygehusCode() {
            String returnValue = null;
            if (aAdministration != null) {
                returnValue = aAdministration.getSygehusCode();
            } else {
                returnValue = aIndlaeggelse.getSygehusCode();
            }
            return returnValue;
        }
    }

    public static class Codes {
        private String aCode;
        private String aSecondaryCode;

        public Codes(String code, String secondary) {
            aCode = code;
            aSecondaryCode = secondary;
        }
    }
}
