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
package dk.nsi.haiba.lprimporter.model.haiba;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * Model object for an Admission from the HAIBA datamodel
 */
public class Indlaeggelse {
	
	long id;
	String cpr;
	String sygehusCode;
	String afdelingsCode;
	Date indlaeggelsesDatetime;
	Date udskrivningsDatetime;
	List<LPRReference> lprReferencer = new ArrayList<LPRReference>();
	List<Diagnose> diagnoses = new ArrayList<Diagnose>();
	List<Procedure> procedures = new ArrayList<Procedure>();

	public Indlaeggelse() {
		
	}

	public Indlaeggelse(long id, String cpr, String sygehusCode,
			String afdelingsCode, Date indlaeggelsesDatetime,
			Date udskrivningsDatetime) {
		this.id = id;
		this.cpr = cpr;
		this.sygehusCode = sygehusCode;
		this.afdelingsCode = afdelingsCode;
		this.indlaeggelsesDatetime = indlaeggelsesDatetime;
		this.udskrivningsDatetime = udskrivningsDatetime;
	}


	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	
	
	public List<LPRReference> getLprReferencer() {
		return lprReferencer;
	}
	public void setLprReferencer(List<LPRReference> lprReferencer) {
		this.lprReferencer = lprReferencer;
	}
	public void addLPRReference(LPRReference lprReference) {
		lprReferencer.add(lprReference);
	}

	public List<Diagnose> getDiagnoses() {
		return diagnoses;
	}

	public void setDiagnoses(List<Diagnose> diagnoses) {
		this.diagnoses = diagnoses;
	}

	public void addDiagnose(Diagnose d) {
		diagnoses.add(d);
	}

	public List<Procedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<Procedure> procedures) {
		this.procedures = procedures;
	}

	public void addProcedure(Procedure p) {
		procedures.add(p);
	}
}
