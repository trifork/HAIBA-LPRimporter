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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class AdministrationTest {

	String cpr;
	long recordNummer;
	String sygehusCode;
	String afdelingsCode;
	DateTime in;
	DateTime out;

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = 1234;
    	sygehusCode = "csgh";
    	afdelingsCode = "afd";
    	in = new DateTime(2010, 5, 3, 0, 0, 0);
    	out = new DateTime(2010, 6, 4, 12, 0, 0);
	}
	
	
	/*
	 * Tests equals implementation, 
	 * be aware that according to businessrules Diagnoses and Procedures linked to the the administration doesn't have to be equals also,
	 * this is due to how data looks in LPR
	 */
	@Test
	public void testAdministrationsAreEqual() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		assertTrue(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreEqualWithNulls() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm1.setCpr(null);
		adm2.setCpr(null);
		
		assertTrue(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreNotEqualWithOneNull() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm1.setCpr(null);
		
		assertFalse(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreNotEqualWithOtherNull() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm2.setCpr(null);
		
		assertFalse(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreNotEqualWithDifferentCPR() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm2.setCpr("1234");
		
		assertFalse(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreNotEqualWithDifferentSygehuskode() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm1.setSygehusCode("Test");
		
		assertFalse(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreNotEqualWithDifferentAfdelingskode() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm2.setAfdelingsCode("Test");
		
		assertFalse(adm1.equals(adm2));
	}

	@Test
	public void testAdministrationsAreEqualWithDifferentRecordnumber() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();

		adm1.setRecordNumber(999);
		
		assertTrue(adm1.equals(adm2));
	}
	
	@Test
	public void testAdministrationsAreNotEqualWithDifferentInDate() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm1.setIndlaeggelsesDatetime(new Date());
		
		assertFalse(adm1.equals(adm2));
	}
	
	@Test
	public void testAdministrationsAreNotEqualWithDifferentOutDate() {
		
		Administration adm1 = getAdm1();
		Administration adm2 = getAdm2();
		
		adm2.setUdskrivningsDatetime(new Date());
		
		assertFalse(adm1.equals(adm2));
	}

	private Administration getAdm1() {
		Administration adm1 = new Administration();
		adm1.setSygehusCode(sygehusCode);
		adm1.setCpr(cpr);
		adm1.setAfdelingsCode(afdelingsCode);
		adm1.setIndlaeggelsesDatetime(in.toDate());
		adm1.setUdskrivningsDatetime(out.toDate());
		adm1.setRecordNumber(recordNummer);
		return adm1;
	}

	private Administration getAdm2() {
		Administration adm2 = new Administration();
		adm2.setSygehusCode(sygehusCode);
		adm2.setCpr(cpr);
		adm2.setAfdelingsCode(afdelingsCode);
		adm2.setIndlaeggelsesDatetime(in.toDate());
		adm2.setUdskrivningsDatetime(out.toDate());
		adm2.setRecordNumber(recordNummer);
		return adm2;
	}
}
