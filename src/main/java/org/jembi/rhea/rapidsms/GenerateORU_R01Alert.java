/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.jembi.rhea.rapidsms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.util.Terser;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public class GenerateORU_R01Alert implements Serializable {

	private Log log = LogFactory.getLog(this.getClass());

	public static String username = "";
	public static String password = "";
	public static String hostname = "";

	public static SSLSocketFactory sslFactory;

	private static final long serialVersionUID = 1L;
	private ORU_R01 r01 = new ORU_R01();

	public ORU_R01 generateORU_R01Message(ORU_R01 encounter, String patientId, String idType) throws Exception {	
		
		MSH msh = r01.getMSH();
		Terser t = new Terser(encounter);

		// Get current date
		String dateFormat = "yyyyMMddHHmmss";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		String formattedDate = formatter.format(new Date());

		msh.getFieldSeparator().setValue(RHEAHL7Constants.FIELD_SEPARATOR);//
		msh.getEncodingCharacters().setValue(
				RHEAHL7Constants.ENCODING_CHARACTERS);//
		msh.getVersionID().getInternationalizationCode().getIdentifier()
				.setValue(RHEAHL7Constants.INTERNATIONALIZATION_CODE);//
		msh.getVersionID().getVersionID().setValue(RHEAHL7Constants.VERSION);//
		msh.getDateTimeOfMessage().getTime().setValue(formattedDate);//
		msh.getSendingApplication()
				.getNamespaceID()
				.setValue("316");
		msh.getSendingFacility().getNamespaceID().setValue("RwandaMOH");//
		msh.getMessageType().getMessageCode()
				.setValue(RHEAHL7Constants.MESSAGE_TYPE);//
		msh.getMessageType().getTriggerEvent()
				.setValue(RHEAHL7Constants.TRIGGER_EVENT);//
		msh.getMessageType().getMessageStructure()
				.setValue(RHEAHL7Constants.MESSAGE_STRUCTURE);//
		msh.getReceivingFacility().getNamespaceID()
				.setValue(RHEAHL7Constants.RECEIVING_FACILITY);//
		msh.getProcessingID().getProcessingID()
				.setValue(RHEAHL7Constants.PROCESSING_ID);//
		msh.getProcessingID().getProcessingMode()
				.setValue(RHEAHL7Constants.PROCESSING_MODE);//
		msh.getMessageControlID().setValue(UUID.randomUUID().toString());//

		msh.getAcceptAcknowledgmentType().setValue(RHEAHL7Constants.ACK_TYPE);
		msh.getApplicationAcknowledgmentType().setValue(
				RHEAHL7Constants.APPLICATION_ACK_TYPE);
		msh.getMessageProfileIdentifier(0).getEntityIdentifier()
				.setValue("ALERT");


		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();

		pid.getPatientIdentifierList(0)
				.getIDNumber().setValue(patientId);
		pid.getPatientIdentifierList(0).getIdentifierTypeCode()
				.setValue(idType);
		pid.getPatientName(0).getFamilyName().getSurname()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/PID-5-1"));
		pid.getPatientName(0).getGivenName()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/PID-5-2"));

		// gender
		// pid.getAdministrativeSex().setValue(pat.getGender());

		// dob
		//Date dob = encounter.getPatient().getBirthdate();
		//String dobStr = "";
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		//dobStr = sdf.format(dob);
		//pid.getDateTimeOfBirth().getTime().setValue(dobStr);

		PV1 pv1 = r01.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();

		pv1.getPatientClass().setValue(RHEAHL7Constants.PATIENT_CLASS);
		
		pv1.getAssignedPatientLocation().getFacility().getNamespaceID()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-3-4-1"));
		
		pv1.getAssignedPatientLocation().getPointOfCare()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-3-1"));
		pv1.getAdmissionType().setValue("ALERT");

		//Map<Integer, String> providerIdentifierMap = null;

		//pv1.getAttendingDoctor(0).getIDNumber().setValue("e8597a14-436f-1031-8b61-8d373bf4f88f");
		pv1.getAttendingDoctor(0).getIDNumber().setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-7-1"));

		pv1.getAttendingDoctor(0).getFamilyName().getSurname()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-7-2-1"));
		pv1.getAttendingDoctor(0).getGivenName()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-7-3"));
		pv1.getAdmitDateTime()
				.getTime()
				.setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-44-1"));

		pv1.getAttendingDoctor(0).getIdentifierTypeCode().setValue(t.get("/PATIENT_RESULT/PATIENT/VISIT/PV1-7-13"));

		r01 = createOBRSegment(r01);

		return r01;

	}

	private ORU_R01 createOBRSegment(ORU_R01 r01) throws HL7Exception {
		OBR obr = null;

		obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR();
		obr.getSetIDOBR().setValue(String.valueOf(0));

		obr.getUniversalServiceIdentifier().getText().setValue("ALERT");

		OBX obx = r01.getPATIENT_RESULT().getORDER_OBSERVATION(0)
				.getOBSERVATION(0).getOBX();

		obx.getSetIDOBX().setValue("0");

		obx.getObservationIdentifier().getIdentifier().setValue("rsms_rm");
		obx.getObservationIdentifier().getText().setValue("rsms_rm REMINDER");
		obx.getObservationIdentifier().getNameOfCodingSystem().setValue("RSMS");

		obx.getValueType().setValue("CE");
		CE ce = new CE(r01);
		ce.getText().setValue("rsms_pmr PATIENT MISSED REFERRAL");
		ce.getIdentifier().setValue("rsms_pmr");
		ce.getNameOfCodingSystem().setValue("RSMS");

		obx.getObservationValue(0).setData(ce);

		return r01;

	}

	public void sendRequest(String msg) throws IOException,
			TransformerFactoryConfigurationError, TransformerException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException,
			KeyManagementException {
		
		//log.info("Sending to RapidSMS:\n" + msg);
		
		// Get the key store that includes self-signed cert as a "trusted"
		// entry.
		InputStream keyStoreStream = org.mule.util.IOUtils.getResourceAsStream("truststore.jks", GenerateORU_R01Alert.class);

		// Load the keyStore

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(keyStoreStream, "Jembi#123".toCharArray());
		//log.info("KeyStoreStream = " + IOUtils.toString(keyStoreStream));
		keyStoreStream.close();

		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, tmf.getTrustManagers(), null);

		// set SSL Factory to be used for all HTTPS connections
		sslFactory = ctx.getSocketFactory();

		callQueryFacility(msg);

	}

	private static void addHTTPBasicAuthProperty(HttpsURLConnection conn) {
		String userpass = username + ":" + password;
		@SuppressWarnings("restriction")
		String basicAuth = "Basic "
				+ new String(DatatypeConverter.printBase64Binary(userpass
						.getBytes()));
		conn.setRequestProperty("Authorization", basicAuth);
	}

	public String callQueryFacility(String msg) throws IOException,
			TransformerFactoryConfigurationError, TransformerException {
		
		
		// Setup connection
		URL url = new URL(hostname + "/ws/rest/v1/alerts");
		System.out.println("full url " + url);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);

		// This is important to get the connection to use our trusted
		// certificate
		conn.setSSLSocketFactory(sslFactory);

		addHTTPBasicAuthProperty(conn);
		// conn.setConnectTimeout(timeOut);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		log.error("body" + msg);
		out.write(msg);
		out.close();
		conn.connect();

		// Test response code
		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		String result = convertInputStreamToString(conn.getInputStream());
		conn.disconnect();

		return result;
	}

	private static String convertInputStreamToString(InputStream is)
			throws IOException {
		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}
		rd.close();
		return sb.toString();
	}

}
