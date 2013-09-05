package org.jembi.rhea.rapidsms;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateDHIS_DXF {
	
	public static final String orgUnit = "testOrgUnit";
	public static final String dataSetID = "dataSetID";
	public static final String dataElementID = "dataElementID";
	public static final String pregnancySuspected = "Suspected";
	public static final String pregnancyConfirmed = "Confirmed";
	
	public static String generateDHIS_DXF(boolean confirmedPregnancy, Date date) {
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfPeriod = new SimpleDateFormat("yyyyMM");
		String dateStr = sdfDate.format(date);
		String periodStr = sdfPeriod.format(date);
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
					 "<dataValueSet xmlns=\"http://dhis2.org/schema/dxf/2.0\" dataSet=\"" + dataSetID + "\"" +
					 	"completeDate=\""+ dateStr +"\" period=\"" + periodStr + "\" orgUnit=\"" + orgUnit + "\">";
		if (confirmedPregnancy) {
			xml += "<dataValue dataElement=\"" + dataElementID + "\" value=\"" + pregnancyConfirmed + "\"/>";
		} else {
			xml += "<dataValue dataElement=\"" + dataElementID + "\" value=\"" + pregnancySuspected + "\"/>";
		}
		xml += "</dataValueSet>";
		
		return xml;
	}

}
