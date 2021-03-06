package tk.giesecke.cctvview.Onvif;

import android.annotation.SuppressLint;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.mina.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static tk.giesecke.cctvview.CCTVview.selectedDevice;

public class OnvifHeaderBody {

	private static String utcTime;
	private static String nonce;

	public static String getAuthorizationHeader() {
		nonce = "" + new Random().nextInt();
		utcTime = getUTCTime();
		String envelopePart;
		String authorizationPart = "";
		envelopePart = "<SOAP-ENV:Envelope " +
				"xmlns:SOAP-ENV=\"http://www.w3.org/2003/05/soap-envelope\" " +
				"xmlns:SOAP-ENC=\"http://www.w3.org/2003/05/soap-encoding\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
				"xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
				"xmlns:wsdd=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" " +
				"xmlns:chan=\"http://schemas.microsoft.com/ws/2005/02/duplex\" " +
				"xmlns:wsa5=\"http://www.w3.org/2005/08/addressing\" " +
				"xmlns:xmime=\"http://tempuri.org/xmime.xsd\" " +
				"xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" " +
				"xmlns:tt=\"http://www.onvif.org/ver10/schema\" " +
				"xmlns:wsrfbf=\"http://docs.oasis-open.org/wsrf/bf-2\" " +
				"xmlns:wstop=\"http://docs.oasis-open.org/wsn/t-1\" " +
				"xmlns:wsrfr=\"http://docs.oasis-open.org/wsrf/r-2\" " +
				"xmlns:tdn=\"http://www.onvif.org/ver10/network/wsdl\" " +
				"xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" " +
				"xmlns:tev=\"http://www.onvif.org/ver10/events/wsdl\" " +
				"xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\" " +
				"xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\" " +
				"xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\">";
		if (!selectedDevice.userName.equalsIgnoreCase("")) {
			authorizationPart = "<SOAP-ENV:Header>" +
					"<wsse:Security SOAP-ENV:mustUnderstand=\"true\">" +
					"<wsu:Timestamp wsu:Id=\"Time\">" +
					"<wsu:Created>" +
					utcTime +
					"</wsu:Created>" +
					"</wsu:Timestamp>" +
					"<wsse:UsernameToken>" +
					"<wsse:Username>" +
					selectedDevice.userName +
					"</wsse:Username>" +
					"<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" +
					encryptPassword(selectedDevice.passWord) +
					"</wsse:Password>" +
					"<wsse:Nonce>" +
					nonce +
					"</wsse:Nonce>" +
					"<wsu:Created>" +
					utcTime +
					"</wsu:Created>" +
					"</wsse:UsernameToken>" +
					"</wsse:Security>" +
					"</SOAP-ENV:Header>";
		}
		return envelopePart + authorizationPart + "<SOAP-ENV:Body>";
	}

	@SuppressWarnings("SameReturnValue")
	public static String getEnvelopeEnd() {
		return "</SOAP-ENV:Body></SOAP-ENV:Envelope>";
	}

	private static String encryptPassword(String password) {
		String beforeEncryption = nonce + utcTime + password;
		byte[] encryptedRaw;
		try {
			MessageDigest SHA1 = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1);
			SHA1.reset();
			SHA1.update(beforeEncryption.getBytes());
			encryptedRaw = SHA1.digest();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		String encodedString = new String(Base64.encodeBase64(encryptedRaw));
		encodedString = encodedString.replace('+','-').replace('/','_');
		return encodedString;
	}

	private static String getUTCTime() {
		@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss'Z'");
		sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		return sdf.format(cal.getTime());
	}

	public static String simpleSoapFormatter(String unformattedStr) {
		return unformattedStr.replace("><", ">\n<");
	}
}
