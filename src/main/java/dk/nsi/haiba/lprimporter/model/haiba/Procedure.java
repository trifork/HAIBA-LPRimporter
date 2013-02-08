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

import java.util.Date;

/*
 * Model object for a Procedure (operation or treatment) from the HAIBA datamodel
 */
public class Procedure {
	
	String procedureCode;
	String procedureType;
	String tillaegsProcedureCode;
	String sygehusCode;
	String afdelingsCode;
	Date procedureDatetime;
	
	public Procedure() {}
	
	public Procedure(String procedureCode,
			String procedureType, String tillaegsProcedureCode,
			String sygehusCode, String afdelingsCode, Date procedureDatetime) {
		this.procedureCode = procedureCode;
		this.procedureType = procedureType;
		this.tillaegsProcedureCode = tillaegsProcedureCode;
		this.sygehusCode = sygehusCode;
		this.afdelingsCode = afdelingsCode;
		this.procedureDatetime = procedureDatetime;
	}

	public String getProcedureCode() {
		return procedureCode;
	}
	public void setProcedureCode(String procedureCode) {
		this.procedureCode = procedureCode;
	}
	public String getProcedureType() {
		return procedureType;
	}
	public void setProcedureType(String procedureType) {
		this.procedureType = procedureType;
	}
	public String getTillaegsProcedureCode() {
		return tillaegsProcedureCode;
	}
	public void setTillaegsProcedureCode(String tillaegsProcedureCode) {
		this.tillaegsProcedureCode = tillaegsProcedureCode;
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
	 * procedureDatetime contains both "Proceduredato" and "Proceduretidspunkt" from the HAIBA database
	 */
	public Date getProcedureDatetime() {
		return procedureDatetime;
	}
	public void setProcedureDatetime(Date procedureDatetime) {
		this.procedureDatetime = procedureDatetime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Procedure that = (Procedure) o;

		if (procedureCode != null ? !procedureCode.equals(that.procedureCode) : that.procedureCode != null) return false;
		if (procedureType != null ? !procedureType.equals(that.procedureType) : that.procedureType != null) return false;
		if (tillaegsProcedureCode != null ? !tillaegsProcedureCode.equals(that.tillaegsProcedureCode) : that.tillaegsProcedureCode != null) return false;
		if (sygehusCode != null ? !sygehusCode.equals(that.sygehusCode) : that.sygehusCode != null) return false;
		if (afdelingsCode != null ? !afdelingsCode.equals(that.afdelingsCode) : that.afdelingsCode != null) return false;
		if (procedureDatetime != null ? !procedureDatetime.equals(that.procedureDatetime) : that.procedureDatetime != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = procedureCode != null ? procedureCode.hashCode() : 0;
		result = 31 * result + (procedureType != null ? procedureType.hashCode() : 0);
		result = 31 * result + (tillaegsProcedureCode != null ? tillaegsProcedureCode.hashCode() : 0);
		result = 12 * result + (sygehusCode != null ? sygehusCode.hashCode() : 0);
		result = 14 * result + (afdelingsCode != null ? afdelingsCode.hashCode() : 0);
		result = 3 * result + (procedureDatetime != null ? procedureDatetime.hashCode() : 0);
		return result;
	}
	
	
	
}
