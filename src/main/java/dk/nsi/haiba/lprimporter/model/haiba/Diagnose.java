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


/*
 * Model object for a Diagnosis from the HAIBA datamodel
 */
public class Diagnose {
	
	String diagnoseCode;
	String diagnoseType;
	String tillaegsDiagnose;
	
	
	public Diagnose() {};
	
	public Diagnose(String diagnoseCode,
			String diagnoseType, String tillaegsDiagnose) {
		this.diagnoseCode = diagnoseCode;
		this.diagnoseType = diagnoseType;
		this.tillaegsDiagnose = tillaegsDiagnose;
	}
	
	public String getDiagnoseCode() {
		return diagnoseCode;
	}
	public void setDiagnoseCode(String diagnoseCode) {
		this.diagnoseCode = diagnoseCode;
	}
	public String getDiagnoseType() {
		return diagnoseType;
	}
	public void setDiagnoseType(String diagnoseType) {
		this.diagnoseType = diagnoseType;
	}
	public String getTillaegsDiagnose() {
		return tillaegsDiagnose;
	}
	public void setTillaegsDiagnose(String tillaegsDiagnose) {
		this.tillaegsDiagnose = tillaegsDiagnose;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Diagnose that = (Diagnose) o;

		if (diagnoseCode != null ? !diagnoseCode.equals(that.diagnoseCode) : that.diagnoseCode != null) return false;
		if (diagnoseType != null ? !diagnoseType.equals(that.diagnoseType) : that.diagnoseType != null) return false;
		if (tillaegsDiagnose != null ? !tillaegsDiagnose.equals(that.tillaegsDiagnose) : that.tillaegsDiagnose != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = diagnoseCode != null ? diagnoseCode.hashCode() : 0;
		result = 31 * result + (diagnoseType != null ? diagnoseType.hashCode() : 0);
		result = 31 * result + (tillaegsDiagnose != null ? tillaegsDiagnose.hashCode() : 0);
		return result;
	}

}
