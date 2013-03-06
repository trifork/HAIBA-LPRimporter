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
package dk.nsi.haiba.lprimporter.model.lpr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.nsi.haiba.lprimporter.model.haiba.LPRReference;

/*
 * Model object for a Contact to the healthsystem from the LPR datamodel
 * This Class matches the t_adm table
 */
public class Administration {
	
	long recordNumber;
	String cpr;
	String sygehusCode;
	String afdelingsCode;
	Date indlaeggelsesDatetime;
	Date udskrivningsDatetime;
	List<LPRDiagnose> lprDiagnoses = new ArrayList<LPRDiagnose>();
	List<LPRProcedure> lprProcedures = new ArrayList<LPRProcedure>();
	List<LPRReference> lprReferencer = new ArrayList<LPRReference>();
	boolean currentPatient = false;
	private int patientType;
	
	
	public long getRecordNumber() {
		return recordNumber;
	}
	public void setRecordNumber(long recordNumber) {
		this.recordNumber = recordNumber;
	}
	public String getCpr() {
		return cpr;
	}
	public void setCpr(String cpr) {
		this.cpr = cpr;
	}
	public String getSygehusCode() {
		return sygehusCode;
	}
	public void setSygehusCode(String sygehusCode) {
		this.sygehusCode = sygehusCode;
	}
	public String getAfdelingsCode() {
		return afdelingsCode;
	}
	public void setAfdelingsCode(String afdelingsCode) {
		this.afdelingsCode = afdelingsCode;
	}
	public boolean isCurrentPatient() {
		return currentPatient;
	}
	public void setCurrentPatient(boolean currentPatient) {
		this.currentPatient = currentPatient;
	}
	public int getPatientType() {
		return patientType;
	}
	public void setPatientType(int patientType) {
		this.patientType = patientType;
	}

	/*
	 * indlaeggelsesDatetime contains both "Indlaeggelsesdato" and "Indlaeggelsestidspunkt" from the HAIBA database
	 */
	public Date getIndlaeggelsesDatetime() {
		return indlaeggelsesDatetime;
	}
	public void setIndlaeggelsesDatetime(Date indlaeggelsesDatetime) {
		this.indlaeggelsesDatetime = indlaeggelsesDatetime;
	}

	/*
	 * udskrivningsDatetime contains both "Udskrivningsdato" and "Udskrivningstidspunkt" from the HAIBA database
	 */
	public Date getUdskrivningsDatetime() {
		return udskrivningsDatetime;
	}
	public void setUdskrivningsDatetime(Date udskrivningsDatetime) {
		this.udskrivningsDatetime = udskrivningsDatetime;
	}

	public List<LPRDiagnose> getLprDiagnoses() {
		return lprDiagnoses;
	}
	public void setLprDiagnoses(List<LPRDiagnose> lprDiagnoses) {
		this.lprDiagnoses = lprDiagnoses;
	}
	public void addLprDiagnose(LPRDiagnose lprDiagnose) {
		lprDiagnoses.add(lprDiagnose);
	}
	
	public List<LPRProcedure> getLprProcedures() {
		return lprProcedures;
	}
	public void setLprProcedures(List<LPRProcedure> lprProcedures) {
		this.lprProcedures = lprProcedures;
	}
	public void addLprProcedure(LPRProcedure lprProcedure) {
		lprProcedures.add(lprProcedure);
	}
	
	/*
	 * According to the rules, some contacts can be disposed due to identical information, but we still need to save the reference number so we can backtrack them
	 * This list does not contain the current recordNumber.
	 */
	public List<LPRReference> getLprReferencer() {
		return lprReferencer;
	}
	public void setLprReferencer(List<LPRReference> lprReferencer) {
		this.lprReferencer = lprReferencer;
	}
	public void addLPRReference(long recordNumber) {
		lprReferencer.add(new LPRReference(recordNumber));
	}
	
	/*
	 * Override to see if content of two administration objects,
	 * with the exception of the recordnumber, diagnoses and procedure, are equal
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Administration)) {
            return false;
        }
        Administration other = (Administration) obj;
        
        if(this.cpr != null && !this.cpr.equals(other.cpr)) {
        	return false;
        } else if(this.cpr == null && other.cpr != null) {
        	return false;
        }
        if(this.sygehusCode != null && !this.sygehusCode.equals(other.sygehusCode)) {
        	return false;
        } else if(this.sygehusCode == null && other.sygehusCode != null) {
        	return false;
        }
        if(this.afdelingsCode != null && !this.afdelingsCode.equals(other.afdelingsCode)) {
        	return false;
        } else if(this.afdelingsCode == null && other.afdelingsCode != null) {
        	return false;
        }
        if(this.indlaeggelsesDatetime != null && !this.indlaeggelsesDatetime.equals(other.indlaeggelsesDatetime)) {
        	return false;
        } else if(this.indlaeggelsesDatetime == null && other.indlaeggelsesDatetime != null) {
        	return false;
        }
        if(this.udskrivningsDatetime != null && !this.udskrivningsDatetime.equals(other.udskrivningsDatetime)) {
        	return false;
        } else if(this.udskrivningsDatetime == null && other.udskrivningsDatetime != null) {
        	return false;
        }

        return true;
    }	


	/*
	 * Hashcode generated from the same members that are used in the equals method
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        return (sygehusCode+
        		afdelingsCode+
        		cpr+
        		indlaeggelsesDatetime+
        		udskrivningsDatetime).hashCode();
    }
}
