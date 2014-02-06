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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO.CheckStructure;
import dk.nsi.haiba.lprimporter.email.EmailSender;

public class EmailFormatTest {
    @Test
    public void testSorting() {
        List<CheckStructure> randomOrder = generate(new String[][] { { "1v50", "is2" }, { "1v50", "is1" },
        { "1v50", "is10" }, { "1v49", "is" }, {"2vr", null} });
        List<CheckStructure> sorted = EmailSender.sort(randomOrder);
        assertTrue("first element was " + sorted.get(0), sorted.get(0).getCode().equals("1v49"));
        assertTrue("second element was " + sorted.get(1), sorted.get(1).getSecondaryCode().equals("is1"));
        assertTrue("third element was " + sorted.get(2), sorted.get(2).getSecondaryCode().equals("is2"));
        assertTrue("fourth element was " + sorted.get(3), sorted.get(3).getSecondaryCode().equals("is10"));
        assertNull("fifth element was " + sorted.get(4), sorted.get(4).getSecondaryCode());
    }

    private List<CheckStructure> generate(String[][] strings) {
        List<CheckStructure> returnValue = new ArrayList<CheckStructure>();
        for (String[] strings2 : strings) {
            returnValue
                    .add(new ClassificationCheckHelper.CheckStructureImpl(strings2[0], strings2[1], null, null, null));
        }
        return returnValue;
    }
}
