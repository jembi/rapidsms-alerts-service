/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jembi.rhea.rapidsms;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;

@Path("/openmrs/ws/rest/RHEA")
public class RapidSMSAlertsService {
	
	Log log = LogFactory.getLog(this.getClass());
	
	@Path("patient/encounters")
	@POST
	public Response processEncounter(String body, @QueryParam("patientId") String pid, @QueryParam("idType") String idType) {
		log.info("Called RapidSMS Alerts Service: process encounter");
		
		Parser p = new GenericParser();
		
		Message hl7 = null;
		try {
			hl7 = p.parse(body);
			
			ORU_R01 oruMsg = (ORU_R01) hl7;
			
			if (oruMsg != null) {
				log.info("Successfully parsed body as HL7v2 ORU_R01 message");
				//TODO determine result of alert request
				if ((oruMsg.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv14_AdmissionType().getValue()).toLowerCase().contains("referral") &&
					!(oruMsg.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv14_AdmissionType().getValue()).toLowerCase().contains("confirmation")) {
				//if (true) {
					sendRapidSMSAlert(oruMsg, pid, idType);
					log.info("Sent RapidSMS alert message");
				} else {
					log.info("ORU_R01 message is not a referral, no alert message sent");
				}
				return Response.created(null).build();
			}
		} catch (Exception e) {
			log.error("Parsing failed: ", e);
			return Response.status(400).entity("Failed to parse body as HL7v2 ORU_R01 message: " + e).build();
		}
		
		return Response.status(500).build();
	}
	
	
	private void sendRapidSMSAlert(ORU_R01 oruMsg, String pid, String idType) {
		GenerateORU_R01Alert alert = new GenerateORU_R01Alert(); 
		try {

			ORU_R01 r01 = alert.generateORU_R01Message(oruMsg, pid, idType);

			GenericParser parser = new GenericParser();
			String msg = null;
			try {
				msg = parser.encode(r01,"XML");
				log.info("Reminder message is : " + msg);
			}
			catch (HL7Exception ex) {
				log.error("Exception parsing constructed message.");
			}
			alert.sendRequest(msg);			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
