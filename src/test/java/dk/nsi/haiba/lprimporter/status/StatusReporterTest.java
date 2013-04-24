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
package dk.nsi.haiba.lprimporter.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dk.nsi.haiba.lprimporter.config.LPRTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class StatusReporterTest {

	@Configuration
    @Import({LPRTestConfiguration.class})
    static class TestConfiguration {
        @Bean
        public StatusReporter statusReporter() {
            return new StatusReporter();
        }
        
    	@Bean
    	public JdbcTemplate jdbcTemplate(@Qualifier("lprDataSource") DataSource dataSource) {
    		return Mockito.mock(JdbcTemplate.class);
    	}

    	@Bean
    	public JdbcTemplate haibaJdbcTemplate(@Qualifier("haibaDataSource") DataSource ds) {
    		return Mockito.mock(JdbcTemplate.class);
    	}
    }

    @Autowired
    StatusReporter reporter;
    
    @Autowired
    JdbcTemplate haibaJdbcTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

	@Before
	public void resetMocks() {
		Mockito.reset(haibaJdbcTemplate);
		Mockito.reset(jdbcTemplate);
	}

	@Test
    public void willReturn200underNormalCircumstances() throws Exception {
        final ResponseEntity<String> response = reporter.reportStatus();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().startsWith("OK"));
    }

    @Test
    public void willReturn500whenHAIBADBisDown() throws Exception {
    	Mockito.when(haibaJdbcTemplate.queryForObject("SELECT indlaeggelsesid from Indlaeggelser LIMIT 1", Long.class)).thenThrow(Exception.class);
    	
        final ResponseEntity<String> response = reporter.reportStatus();

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("HAIBA Database is _NOT_ running correctly"));
    }

    @Test
    public void willReturn500whenLPRDBisDown() throws Exception {
    	Mockito.when(jdbcTemplate.queryForObject("SELECT v_recnum from T_ADM  LIMIT 1", Long.class)).thenThrow(Exception.class);
    	
        final ResponseEntity<String> response = reporter.reportStatus();

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("LPR Database is _NOT_ running correctly"));
    }
}
