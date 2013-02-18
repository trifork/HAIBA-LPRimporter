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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DiagnoseTest {

	@Test
	public void testDiagnoseFieldsAreCorrect() {
	
		String diagnoseCode = "fdsa";
		String diagnoseType = "zxcv";
	    String tillaegsDiagnose = "poiu";
		
		Diagnose d = new Diagnose(diagnoseCode, diagnoseType, tillaegsDiagnose);
		
		assertEquals(diagnoseCode, d.getDiagnoseCode());
		assertEquals(diagnoseType, d.getDiagnoseType());
		assertEquals(tillaegsDiagnose, d.getTillaegsDiagnose());
	}

	
	@Test
	public void diagnosesAreEqual() {

		String diagnoseCode = "fdsa";
		String diagnoseType = "zxcv";
	    String tillaegsDiagnose = "poiu";
		
		Diagnose d = new Diagnose(diagnoseCode, diagnoseType, tillaegsDiagnose);
		Diagnose d2 = new Diagnose(diagnoseCode, diagnoseType, tillaegsDiagnose);

		assertEquals(d,  d2);

		Diagnose d3 = new Diagnose("d3", diagnoseType, tillaegsDiagnose);
		assertFalse(d.equals(d3));
		
		Diagnose d4 = new Diagnose(diagnoseCode, null, tillaegsDiagnose);
		assertFalse(d.equals(d4));

		Map<Diagnose, Diagnose> diagnoses = new HashMap<Diagnose, Diagnose>();
		diagnoses.put(d, d);
		assertEquals(d,  diagnoses.get(d));
		
		
	}
}
