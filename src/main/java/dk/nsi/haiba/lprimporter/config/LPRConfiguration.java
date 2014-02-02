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

import java.util.Properties;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.ClassificationCheckDAOImpl;
import dk.nsi.haiba.lprimporter.dao.impl.HAIBADAOImpl;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOComposite;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOImpl;
import dk.nsi.haiba.lprimporter.email.EmailSender;
import dk.nsi.haiba.lprimporter.importer.ClassificationCheckHelper;
import dk.nsi.haiba.lprimporter.importer.ImportExecutor;
import dk.nsi.haiba.lprimporter.log.BusinessRuleErrorLog;
import dk.nsi.haiba.lprimporter.message.MessageResolver;
import dk.nsi.haiba.lprimporter.rules.ConnectAdmissionsRule;
import dk.nsi.haiba.lprimporter.rules.ConnectContactsRule;
import dk.nsi.haiba.lprimporter.rules.ContactToAdmissionRule;
import dk.nsi.haiba.lprimporter.rules.ContactsWithSameStartDateRule;
import dk.nsi.haiba.lprimporter.rules.ExtendContactEndtimeRule;
import dk.nsi.haiba.lprimporter.rules.LPRDateTimeRule;
import dk.nsi.haiba.lprimporter.rules.LPRPrepareDataRule;
import dk.nsi.haiba.lprimporter.rules.LPRRulesEngine;
import dk.nsi.haiba.lprimporter.rules.OverlappingContactsRule;
import dk.nsi.haiba.lprimporter.rules.RemoveIdenticalContactsRule;
import dk.nsi.haiba.lprimporter.rules.RulesEngine;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepositoryJdbcImpl;
import dk.nsi.haiba.lprimporter.status.TimeSource;
import dk.nsi.haiba.lprimporter.status.TimeSourceRealTimeImpl;

/**
 * Configuration class providing the common infrastructure.
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
public class LPRConfiguration {
    @Value("${jdbc.lprJNDIName}")
    private String lprJdbcJNDIName;

    @Value("${jdbc.lprJNDIName_minipas}")
    private String lprJdbcJNDIName_minipas;

    @Value("${jdbc.haibaJNDIName}")
    private String haibaJdbcJNDIName;

    @Value("${jdbc.classificationJNDIName}")
    private String classificationJdbcJNDIName;

    @Value("${smtp.host}")
    private String smtpHost;
    @Value("${smtp.port}")
    private int smtpPort;
    @Value("${smtp.user}")
    private String smtpUser;
    @Value("${smtp.password}")
    private String smtpPassword;
    @Value("${smtp.auth}")
    private String smtpAuth;

    // this is not automatically registered, see https://jira.springsource.org/browse/SPR-8539
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(false);

        propertySourcesPlaceholderConfigurer.setLocations(new Resource[] {
                new ClassPathResource("default-config.properties"), new ClassPathResource("config.properties") });

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
    public DataSource haibaDataSource() throws Exception {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiName(haibaJdbcJNDIName);
        factory.setExpectedType(DataSource.class);
        factory.afterPropertiesSet();
        return (DataSource) factory.getObject();
    }

    @Bean
    public DataSource lprDataSourceMinipas() throws Exception {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiName(lprJdbcJNDIName_minipas);
        factory.setExpectedType(DataSource.class);
        factory.afterPropertiesSet();
        return (DataSource) factory.getObject();
    }

    @Bean
    public PlatformTransactionManager lprTransactionManager(@Qualifier("lprDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public PlatformTransactionManager minipasTransactionManagerMinipas(@Qualifier("lprDataSourceMinipas") DataSource ds) {
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
    public JdbcTemplate minipasJdbcTemplate(@Qualifier("lprDataSourceMinipas") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("haibaTransactionManager")
    public PlatformTransactionManager haibaTransactionManager(@Qualifier("haibaDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    // This needs the static modifier due to https://jira.springsource.org/browse/SPR-8269. If not static, field
    // jdbcJndiName
    // will not be set when trying to instantiate the DataSource
    @Bean
    public static CustomScopeConfigurer scopeConfigurer() {
        return new SimpleThreadScopeConfigurer();
    }

    @Bean
    public ImportStatusRepository statusRepo() {
        return new ImportStatusRepositoryJdbcImpl();
    }

    @Bean
    public TimeSource timeSource() {
        return new TimeSourceRealTimeImpl();
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        String[] resources = { "classpath:messages" };
        messageSource.setBasenames(resources);
        return messageSource;
    }

    @Bean
    public MessageResolver resolver() {
        return new MessageResolver();
    }

    @Bean
    BusinessRuleErrorLog businessRuleErrorLog() {
        return new BusinessRuleErrorLog();
    }

    @Bean
    public ImportExecutor importExecutor(@Qualifier(value = "compositeLPRDAO") LPRDAO lprdao) {
        return new ImportExecutor(lprdao);
    }

    @Bean
    public RulesEngine rulesEngine() {
        return new LPRRulesEngine();
    }

    @Bean(name = "ssiLPRDAO")
    public LPRDAO lprdao(@Qualifier("lprDataSource") DataSource ds) {
        return new LPRDAOImpl(ds);
    }

    @Bean
    public HAIBADAO haibaDao() {
        return new HAIBADAOImpl();
    }

    @Bean(name = "minipasLPRDAO")
    public LPRDAO minipasLPRDAO(@Qualifier("lprDataSourceMinipas") DataSource ds) {
        return new LPRDAOImpl(ds);
    }

    @Bean(name = "compositeLPRDAO")
    public LPRDAO compositeLPRDAO() {
        return new LPRDAOComposite();
    }

    @Bean
    public LPRDateTimeRule lprDateTimeRule() {
        return new LPRDateTimeRule();
    }

    @Bean
    public ExtendContactEndtimeRule extendContactEndtimeRule() {
        return new ExtendContactEndtimeRule();
    }

    @Bean
    public ContactToAdmissionRule contactToAdmissionRule() {
        return new ContactToAdmissionRule();
    }

    @Bean
    public RemoveIdenticalContactsRule removeIdenticalContactsRule() {
        return new RemoveIdenticalContactsRule();
    }

    @Bean
    public OverlappingContactsRule overlappingContactsRule() {
        return new OverlappingContactsRule();
    }

    @Bean
    public ConnectContactsRule connectContactsRule() {
        return new ConnectContactsRule();
    }

    @Bean
    public ConnectAdmissionsRule connectAdmissionsRule() {
        return new ConnectAdmissionsRule();
    }

    @Bean
    public LPRPrepareDataRule lprPrepareDataRule() {
        return new LPRPrepareDataRule();
    }

    @Bean
    public ContactsWithSameStartDateRule contactsWithSameStartDateRule() {
        return new ContactsWithSameStartDateRule();
    }

    @Bean
    @Qualifier("classificationDataSource")
    public DataSource classificationDataSource() throws Exception {
        JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
        factory.setJndiName(classificationJdbcJNDIName);
        factory.setExpectedType(DataSource.class);
        factory.afterPropertiesSet();
        return (DataSource) factory.getObject();
    }

    @Bean
    public JdbcTemplate classificationJdbcTemplate(@Qualifier("classificationDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public ClassificationCheckDAO classificationCheckDAO(
            @Qualifier("classificationJdbcTemplate") JdbcTemplate classificationJdbcTemplate) {
        return new ClassificationCheckDAOImpl(classificationJdbcTemplate);
    }

    @Bean
    public EmailSender mailSender() {
        return new EmailSender();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", smtpAuth);
        javaMailProperties.put("mail.smtp.starttls.enable", true);
        javaMailProperties.put("mail.smtp.host", smtpHost);
        javaMailProperties.put("mail.smtp.port", smtpPort);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setJavaMailProperties(javaMailProperties);
        sender.setUsername(smtpUser);
        sender.setPassword(smtpPassword);

        return sender;
    }

    @Bean
    public ClassificationCheckHelper classificationCheckHelper() {
        return new ClassificationCheckHelper();
    }
}
