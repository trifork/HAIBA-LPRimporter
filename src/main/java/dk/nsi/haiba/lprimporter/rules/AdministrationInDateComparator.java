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

import java.util.Comparator;

import org.apache.log4j.Logger;

import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.lpr.Administration;

public class AdministrationInDateComparator implements Comparator<Administration> {
	private static Log log = new Log(Logger.getLogger(AdministrationInDateComparator.class));

	@Override
    public int compare(Administration o1, Administration o2) {
    	// if indates are equal, the two contacts must be sorted on outdates to get the right sequence
    	
    	if(o1.getIndlaeggelsesDatetime().equals(o2.getIndlaeggelsesDatetime())) {
    		if(o1.getUdskrivningsDatetime() == null || o2.getUdskrivningsDatetime() == null) {
    			log.warn("UdskrivningsDateTime is null, connot compare it");
    	        return o1.getIndlaeggelsesDatetime().compareTo(o2.getIndlaeggelsesDatetime());
    			
    		} else {
                return o1.getUdskrivningsDatetime().compareTo(o2.getUdskrivningsDatetime());
    		}
    	}
        return o1.getIndlaeggelsesDatetime().compareTo(o2.getIndlaeggelsesDatetime());
    }
}
