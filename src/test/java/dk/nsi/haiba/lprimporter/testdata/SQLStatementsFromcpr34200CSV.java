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
package dk.nsi.haiba.lprimporter.testdata;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/*
 * Standalone class made only to generate SQL statements from the csv files with testdata
 * csv files are exported from the spreadsheet (Trifork testeksempler.xlsx)
 */
public class SQLStatementsFromcpr34200CSV {
	
	public void generateSQL() {
		
        try {
            generateAdministrationData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void generateAdministrationData() throws IOException {
		File file = FileUtils.toFile(getClass().getClassLoader().getResource("data/cpr34200.csv"));
		boolean first = true;
		List<String> lines = FileUtils.readLines(file);
	
		for (String line : lines) {
			if(first) {
				// first row is column metadata
				first=false;
				continue;
			}
			
			String[] splits = line.split(";");
			String recnum=splits[0];
			String sygehus=splits[1];
			String afdeling=splits[2];
			String type=splits[3];
			String cpr=splits[4];
			String idate=splits[5];
			String udate=splits[6];
			
			String itime=splits[7];
			if(itime.length() == 0) {
				itime="0";
			}
			String utime=splits[8];
			if(utime.length() == 0) {
				utime="0";
			}
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, V_CPR, D_INDDTO,D_UDDTO,V_INDTIME,V_UDTIME, C_PATTYPE) VALUES (");
			sql.append(recnum);
			sql.append(", '");
			sql.append(sygehus);
			sql.append("', '");
			sql.append(afdeling);
			sql.append("', '");
			sql.append(cpr);
			sql.append("', '");
			sql.append(idate);
			sql.append("', '");
			sql.append(udate);
			sql.append("', ");
			sql.append(itime);
			sql.append(", ");
			sql.append(utime);
			sql.append(", ");
			sql.append(type);
			sql.append(");");
			
			System.out.println(sql.toString());
		}
	}

	public static void main(String[] args) {
		
		SQLStatementsFromcpr34200CSV testDataGenerator = new SQLStatementsFromcpr34200CSV();
		
		testDataGenerator.generateSQL();
		
	}

}
