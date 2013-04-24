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
public class SQLStatementsFromCPR83174CSV {
	
	public void generateSQL() {
		
        try {
            generateAdministrationData();
            generateProceduresData();
            generateDiagnosesData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void generateAdministrationData() throws IOException {
		File file = FileUtils.toFile(getClass().getClassLoader().getResource("data/cpr83174ADM.csv"));
		boolean first = true;
		List<String> lines = FileUtils.readLines(file);
	
		for (String line : lines) {
			if(first) {
				// first row is column metadata
				first=false;
				continue;
			}
			//v_recnum;c_sgh;c_afd;c_pattype;v_cpr;d_inddto;d_uddto;v_indtime;v_udtime
			
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
			sql.append("INSERT INTO T_ADM (v_RECNUM, C_SGH, C_AFD, V_CPR, D_INDDTO,D_UDDTO,V_INDTIME,V_UDTIME, C_PATTYPE) VALUES (");
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


	private void generateProceduresData() throws IOException {
		File file = FileUtils.toFile(getClass().getClassLoader().getResource("data/cpr83174PROC.csv"));
		boolean first = true;
		List<String> lines = FileUtils.readLines(file);
		for (String line : lines) {
			if(first) {
				// first row is column metadata
				first=false;
				continue;
			}
			//V_RECNUM;C_OPR;D_ODTO;V_OTIME;C_TILOPR;C_OSGH;C_OAFD
			
			String[] splits = line.split(";");
			String recnum=splits[0];
			String code=splits[1];
			String odate=splits[2];
			String otime=splits[3];
			if(otime.length() == 0) {
				otime="0";
			}
			String tillaeg=splits[4];
			String sygehus=splits[5];
			String afdeling=splits[6];
			String type="A";
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO T_PROCEDURER (V_RECNUM, C_OPR, C_TILOPR, C_OPRART, D_ODTO, V_OTIME, C_OSGH, C_OAFD) VALUES (");
			sql.append(recnum);
			sql.append(", '");
			sql.append(code);
			sql.append("', '");
			sql.append(tillaeg);
			sql.append("', '");
			sql.append(type);
			sql.append("', '");
			sql.append(odate);
			sql.append("', ");
			sql.append(otime);
			sql.append(", '");
			sql.append(sygehus);
			sql.append("', '");
			sql.append(afdeling);
			sql.append("');");
			
			System.out.println(sql.toString());
		}
	}


	private void generateDiagnosesData() throws IOException {
		File file = FileUtils.toFile(getClass().getClassLoader().getResource("data/cpr83174DIAG.csv"));
		boolean first = true;
		List<String> lines = FileUtils.readLines(file);
		for (String line : lines) {
			if(first) {
				// first row is column metadata
				first=false;
				continue;
			}
			// V_RECNUM;C_DIAG;C_TILDIAG

			String[] splits = line.split(";");
			String recnum=splits[0];
			String code=splits[1];
			String tillaeg=splits[2];
			String type="A";
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO T_DIAG (V_RECNUM, C_DIAG, C_TILDIAG, C_DIAGTYPE) VALUES (");
			sql.append(recnum);
			sql.append(", '");
			sql.append(code);
			sql.append("', '");
			sql.append(tillaeg);
			sql.append("', '");
			sql.append(type);
			sql.append("');");
			
			System.out.println(sql.toString());
		}
	}
	
	
	public static void main(String[] args) {
		
		SQLStatementsFromCPR83174CSV testDataGenerator = new SQLStatementsFromCPR83174CSV();
		
		testDataGenerator.generateSQL();
		
	}

}
