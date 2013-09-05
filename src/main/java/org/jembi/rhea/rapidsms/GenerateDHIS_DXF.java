package org.jembi.rhea.rapidsms;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateDHIS_DXF {
	
	public static final String program = "xlMzncNto5k";
	public static final String orgUnit = "nOHYeEmgwQs";
	public static final String dataSetID = "dataSetID";
	public static final String pregnancySuspectedId = "YnxgptVUwPI";
	public static final String pregnancyConfirmedId = "ZC64lHc3Khz";
	
	public static String generateDHIS_DXF(boolean confirmedPregnancy, Date date) {
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdfDate.format(date);
		
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
					 "<event program=\"" + program + "\" orgUnit=\"" + orgUnit  + "\" eventDate=\"" + dateStr + "\" completed=\"true\" storedBy=\"admin\">\n" +
					 "	<coordinate latitude=\"59.8\" longitude=\"10.9\" />\n" +
					 "	<dataValues>\n";
		if (confirmedPregnancy) {
			xml += "		<dataValue dataElement=\"" + pregnancyConfirmedId + "\" value=\"1\"/>\n";
		} else {
			xml += "		<dataValue dataElement=\"" + pregnancySuspectedId + "\" value=\"1\"/>\n";
		}
		xml += "	</dataValues>\n";
		xml += "</event>";
		
		return xml;
	}

}
