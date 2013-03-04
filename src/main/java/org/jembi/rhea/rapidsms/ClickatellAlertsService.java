package org.jembi.rhea.rapidsms;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.module.client.MuleClient;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;

@Path("/openmrs/ws/rest/RHEA")
public class ClickatellAlertsService implements MuleContextAware {
	
	Log log = LogFactory.getLog(this.getClass());
	
	private MuleContext context;
	
	@Path("patient/encounters")
	@POST
	public Response processEncounter(String body, @QueryParam("patientId") String pid, @QueryParam("idType") String idType) {
		log.info("HIMSS 2013 OpenHIE Demo: Called Clickatell Alerts Service");
		
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
					new WaitForNextPhase(new MuleClient(context)).start();
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

	@Override
	public void setMuleContext(MuleContext context) {
		this.context = context;
	}
	
	private static class WaitForNextPhase extends Thread {
		Log log = LogFactory.getLog(this.getClass());
		MuleClient client;
		
		public WaitForNextPhase(MuleClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			log.info("HIMSS 2013 OpenHIE Demo: Waiting for 12 April before sending alert message...");
			
			//Wait for 12 Apr, then send SMS
			Calendar now = new GregorianCalendar();
			while (now.get(Calendar.MONTH)!=Calendar.APRIL || now.get(Calendar.DAY_OF_MONTH)!=12) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				now.setTimeInMillis(System.currentTimeMillis());
			}
			try {
				client.dispatch("vm://sendClickatellSMS", "Go message go!", null);
				log.info("HIMSS 2013 OpenHIE Demo: Sent Clickatell alert message");
			} catch (MuleException e) {
				log.error("HIMSS 2013 OpenHIE Demo: Failed to send Clickatell alert message", e);
			}
		}
	}
}
