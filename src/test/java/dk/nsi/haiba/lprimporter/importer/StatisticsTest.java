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

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
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
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class StatisticsTest {

    @Configuration
    @Import({ LPRTestConfiguration.class })
    static class TestConfiguration {
        @Bean
        public HAIBADAO haibaDao() {
            return Mockito.mock(HAIBADAO.class);
        }

        @Bean
        public LPRDAO lprdao() {
            return Mockito.mock(LPRDAO.class);
        }

        @Bean
        public RulesEngine rulesEngine() {
            return Mockito.mock(RulesEngine.class);
        }

        @Bean
        public ImportStatusRepository statusRepo() {
            return Mockito.mock(ImportStatusRepository.class);
        }

        @Bean
        public ImportExecutor importExecutor() {
            return new ImportExecutor();
        }
    }

    @Autowired
    ImportExecutor executor;

    @Autowired
    LPRDAO lprdao;

    @Autowired
    HAIBADAO haibadao;

    @Autowired
    RulesEngine rulesEngine;

    @Before
    public void resetMocks() {
        Mockito.reset(lprdao);
        Mockito.reset(rulesEngine);
    }

    @Test
    public void statisticsCounters() throws Exception {
        long syncId = 1l;
        List<String> cprList = generateCprList(2);
        Mockito.when(lprdao.isdatabaseReadyForImport()).thenReturn(syncId);
        Mockito.when(lprdao.hasUnprocessedCPRnumbers()).thenReturn(true);
        Mockito.when(lprdao.getCPRnumbersFromDeletedContacts(syncId)).thenReturn(cprList);
        Mockito.when(lprdao.getCPRnumberBatch(20)).thenReturn(generateCprList(20)).thenReturn(generateCprList(5))
                .thenReturn(new ArrayList<String>());
        Mockito.when(haibadao.getCurrentPatients()).thenReturn(cprList);

        Statistics statistics = Statistics.getInstance();
        executor.doProcess();
        System.out.println(statistics);
        assertEquals("Statistics contactCounter", 0, statistics.contactCounter);
        assertEquals("Statistics cprNumbersWithDeletedContactsCounter counter", 2,
                statistics.cprNumbersWithDeletedContactsCounter);
        assertEquals("Statistics currentPatientsCounter counter", 2, statistics.currentPatientsCounter);
        assertEquals("Statistics cprCounter counter", 25, statistics.cprCounter);
    }

    private List<String> generateCprList(int size) {
        List<String> returnValue = new ArrayList<String>();
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumIntegerDigits(10);
        for (int i = 0; i < size; i++) {
            returnValue.add(numberFormat.format(i));
        }
        return returnValue;
    }
}
