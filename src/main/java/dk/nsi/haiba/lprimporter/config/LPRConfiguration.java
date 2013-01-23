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
package dk.nsi.haiba.lprimporter.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.HAIBADAOImpl;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOImpl;
import dk.nsi.haiba.lprimporter.importer.ImportExecutor;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.rules.ContactToAdmissionRule;
import dk.nsi.haiba.lprimporter.rules.LPRDateTimeRule;
import dk.nsi.haiba.lprimporter.rules.LPRRulesEngine;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;

/**
 * Configuration class 
 * providing the common infrastructure.
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
public class LPRConfiguration {
	@Value("${jdbc.lprJNDIName}")
	private String lprJdbcJNDIName;

	@Value("${jdbc.haibaJNDIName}")
	private String haibaJdbcJNDIName;

	// this is not automatically registered, see https://jira.springsource.org/browse/SPR-8539
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
		propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(false);

		propertySourcesPlaceholderConfigurer.setLocations(new Resource[]{new ClassPathResource("default-config.properties"), new ClassPathResource("config.properties")});

		return propertySourcesPlaceholderConfigurer;
	}

	@Bean
	@Qualifier("lprDataSource")
	public DataSource lprDataSource() throws Exception {
		JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
		factory.setJndiName(lprJdbcJNDIName);
		factory.setExpectedType(DataSource.class);
		factory.afterPropertiesSet();
		return (DataSource) factory.getObject();
	}

	@Bean
	@Qualifier("haibaDataSource")
	public DataSource haibaDataSource() throws Exception {
		JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
		factory.setJndiName(haibaJdbcJNDIName);
		factory.setExpectedType(DataSource.class);
		factory.afterPropertiesSet();
		return (DataSource) factory.getObject();
	}

	@Bean
	@Qualifier("lprTransactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier("lprDataSource") DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	@Bean
	public JdbcTemplate jdbcTemplate(@Qualifier("lprDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
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

	// This needs the static modifier due to https://jira.springsource.org/browse/SPR-8269. If not static, field jdbcJndiName
	// will not be set when trying to instantiate the DataSource
	@Bean
	public static CustomScopeConfigurer scopeConfigurer() {
		return new SimpleThreadScopeConfigurer();
	}
	
	@Bean
    public ReloadableResourceBundleMessageSource messageSource(){
        ReloadableResourceBundleMessageSource messageSource=new ReloadableResourceBundleMessageSource();
        String[] resources= {"classpath:messages"};
        messageSource.setBasenames(resources);
        return messageSource;
    }
	
	@Bean
	public MessageResolver resolver() {
		return new MessageResolver();
	}
	
	@Bean
	public ImportExecutor importExecutor() {
		return new ImportExecutor();
	}

	@Bean
	public RulesEngine rulesEngine() {
		return new LPRRulesEngine();
	}

	@Bean
    public LPRDAO lprdao() {
        return new LPRDAOImpl();
    }
	
	@Bean
    public HAIBADAO haibaDao() {
        return new HAIBADAOImpl();
    }
	
	@Bean
	public LPRDateTimeRule lprDateTimeRule() {
		return new LPRDateTimeRule();
	}
	
	@Bean
	public ContactToAdmissionRule contactToAdmissionRule() {
		return new ContactToAdmissionRule();
	}
	
}
