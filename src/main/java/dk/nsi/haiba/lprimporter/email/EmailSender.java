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
package dk.nsi.haiba.lprimporter.email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO.CheckStructure;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.util.AlphanumComparator;

public class EmailSender {
    private static Log log = new Log(Logger.getLogger(EmailSender.class));

    @Value("${smtp.from}")
    private String from;
    @Value("${smtp.to_commaseparated}")
    private String to_commaseparated;
    @Value("${smtp.sendhello}")
    private boolean sendHello;

    @Autowired
    private JavaMailSender javaMailSender;

    private static MyNaturalCheckStructureComparator aMyNaturalCheckStructureComparator = new MyNaturalCheckStructureComparator();

    public String getTo() {
        return to_commaseparated;
    }

    public void send(Collection<CheckStructure> newSygehusClassifications,
            Collection<CheckStructure> newProcedureCheckClassifications,
            Collection<CheckStructure> newDiagnoseCheckClassifications) {
        Collection<CheckStructure> sygehuse = sort(newSygehusClassifications);
        Collection<CheckStructure> procedurer = sort(newProcedureCheckClassifications);
        Collection<CheckStructure> diagnoser = sort(newDiagnoseCheckClassifications);

        String not_html = "After the recent import, the following unknown table entries are discovered:\n";

        if (!sygehuse.isEmpty()) {
            not_html += printCheckStructures(sygehuse, "sygehus", "afdeling");
        }
        if (!procedurer.isEmpty()) {
            not_html += printCheckStructures(procedurer, "procedure", "tillaegskode");
        }
        if (!diagnoser.isEmpty()) {
            not_html += printCheckStructures(diagnoser, "diagnose", "tillaegskode");
        }
        sendText("LPR: Notification on unknown table entries", not_html);
    }

    private String printCheckStructures(Collection<CheckStructure> checkStructures, String codeLabel,
            String secondaryCodeLabel) {
        String returnValue = "-----\n";
        for (CheckStructure cs : checkStructures) {
            returnValue += codeLabel + ":" + cs.getCode() + ", " + secondaryCodeLabel + ":"
                    + (cs.getSecondaryCode() != null ? cs.getSecondaryCode() : "") + "\n";
        }
        return returnValue;
    }

    private void sendText(final String subject, final String nonHtml) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
                messageHelper.setValidateAddresses(true);

                String[] split = to_commaseparated.split(",");
                for (String emailAddress : split) {
                    emailAddress = emailAddress.trim();
                    try {
                        log.debug("adding " + emailAddress);
                        messageHelper.addTo(emailAddress);
                        log.debug("added " + emailAddress);
                    } catch (MessagingException e) {
                        log.error("unable to parse email address from " + emailAddress, e);
                    }
                }
                messageHelper.setFrom(from);
                messageHelper.setSubject(subject);
                messageHelper.setText(nonHtml, false);
            }
        };
        javaMailSender.send(preparator);
    }

    public static List<CheckStructure> sort(Collection<CheckStructure> c) {
        List<CheckStructure> list = new ArrayList<CheckStructure>(c);
        Collections.sort(list, aMyNaturalCheckStructureComparator);
        return list;
    }

    public static class MyNaturalCheckStructureComparator implements Comparator<CheckStructure> {
        @Override
        public int compare(CheckStructure o1, CheckStructure o2) {
            return AlphanumComparator.INSTANCE.compare(getString(o1), getString(o2));
        }

        private String getString(CheckStructure cs) {
            String s = "";
            if (cs.getCode() != null) {
                s += cs.getCode();
            }
            if (cs.getSecondaryCode() != null) {
                s += cs.getSecondaryCode();
            }
            return s;
        }
    }

    public void sendHello() {
        if (sendHello) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sendText("LPR: Import started at " + dateFormat.format(new Date()), "");
        }
    }

    public void sendDone(String error) {
        if (sendHello) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (error == null) {
                sendText("LPR: Import done at " + dateFormat.format(new Date()), "No errors");
            } else {
                sendText("LPR: Import done at " + dateFormat.format(new Date()), "Errors found\n:" + error);
            }
        }
    }
}
