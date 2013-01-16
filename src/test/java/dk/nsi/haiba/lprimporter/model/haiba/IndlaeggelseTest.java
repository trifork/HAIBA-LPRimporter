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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class IndlaeggelseTest {

	@Test
	public void testIndlaeggelseFieldsAreCorrect() {

		String cpr = "1234567890";
		String sygehusCode = "qwerty";
		String afdelingsCode = "asdf";
		
		Calendar calendar = new GregorianCalendar();
		Date d1 = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date d2 = calendar.getTime();
		
		Indlaeggelse indlaeggelse = new Indlaeggelse(cpr,sygehusCode,afdelingsCode, d1, d2);
		
		assertEquals(cpr, indlaeggelse.getCpr());
		assertEquals(sygehusCode, indlaeggelse.getSygehusCode());
		assertEquals(afdelingsCode, indlaeggelse.getAfdelingsCode());
		assertEquals(d1, indlaeggelse.getIndlaeggelsesDatetime());
		assertEquals(d2, indlaeggelse.getUdskrivningsDatetime());
		
	}
}
