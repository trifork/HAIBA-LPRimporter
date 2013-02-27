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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dk.nsi.haiba.lprimporter.config.LPRTestConfiguration;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class LPRPrepareDataRuleTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
		@Bean
		public HAIBADAO haibaDao() {
			return Mockito.mock(HAIBADAO.class);
		}
	}
	
	@Autowired
	HAIBADAO haibaDao;
	
	@Autowired
	LPRPrepareDataRule lprPrepareDataRule;

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
	
	
	@Test
	public void contactIsOK() {
		
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("1 contact is still expected", preparedContacts);
		assertEquals(1, preparedContacts.size());
	
	}

	@Test
	public void contactWithoutRecordNumber() {
		recordNummer = 0;
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("0 contacts are expected", preparedContacts);
		assertEquals(0, preparedContacts.size());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	@Test
	public void contactWithoutCPRNumber() {
		cpr = null;
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("0 contacts are expected", preparedContacts);
		assertEquals(0, preparedContacts.size());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	@Test
	public void contactWithoutSygehusCode() {
		sygehusCode = null;
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("0 contacts are expected", preparedContacts);
		assertEquals(0, preparedContacts.size());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	@Test
	public void contactWithoutAfdelingsCode() {
		afdelingsCode = null;
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("0 contacts are expected", preparedContacts);
		assertEquals(0, preparedContacts.size());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	@Test
	public void contactWithoutInDate() {
		in = null;
		List<Administration> contacts = setupContacts();

		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("0 contacts are expected", preparedContacts);
		assertEquals(0, preparedContacts.size());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	@Test
	public void contactWithSygehusCode3800() {
		sygehusCode = "3800";
		List<Administration> contacts = setupContacts();

		Mockito.when(haibaDao.getSygehusInitials(sygehusCode, afdelingsCode, in.toDate())).thenReturn("HOL");
		
		lprPrepareDataRule.setContacts(contacts);
		lprPrepareDataRule.doProcessing();
		
		List<Administration> preparedContacts = lprPrepareDataRule.getContacts();
		
		assertNotNull("1 contact is still expected", preparedContacts);
		assertEquals(1, preparedContacts.size());
		assertEquals(sygehusCode+"HOL", contacts.get(0).getSygehusCode());
		
		Mockito.verify(haibaDao, Mockito.atLeastOnce()).saveBusinessRuleError((BusinessRuleError) Mockito.any());
	}

	private List<Administration> setupContacts() {
		List<Administration> contacts = new ArrayList<Administration>();
		Administration contact = new Administration();
		contact.setRecordNumber(recordNummer);
		contact.setSygehusCode(sygehusCode);
		contact.setAfdelingsCode(afdelingsCode);
		contact.setCpr(cpr);
		if(in != null) {
			contact.setIndlaeggelsesDatetime(in.toDate());
		}
		contact.setUdskrivningsDatetime(out.toDate());
		contacts.add(contact);
		
		return contacts;
	}
	
}
