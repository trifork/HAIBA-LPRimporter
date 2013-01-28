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
package dk.nsi.haiba.lprimporter.integrationtest;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysql.jdbc.Driver;

import dk.nsi.haiba.lprimporter.config.LPRConfiguration;
import dk.sdsd.nsp.slalog.api.SLALogger;
import dk.sdsd.nsp.slalog.impl.SLALoggerDummyImpl;

@Configuration
@EnableTransactionManagement
@PropertySource("test.properties")
public class LPRIntegrationTestConfiguration extends LPRConfiguration {
	@Value("${test.mysql.port}")
	private int mysqlPort;
	@Value("${test.mysql.lprdbname}")
	private String testLPRDbName;
	@Value("${test.mysql.lprdbusername}")
	private String testLPRDbUsername;
	@Value("${test.mysql.lprdbpassword}")
	private String testLPRDbPassword;
	@Value("${test.mysql.haibadbname}")
	private String testHAIBADbName;
	@Value("${test.mysql.haibadbusername}")
	private String testHAIBADbUsername;
	@Value("${test.mysql.haibadbpassword}")
	private String testHAIBADbPassword;

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties(){
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	@Qualifier("lprDataSource")
	public DataSource lprDataSource() throws Exception{
		String jdbcUrlPrefix = "jdbc:mysql://127.0.0.1:" + mysqlPort + "/";

		// TODO Create a test version of the database only used in integrationtests.

		return new SimpleDriverDataSource(new Driver(), jdbcUrlPrefix + testLPRDbName + "?createDatabaseIfNotExist=true", testLPRDbUsername, testLPRDbPassword);
	}

	@Bean
	public JdbcTemplate jdbcTemplate(@Qualifier("lprDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	@Bean
	@Qualifier("lprTransactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier("lprDataSource") DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	@Bean
	@Qualifier("haibaDataSource")
	public DataSource haibaDataSource() throws Exception{
		String jdbcUrlPrefix = "jdbc:mysql://127.0.0.1:" + mysqlPort + "/";

		// TODO Create a test version of the database only used in integrationtests.

		return new SimpleDriverDataSource(new Driver(), jdbcUrlPrefix + testHAIBADbName + "?createDatabaseIfNotExist=true", testHAIBADbUsername, testHAIBADbPassword);
	}

	@Bean
	public JdbcTemplate haibaJdbcTemplate(@Qualifier("haibaDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

	@Bean
	@Qualifier("haibaTransactionManager")
	public PlatformTransactionManager haibaTransactionManager(@Qualifier("haibaDataSource") DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	@Bean
	public SLALogger slaLogger() {
		return new SLALoggerDummyImpl();
	}
}
