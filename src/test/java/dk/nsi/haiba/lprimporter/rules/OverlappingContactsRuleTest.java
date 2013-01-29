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
package dk.nsi.haiba.lprimporter.rules;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dk.nsi.haiba.lprimporter.config.LPRTestConfiguration;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class OverlappingContactsRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
	}
	
	@Autowired
	OverlappingContactsRule overlappingContactsRule;

	String cpr;
	long recordNummer;
	long recordNummer2;
	long recordNummer3;
	String sygehusCode;
	String afdelingsCode;
	String sygehusCode2;
	String afdelingsCode2;
	String sygehusCode3;
	String afdelingsCode3;
	DateTime in;
	DateTime out;
	DateTime in2;
	DateTime out2;
	DateTime in3;
	DateTime out3;

	@Before
	public void init() {
    	// Init Administration data
		cpr = "1111111111";
    	recordNummer = 1234;
    	sygehusCode = "csgh";
    	afdelingsCode = "afd";
    	in = new DateTime(2010, 5, 3, 0, 0, 0);
    	out = new DateTime(2010, 6, 4, 12, 0, 0);

    	recordNummer2 = 1235;
    	sygehusCode2 = "csgh";
    	afdelingsCode2 = "afd";
    	in2 = new DateTime(2010, 5, 4, 0, 0, 0);
    	out2 = new DateTime(2010, 5, 10, 12, 0, 0);

    	recordNummer3 = 1236;
    	sygehusCode3 = "abcd";
    	afdelingsCode3 = "afd";
    	in3 = new DateTime(2010, 8, 4, 0, 0, 0);
    	out3 = new DateTime(2010, 8, 10, 12, 0, 0);
	}
	
	/*
	 * 2 contacts with same fields are in the list, one of the should be removed
	 */
	@Test 
	public void overlappingContactIsSplittedIntoTwo() {
		assertNotNull(overlappingContactsRule);
		
		List<Administration> contacts = setupContacts();

		overlappingContactsRule.setContacts(contacts);
		overlappingContactsRule.doProcessing();
		
		List<Administration> processedContacts = overlappingContactsRule.getContacts();
		
		assertTrue("Expecting 1 extra contact added to the list", processedContacts.size() == 4);

		Collections.sort(processedContacts, new InDateComparator());
		
		Administration previous = null;
		for (Administration current : processedContacts) {
			if(previous == null) {
				previous = current;
				continue;
			}
			DateTime previousIn = new DateTime(previous.getIndlaeggelsesDatetime());
			DateTime previousOut = new DateTime(previous.getUdskrivningsDatetime());
			DateTime in = new DateTime(current.getIndlaeggelsesDatetime());
			DateTime out = new DateTime(current.getUdskrivningsDatetime());
			
			assertTrue(previousIn.isBefore(in));
			assertTrue(previousOut.isBefore(out) || previousOut.isEqual(out));
		}
	}
	
	private List<Administration> setupContacts() {
		List<Administration> contacts = new ArrayList<Administration>();
		Administration contact = new Administration();
		contact.setRecordNumber(recordNummer);
		contact.setSygehusCode(sygehusCode);
		contact.setAfdelingsCode(afdelingsCode);
		contact.setCpr(cpr);
		contact.setIndlaeggelsesDatetime(in.toDate());
		contact.setUdskrivningsDatetime(out.toDate());
		
		Administration contact2 = new Administration();
		contact2.setRecordNumber(recordNummer2);
		contact2.setSygehusCode(sygehusCode2);
		contact2.setAfdelingsCode(afdelingsCode2);
		contact2.setCpr(cpr);
		contact2.setIndlaeggelsesDatetime(in2.toDate());
		contact2.setUdskrivningsDatetime(out2.toDate());
		
		Administration contact3 = new Administration();
		contact3.setRecordNumber(recordNummer3);
		contact3.setSygehusCode(sygehusCode3);
		contact3.setAfdelingsCode(afdelingsCode3);
		contact3.setCpr(cpr);
		contact3.setIndlaeggelsesDatetime(in3.toDate());
		contact3.setUdskrivningsDatetime(out3.toDate());

		contacts.add(contact3);
		contacts.add(contact2);
		contacts.add(contact);

		return contacts;
	}
	
}
