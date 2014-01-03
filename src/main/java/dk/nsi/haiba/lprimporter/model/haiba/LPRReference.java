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
package dk.nsi.haiba.lprimporter.model.haiba;


/*
 * Model object for a LPR reference, an Indlaeggelse can have a series of these
 */
public class LPRReference {
	
	private long lprRecordNumber;
	private int dbId;

	public int getDbId() {
        return dbId;
    }

    public LPRReference() {
		// empty default constructor
	}
	
	public LPRReference(long lprRecordNumber) {
		this.lprRecordNumber = lprRecordNumber;
	}

	public LPRReference(int dbId, long recordNumber) {
        this.dbId = dbId;
        lprRecordNumber = recordNumber;
    }

    public long getLprRecordNumber() {
		return lprRecordNumber;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LPRReference other = (LPRReference) obj;
        if (dbId != other.dbId)
            return false;
        if (lprRecordNumber != other.lprRecordNumber)
            return false;
        return true;
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dbId;
        result = prime * result + (int) (lprRecordNumber ^ (lprRecordNumber >>> 32));
        return result;
    }
}
