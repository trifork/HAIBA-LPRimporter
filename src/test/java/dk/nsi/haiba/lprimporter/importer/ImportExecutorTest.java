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
package dk.nsi.haiba.lprimporter.importer;

import java.util.ArrayList;
import java.util.List;

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
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportExecutorTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
	static class TestConfiguration {
		@Bean
		public LPRDAO lprdao() {
			return Mockito.mock(LPRDAO.class);
		}
		@Bean
		public RulesEngine rulesEngine() {
			return Mockito.mock(RulesEngine.class);
		}
	}
	
	@Autowired
	ImportExecutor executor;

	@Autowired
	LPRDAO lprdao;

	@Autowired
	RulesEngine rulesEngine;

	@Before
	public void resetMocks() {
		Mockito.reset(lprdao);
		Mockito.reset(rulesEngine);
	}

	@Test
	public void executorDoesntFecthAnyContacts() throws Exception {
		
		Mockito.when(lprdao.getUnprocessedCPRnumbers()).thenReturn(new ArrayList<String>());
		
		executor.doProcess();
		
		Mockito.verify(lprdao).getUnprocessedCPRnumbers();
		Mockito.verify(lprdao, Mockito.never()).getContactsByCPR(null);
	}

	@Test
	public void executorFetchesOneUnprocessedContact() throws Exception {
		List<String> cprList = new ArrayList<String>();
		cprList.add("1234567890");
		
		Mockito.when(lprdao.getUnprocessedCPRnumbers()).thenReturn(cprList);
		
		executor.doProcess();
		
		Mockito.verify(lprdao).getUnprocessedCPRnumbers();
		Mockito.verify(lprdao, Mockito.atLeastOnce()).getContactsByCPR(cprList.get(0));
		Mockito.verify(rulesEngine, Mockito.atLeastOnce()).processRuleChain(Mockito.anyListOf(Administration.class));
	}

}
