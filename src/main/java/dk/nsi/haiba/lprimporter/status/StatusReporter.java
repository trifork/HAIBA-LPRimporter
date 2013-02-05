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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * This class is responsible for showing a statuspage, this page contains information about the general health of the application.
 * If it returns HTTP 200, no errors are detected
 * If it returns HTTP 500, an error is detected and must be taken care of before further operation. 
 */
@Controller
public class StatusReporter {
	
	@Autowired
	ImportStatusRepository statusRepo;

	@RequestMapping(value = "/status")
	public ResponseEntity<String> reportStatus() {
		HttpHeaders headers = new HttpHeaders();
		String body = "OK";
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		body = "OK";
		
		try {
			if (!statusRepo.isHAIBADBAlive()) {
				body = "HAIBA Database is _NOT_ running correctly";
			} else if (!statusRepo.isLPRDBAlive()) {
				body = "LPR Database is _NOT_ running correctly";
			} else if (statusRepo.isOverdue()) {
				// last run information is applied to body later
				body = "Is overdue";
			} else {
				status = HttpStatus.OK;
			}
		} catch (Exception e) {
			body = e.getMessage();
		}

		body = addLastRunInformation(body);
		headers.setContentType(MediaType.TEXT_PLAIN);
		
		return new ResponseEntity<String>(body, headers, status);
	}

	
	private String addLastRunInformation(String body) {
		ImportStatus latestStatus = statusRepo.getLatestStatus();
		if (latestStatus == null) {
			return body + "\nLast import: Never run";
		} else {
			return body + "\n" + latestStatus.toString();
		}
	}
	
}
