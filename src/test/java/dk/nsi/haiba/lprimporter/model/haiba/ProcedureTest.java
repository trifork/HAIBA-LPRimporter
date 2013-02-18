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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ProcedureTest {

	@Test
	public void testProcedureFieldsAreCorrect() {

		String procedureCode = "pCode";
		String procedureType = "pType";
		String tillaegsProcedureCode = "tpCode";
		String sygehusCode = "sCode";
		String afdelingsCode = "aCode";
		
		Calendar calendar = new GregorianCalendar();
		Date d1 = calendar.getTime();
		
		Procedure p = new Procedure(procedureCode,procedureType, tillaegsProcedureCode, sygehusCode, afdelingsCode, d1);
		
		assertEquals(procedureCode, p.getProcedureCode());
		assertEquals(procedureType, p.getProcedureType());
		assertEquals(tillaegsProcedureCode, p.getTillaegsProcedureCode());
		assertEquals(sygehusCode, p.getSygehusCode());
		assertEquals(afdelingsCode, p.getAfdelingsCode());
		assertEquals(d1, p.getProcedureDatetime());
	}
	
	@Test
	public void proceduresAreEqual() {

		String procedureCode = "pCode";
		String procedureType = "pType";
		String tillaegsProcedureCode = "tpCode";
		String sygehusCode = "sCode";
		String afdelingsCode = "aCode";
		Calendar calendar = new GregorianCalendar();
		Date d1 = calendar.getTime();
		
		Procedure p = new Procedure(procedureCode,procedureType, tillaegsProcedureCode, sygehusCode, afdelingsCode, d1);
		Procedure p2 = new Procedure(procedureCode,procedureType, tillaegsProcedureCode, sygehusCode, afdelingsCode, d1);
		assertEquals(p,  p2);

		Procedure p3 = new Procedure("p3",procedureType, tillaegsProcedureCode, sygehusCode, afdelingsCode, d1);
		assertFalse(p.equals(p3));
		
		Procedure p4 = new Procedure(procedureCode,null, tillaegsProcedureCode, sygehusCode, afdelingsCode, d1);
		assertFalse(p.equals(p4));

		Map<Procedure, Procedure> procedures = new HashMap<Procedure, Procedure>();
		procedures.put(p, p);
		assertEquals(p,  procedures.get(p));
	}
}
