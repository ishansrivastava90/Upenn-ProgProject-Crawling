package edu.upenn.cis455.crawler;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.SocketClient;

public class MedCrawler3_19 {
	
	private static final String HOST = "http://publicindex.sccourts.org";
	private static final String SEARCH_PAGE = "PISearch.aspx";
	private static final String ERROR_PAGE = "PIError.aspx";
	
	private static final String viewStateVal;
	static {
		// Read VIEWSTATE value from the file		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader vbr = new BufferedReader(new FileReader("param_viewstate_val_marlboro"));
			String line  = "";			
			while ((line = vbr.readLine()) != null) {
				sb.append(line);
			}
			vbr.close();
		} catch (Exception e) {
			System.err.println("Exception while getting vVIEWSTATE param val " + e.getMessage());
		}
		viewStateVal = sb.toString();		
	}
	
	private static final String eventValidationVal;
	static {
		// Read EVENT_VALIDATION value from the file		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader vbr = new BufferedReader(new FileReader("param_eventvalidation_val_marlboro"));
			String line  = "";			
			while ((line = vbr.readLine()) != null) {
				sb.append(line);
			}
			vbr.close();
		} catch (Exception e) {
			System.err.println("Exception while getting vVIEWSTATE param val " + e.getMessage());
		}
		eventValidationVal = sb.toString();		
	}

	public static String getCaptchaImgUrl(Document doc) {
		if (doc == null) return null;
		
		Element captchaDiv = 
			doc.getElementById("c_pierror_contentplaceholder1_botdetectcaptcha_CaptchaImageDiv");
		
		if (captchaDiv == null) return null;
		
		Elements imgElements = captchaDiv.getElementsByTag("img");
		if (imgElements == null || imgElements.size() != 1) {
			System.out.println("No captcha image or multiple images");
			return null;
		}
		Element imgElement = imgElements.get(0);
		String captcha_url = HOST + imgElement.attr("src");
		System.out.println(captcha_url);
		
		return captcha_url;
	}
	

	private static String getCaptchaId(String captchaImgUrl) throws URISyntaxException {
		URI captchaUrl = new URI(captchaImgUrl);
		String query = captchaUrl.getQuery();
		String [] queryParams = query.split("&");
		
		for (int i = 0; i < queryParams.length; ++i) {
			String [] keyVals = queryParams[i].split("=");
			if (keyVals.length == 2 && "t".equals(keyVals[0])) {
				System.out.println(keyVals[1]);
				return keyVals[1];
			}
		}			
		return null;
	}


	
	private static String getCaptchaText() {
		SocketClient client = new SocketClient("ishanpenn", "ishanpenn011235");

		Captcha captcha = null;                                                                                     
		try {
			captcha = client.decode("captcha3.jpg", 120);
		} catch (Exception e) {
			System.out.println("Failed uploading CAPTCHA - " + e.getMessage());                                                         
		}
		if (null != captcha) {
			System.out.println("CAPTCHA " + captcha.id + " solved: " + captcha.text);
			return captcha.text;
		}
		return null;
	}

	
	public static boolean extractAndSaveCaptcha(String captchaImgUrl, String url) throws IOException {
		URL captchaUrl = new URL(captchaImgUrl);
		
		HttpURLConnection connection = (HttpURLConnection) captchaUrl.openConnection();
	//	connection.setRequestMethod(method);

		connection.setRequestProperty("Accept",	"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// connection.setRequestProperty("Accept-Encoding","gzip, deflate");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		connection.setRequestProperty("Cache-Control", "max-age=0");
		connection.setRequestProperty("Connection", "keep-alive");
		//connection.setRequestProperty("Cookie", "gsScrollPos=0; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQUIPAAAAAABTX0gXoHBCygATrHOHndYo; incap_ses_237_666243=6hJUSqmHXxXvxvnpcf9JA4Y65FYAAAAAYZDl38DshtltL02LUipAuw==; __utma=152568635.1581997736.1455313479.1457760018.1457801266.5; __utmc=152568635; __utmz=152568635.1457801266.5.3.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PIError.aspx|utmcmd=referral; incap_ses_221_666243=NEmICnShS0SX+lRFkycRAwB25FYAAAAA4DJAun04OQlA3OzfatELUQ==; ___utmvmfOuFiPi=SwNHxuwWpzo; ___utmvbfOuFiPi=yZf XBeOFalt: StL");
		//--connection.setRequestProperty("Cookie", "gsScrollPos=; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457801266.1457992530.6; __utmc=152568635; __utmz=152568635.1457992530.6.4.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PISearch.aspx|utmcmd=referral; incap_ses_237_666243=IHKQD7qHejvHuxLycf9JA+cp6FYAAAAAogDqXvvVsNzDwcGdzBj1xA==; incap_ses_221_666243=sdfRLD/k/Bs7Qo9RkycRA+446FYAAAAAsTemAa09ur86QHpwFWsn+Q==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQkIPAAAAAACA68hyAZ6C1b1n2tMjJhoix/6MhmdjwyZ2; incap_ses_220_666243=ZfIIcM76clNhsT6XFaINA57S6VYAAAAAF+ljJJyyzMtRxpCGYBiKgQ==");
		//connection.setRequestProperty("Cookie", "");
		connection.setRequestProperty("Cookie", "visid_incap_644691=iPE+n3PDQASi8AKN6cU7wZdxBFcAAAAAQUIPAAAAAAByzPw7rtavpS06TJ2pjzpu; visid_incap_709850=b/j/BhVoQW2Vw7ouz1rlN1zrAVcAAAAAQUIPAAAAAABDJ8pS9isahVmtOAK4ng3G; AspxAutoDetectCookieSupport=1; ASP.NET_SessionId=agzwdlqh51nb233inlbj0sg2; visid_incap_666243=igzswpA9QSiGcrFYOTnDbl7rAVcAAAAAVEIPAAAAAACAmQ90AZ6CF0K5G3/kxzeVWIWDmFdCbznP; incap_ses_220_666243=9yrHGir/2GujHFd0KqINAxmwM1cAAAAAezSZP/u1Hju4xjzcdxp9GA==");
        connection.setRequestProperty("Host", "publicindex.sccourts.org");
		connection.setRequestProperty("Origin", "http://publicindex.sccourts.org");
		connection.setRequestProperty("Referer", url + "/" + SEARCH_PAGE);
		//connection.setRequestProperty("Referer", "http://publicindex.sccourts.org/Florence/PublicIndex/PISearch.aspx");
		connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
		connection.setRequestProperty("User-Agent",	"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");

		//connection.setDoOutput(true); 
		InputStream is1 = connection.getInputStream();
		if (is1 == null) {
			System.out.println("Error fetching captcha image. InputStream NULL");
			return false;
		}
		try {
			System.out.println("Saving captcha file");
			FileOutputStream out = new FileOutputStream("captcha3.jpg");
			BufferedImage image = ImageIO.read(is1);				
			ImageIO.write(image, "jpg", out);
			
			
		} catch (Exception e) {
			System.out.println("Captcha issues while writing image - " + e.getMessage());
			is1.close();
			return false;
			
		}
		is1.close();
		return true;
	}
	

	private static void revalidateWithCaptcha(String url, String captchaText, String captchaId) throws IOException {
		
		URL urlInstance = new URL(url + "/" + ERROR_PAGE);
		HttpURLConnection connection = (HttpURLConnection) urlInstance.openConnection();
	//	connection.setRequestMethod(method);

		connection.setRequestProperty("Accept",	"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// connection.setRequestProperty("Accept-Encoding","gzip, deflate");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		connection.setRequestProperty("Cache-Control", "max-age=0");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Content-Length", "114506");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		//connection.setRequestProperty("Cookie", "gsScrollPos=0; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; incap_ses_237_666243=6hJUSqmHXxXvxvnpcf9JA4Y65FYAAAAAYZDl38DshtltL02LUipAuw==; __utma=152568635.1581997736.1455313479.1457760018.1457801266.5; __utmc=152568635; __utmz=152568635.1457801266.5.3.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PIError.aspx|utmcmd=referral; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQUIPAAAAAABTX0gXoHBCygATrHOHndYo; incap_ses_221_666243=NEmICnShS0SX+lRFkycRAwB25FYAAAAA4DJAun04OQlA3OzfatELUQ==");
		//--connection.setRequestProperty("Cookie", "gsScrollPos=; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457801266.1457992530.6; __utmc=152568635; __utmz=152568635.1457992530.6.4.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PISearch.aspx|utmcmd=referral; incap_ses_237_666243=IHKQD7qHejvHuxLycf9JA+cp6FYAAAAAogDqXvvVsNzDwcGdzBj1xA==; incap_ses_221_666243=sdfRLD/k/Bs7Qo9RkycRA+446FYAAAAAsTemAa09ur86QHpwFWsn+Q==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQkIPAAAAAACA68hyAZ6C1b1n2tMjJhoix/6MhmdjwyZ2; incap_ses_220_666243=ZfIIcM76clNhsT6XFaINA57S6VYAAAAAF+ljJJyyzMtRxpCGYBiKgQ==");
		connection.setRequestProperty("Cookie", "visid_incap_644691=iPE+n3PDQASi8AKN6cU7wZdxBFcAAAAAQUIPAAAAAAByzPw7rtavpS06TJ2pjzpu; visid_incap_709850=b/j/BhVoQW2Vw7ouz1rlN1zrAVcAAAAAQUIPAAAAAABDJ8pS9isahVmtOAK4ng3G; AspxAutoDetectCookieSupport=1; ASP.NET_SessionId=agzwdlqh51nb233inlbj0sg2; visid_incap_666243=igzswpA9QSiGcrFYOTnDbl7rAVcAAAAAVEIPAAAAAACAmQ90AZ6CF0K5G3/kxzeVWIWDmFdCbznP; incap_ses_220_666243=9yrHGir/2GujHFd0KqINAxmwM1cAAAAAezSZP/u1Hju4xjzcdxp9GA==");
		connection.setRequestProperty("Host", "publicindex.sccourts.org");
		connection.setRequestProperty("Origin", "http://publicindex.sccourts.org");
		connection.setRequestProperty("Referer", url + "/" + SEARCH_PAGE);
		//connection.setRequestProperty("Referer", "http://publicindex.sccourts.org/Florence/PublicIndex/PISearch.aspx");
		connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
		connection.setRequestProperty("User-Agent",	"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");

		Map<String, String> params = new HashMap<>();
		
		params.put("__VIEWSTATE", "/wEPDwUJMTk2NDQ4NzY3ZGSyVN+hDmhnQydWotmzaFskr+H64JVtRRkpzpRDLS7h1w==");
		params.put("__VIEWSTATEGENERATOR", "82DBEBFC");
		params.put("__EVENTVALIDATION",	"/wEdAAPXSf1mZlO2HJ26LOF37nkpX3LD91CpZ3r7YmjGY3PROg7m+kMKY3IaT2Nwgw+sZrwqeJr1OEeRUugUoKfer9ERT6tFdsWB3CbPmoDnOKwpDQ==");
		params.put("LBD_VCID_c_pierror_contentplaceholder1_botdetectcaptcha", captchaId);		
		params.put("ctl00$ContentPlaceHolder1$BotDetectCaptchaCode", captchaText);
		params.put("ctl00$ContentPlaceHolder1$BotDetectCaptchaButton", "Validate");
		
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()),
					"UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		connection.setDoOutput(true);
		connection.getOutputStream().write(postDataBytes);
		
		Document doc = Jsoup.parse(connection.getInputStream(), null, "");
		if (doc == null) {
			System.out.println("Response in revalidation - Document is null");
		}
		
		//System.out.println(doc.body().toString());		
		return;

		
	}

	
	public static InputStream getListByFields(String url, String method,
		Map<String, String> fields)	throws IOException {
		
		System.out.println(url + "/" + SEARCH_PAGE);
		System.out.println("Crawling for phrase - " +  fields.get("lastnamephrase") + " and caseType " + fields.get("casetype"));
		
		URL urlInstance = new URL(url + "/" + SEARCH_PAGE);
		HttpURLConnection connection = (HttpURLConnection) urlInstance.openConnection();
	//	connection.setRequestMethod(method);

		connection.setRequestProperty("Accept",	"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// connection.setRequestProperty("Accept-Encoding","gzip, deflate");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
		connection.setRequestProperty("Cache-Control", "max-age=0");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Content-Length", "118506");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		//connection.setRequestProperty("Cookie", "gsScrollPos=0; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457760018.1457801266.5; __utmc=152568635; __utmz=152568635.1457801266.5.3.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PIError.aspx|utmcmd=referral; incap_ses_221_666243=NEmICnShS0SX+lRFkycRAwB25FYAAAAA4DJAun04OQlA3OzfatELUQ==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQUIPAAAAAABTX0gXoHBCygATrHOHndYo; incap_ses_237_666243=Mmsua3ucMlhdtOnqcf9JAzON5FYAAAAAC4aipx8DwInCjF5Jfe9zZg==");
		//--connection.setRequestProperty("Cookie", "gsScrollPos=; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457801266.1457992530.6; __utmc=152568635; __utmz=152568635.1457992530.6.4.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PISearch.aspx|utmcmd=referral; incap_ses_237_666243=IHKQD7qHejvHuxLycf9JA+cp6FYAAAAAogDqXvvVsNzDwcGdzBj1xA==; incap_ses_221_666243=sdfRLD/k/Bs7Qo9RkycRA+446FYAAAAAsTemAa09ur86QHpwFWsn+Q==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQkIPAAAAAACA68hyAZ6C1b1n2tMjJhoix/6MhmdjwyZ2; incap_ses_220_666243=ZfIIcM76clNhsT6XFaINA57S6VYAAAAAF+ljJJyyzMtRxpCGYBiKgQ==");
		connection.setRequestProperty("Cookie", "visid_incap_644691=iPE+n3PDQASi8AKN6cU7wZdxBFcAAAAAQUIPAAAAAAByzPw7rtavpS06TJ2pjzpu; visid_incap_709850=b/j/BhVoQW2Vw7ouz1rlN1zrAVcAAAAAQUIPAAAAAABDJ8pS9isahVmtOAK4ng3G; AspxAutoDetectCookieSupport=1; ASP.NET_SessionId=agzwdlqh51nb233inlbj0sg2; visid_incap_666243=igzswpA9QSiGcrFYOTnDbl7rAVcAAAAAVEIPAAAAAACAmQ90AZ6CF0K5G3/kxzeVWIWDmFdCbznP; incap_ses_220_666243=9yrHGir/2GujHFd0KqINAxmwM1cAAAAAezSZP/u1Hju4xjzcdxp9GA==");
		connection.setRequestProperty("Host", "publicindex.sccourts.org");
		connection.setRequestProperty("Origin", "http://publicindex.sccourts.org");
		connection.setRequestProperty("Referer", url + "/" + SEARCH_PAGE);
		connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
		connection.setRequestProperty("User-Agent",	"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");

		Map<String, String> params = new HashMap<>();
		
		//System.out.println("sdfsdf\n" + viewStateVal);
		
		params.put("__VIEWSTATE", viewStateVal);		
		params.put("__VIEWSTATEGENERATOR", "E3709835");
		//params.put("__EVENTVALIDATION",	"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");
		params.put("__EVENTVALIDATION",	eventValidationVal);
		
		
		params.put("ctl00$ContentPlaceHolder1$NoBotSearch$NoBotSearch_NoBotExtender_ClientState", "-509");
		params.put("ctl00$ContentPlaceHolder1$DropDownListCaseTypes", fields.get("casetype"));
		if (fields.containsKey("lastnamephrase")) {
			params.put("ctl00$ContentPlaceHolder1$TextBoxlastName", fields.get("lastnamephrase"));
		}
		params.put("ctl00$ContentPlaceHolder1$IndexGroup", "RadioButtonIndexAll");
		params.put("ctl00$ContentPlaceHolder1$ButtonSearch", "Search");
		params.put("ctl00$ContentPlaceHolder1$NameSearchOption", "RadioButtonNameBeginsWith");
		
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()),
					"UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		connection.setDoOutput(true);
		connection.getOutputStream().write(postDataBytes);
		
		
		return connection.getInputStream();
		
	}

	public static InputStream getCrawledData(String url, String method, String s_url)
				throws IOException {
			
			System.out.println("Crawling for url - " + url);
			
			URL urlInstance = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlInstance.openConnection();
			connection.setRequestMethod(method);

			connection.setRequestProperty("Accept",	"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			// connection.setRequestProperty("Accept-Encoding","gzip, deflate");
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
			connection.setRequestProperty("Cache-Control", "max-age=0");
			connection.setRequestProperty("Connection", "keep-alive");
			//connection.setRequestProperty("Cookie", "gsScrollPos=0; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457760018.1457801266.5; __utmc=152568635; __utmz=152568635.1457801266.5.3.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PIError.aspx|utmcmd=referral; incap_ses_221_666243=NEmICnShS0SX+lRFkycRAwB25FYAAAAA4DJAun04OQlA3OzfatELUQ==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQUIPAAAAAABTX0gXoHBCygATrHOHndYo; incap_ses_237_666243=Mmsua3ucMlhdtOnqcf9JAzON5FYAAAAAC4aipx8DwInCjF5Jfe9zZg==");
			//--connection.setRequestProperty("Cookie", "gsScrollPos=; ASP.NET_SessionId=j4v3c1cicornin44vq0uqnzl; gsScrollPos=0; __utma=152568635.1581997736.1455313479.1457801266.1457992530.6; __utmc=152568635; __utmz=152568635.1457992530.6.4.utmccn=(referral)|utmcsr=publicindex.sccourts.org|utmcct=/Florence/PublicIndex/PISearch.aspx|utmcmd=referral; incap_ses_237_666243=IHKQD7qHejvHuxLycf9JA+cp6FYAAAAAogDqXvvVsNzDwcGdzBj1xA==; incap_ses_221_666243=sdfRLD/k/Bs7Qo9RkycRA+446FYAAAAAsTemAa09ur86QHpwFWsn+Q==; visid_incap_666243=4ZdB3MViQwiIYS2jB2fd9G9SvlYAAAAAQkIPAAAAAACA68hyAZ6C1b1n2tMjJhoix/6MhmdjwyZ2; incap_ses_220_666243=ZfIIcM76clNhsT6XFaINA57S6VYAAAAAF+ljJJyyzMtRxpCGYBiKgQ==");
		connection.setRequestProperty("Cookie", "visid_incap_644691=iPE+n3PDQASi8AKN6cU7wZdxBFcAAAAAQUIPAAAAAAByzPw7rtavpS06TJ2pjzpu; visid_incap_709850=b/j/BhVoQW2Vw7ouz1rlN1zrAVcAAAAAQUIPAAAAAABDJ8pS9isahVmtOAK4ng3G; AspxAutoDetectCookieSupport=1; ASP.NET_SessionId=agzwdlqh51nb233inlbj0sg2; visid_incap_666243=igzswpA9QSiGcrFYOTnDbl7rAVcAAAAAVEIPAAAAAACAmQ90AZ6CF0K5G3/kxzeVWIWDmFdCbznP; incap_ses_220_666243=9yrHGir/2GujHFd0KqINAxmwM1cAAAAAezSZP/u1Hju4xjzcdxp9GA==");
			connection.setRequestProperty("Host", "publicindex.sccourts.org");
			connection.setRequestProperty("Origin", "http://publicindex.sccourts.org");
			connection.setRequestProperty("Referer", s_url + SEARCH_PAGE);
			connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
			connection.setRequestProperty("User-Agent",	"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
			
			connection.setConnectTimeout(0);
			//connection.setDoOutput(true);
			if (connection.getResponseCode() != 200) {
				return null;
			}
			
			return connection.getInputStream();
			
		}

	
	public static List<String> extractNameLinks(InputStream is, String phrase, String s_url) 
			throws IOException, URISyntaxException {
		Document doc = Jsoup.parse(is, null, "");
		if (doc == null) {
			System.out.println(phrase + " - Document is null");
		}
		if (doc.body().toString().contains("Your search results exceeded the maximum number of records allowed and all records were not returned")) {
			System.out.println(phrase +" Exceeded num of records - 1");
		}

		Element captchaEle = doc.getElementById("c_pierror_contentplaceholder1_botdetectcaptcha_CaptchaDiv");
		if (captchaEle != null) {
			System.out.println("Need to revalidate with captcha");
			String captchaImgUrl = getCaptchaImgUrl(doc);
			String captchaId = getCaptchaId(captchaImgUrl);
			
			System.out.println("Trying to extract captcha...");
			boolean extracted = false;
			while (!extracted) {
				extracted = extractAndSaveCaptcha(captchaImgUrl, s_url);
			}
			System.out.println("Captcha Extraction complete");
			
			String text = getCaptchaText();			
			if (text == null) {
				System.out.println(" No captcha text");
				return null;
			
			}
			revalidateWithCaptcha(s_url, text, captchaId);
			return null;
			
		}
		
		List<String> urls = new ArrayList<>();
		Element table = doc.getElementById("ContentPlaceHolder1_SearchResults");
		if (table == null) {
			System.out.println("Table was null in Extract Name Links");
			return urls;
		}
		
		Elements links = table.getElementsByTag("a");		
		for (Element link: links) {
		    String url = link.attr("href");
		   // String text = link.text();
		    //System.out.println(text + ", " + s_url+"/"+url);
		    urls.add(s_url+"/"+url);
		    
		}		
		return urls;			
	}
	

	public static String extractDetails(InputStream is, String url, String s_url) 
			throws IOException, URISyntaxException {
		Document doc = Jsoup.parse(is, null, "");
		if (doc == null) {
			System.out.println(url + " - Document is null");
		}	
		
		//System.out.println(doc.body());
		
		Element captcha = doc.getElementById("c_pierror_contentplaceholder1_botdetectcaptcha_CaptchaDiv");
		if (captcha != null) {
			
			System.out.println("Need to revalidate with captcha");
			String captchaImgUrl = getCaptchaImgUrl(doc);
			String captchaId = getCaptchaId(captchaImgUrl);
			extractAndSaveCaptcha(captchaImgUrl, s_url);
			String text = getCaptchaText();
			
			if (text == null) {
				System.out.println(" No captcha text");
				return null;			
			}
			revalidateWithCaptcha(s_url, text, captchaId);
			return null;
		}
		
		Element table = doc.getElementById("ContentPlaceHolder1_PanelDetails");
		if (table == null) {
			System.out.println("Table was null in Extract details");
			System.out.println(doc.body());
			return null;
		}
		Elements rows = table.select("tr");
		if (rows == null) {
			System.out.println(url +" No rows found");
		}
		
		StringBuffer sbuffer = new StringBuffer();

		boolean useDelimiter = false;
		for (Element row : rows) {
			Elements tds = row.select("td");			
			
			for (int ind = 0; ind < tds.size(); ++ind) {
				
				Element td = tds.get(ind);
				String toWrite = " ";
				
				if ("dataLabel".equals(td.attr("class")) && "Case Number:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Case Type:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Status:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Disposition:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Disposition Date:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Court Agency:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Case Sub Type:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Assigned Judge:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Filed Date:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Disposition Judge:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else if ("dataLabel".equals(td.attr("class")) && "Arrest Date:".equals(td.text()) )
					toWrite = tds.get(ind + 1).text().isEmpty() ? " " : tds.get(ind + 1).text();
				else 
					continue;
				
				if (useDelimiter) {
					sbuffer.append(";");					
				}
				sbuffer.append(toWrite);
				useDelimiter = true;
			 //System.out.println(td.text());
			}
			//sbuffer.append("\n");
		}
			
			
		table = doc.getElementById("ContentPlaceHolder1_LabelFullCase1");
		if (table != null) {
			rows = table.select("tr");
			if (rows == null) {
				System.out.println(url +" No rows found");
			}
			
			
			for (int r_ind = 1; r_ind <= 2; ++r_ind) {
				
				Elements tds = null;
				if (r_ind < rows.size()) {
					tds = rows.get(r_ind).select("td");
					//System.out.println(tds.text());
				}
				
				for (int ind = 0; ind < 8; ++ind) {
					String toWrite = " ";
					if (tds != null && tds.size() > ind) {
						Element td = tds.get(ind);
						toWrite = (td == null || td.text().isEmpty()) ? " ": td.text();
					}
					
					sbuffer.append(";");
					sbuffer.append(toWrite);
				}
			}
		}
		
		
		table = doc.getElementById("ContentPlaceHolder1_LabelFullCase2");
		if (table != null) {
			rows = table.select("tr");
			if (rows == null) {
				System.out.println(url +" No rows found");
			}
			for (Element row : rows) {
				Elements tds = row.select("td");			
				for (Element td : tds) {
					String toWrite = td.text().isEmpty() ? " ": td.text();
				//	System.out.println(toWrite);
					sbuffer.append(";");
					sbuffer.append(toWrite);
				}
			}
		}
		
		table = doc.getElementById("ContentPlaceHolder1_LabelFullCase3");
		if (table != null) {
			rows = table.select("tr");
			if (rows == null) {
				System.out.println(url +" No rows found");
			}
			for (Element row : rows) {
				Elements tds = row.select("td");			
				for (Element td : tds) {
					String toWrite = td.text().isEmpty() ? " ": td.text();
					//System.out.println(toWrite);
					sbuffer.append(";");
					sbuffer.append(toWrite);
				}
			}
		}
		
		return sbuffer.toString();

	}
	
	
	public static String extractList(InputStream is, String fileName) throws IOException {
		Document doc = Jsoup.parse(is, null, "");
		if (doc == null) {
			System.out.println(fileName + " - Document is null");
		}
		if (doc.body().toString().contains("Your search results exceeded the maximum number of records allowed and all records were not returned")) {
			System.out.println(fileName +" Exceeded num of records - 1");
		}
		
		Element table = doc.getElementById("ContentPlaceHolder1_SearchResults");
		Elements rows = table.select("tr");
		if (rows == null) {
			System.out.println(fileName +" No rows found");
		}
		Elements ths = rows.select("th");

		String thstr = "";
		for (Element th : ths) {
			thstr += th.text() + ";";
		}
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append(thstr+"\n");
		//System.out.println(thstr);

		for (Element row : rows) {
			Elements tds = row.select("td");			
			int i = 0;
			for (Element td : tds) {
				if (i != 0 ) {
					sbuffer.append(";");
				}
				String toWrite = td.text().isEmpty() ? " ": td.text();
				sbuffer.append(toWrite);
				i++;
			 //System.out.println(td.text());
			}
			sbuffer.append("\n");
		//	System.out.println(tds.text()); // -->This will print everything
											// in the row
		}
		System.out.println("Writing to file - " + fileName);
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.print(sbuffer.toString());;
		writer.close();
		
		
		return sbuffer.toString();

		
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("Insufficient args");			
		}
		
		String county = args[0];
		String [] alphabets = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
				"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
				"x", "y", "z"};
		List<String> caseTypes = new ArrayList<>();
		caseTypes.add("CR  ");
		caseTypes.add("GS  ");
		
		
		PrintWriter writer = new PrintWriter(new FileWriter(county + "-crawled_data.csv", true));
		
		Map<String, String> fields = new HashMap<>();
		fields.put("lastnamephrase", "");
		fields.put("casetype", "");
		
//		MedCrawler3_19.crawl("http://publicindex.sccourts.org/Florence/PublicIndex/PISearch.aspx", "POST", "jo");
		String s_url = HOST +"/" + county + "/PublicIndex";
		String lineToWrite = "";
		int linesAdded = 0;
		for (int i = 1; i < alphabets.length; ++i) {
			for (int j = 0; j < alphabets.length ; ++j) {
				if (alphabets[i].equals("b") && j < 9 ) continue;
				String phrase = alphabets[i]+alphabets[j];				
				fields.put("lastnamephrase", phrase);
				//if (phrase.equals("ha")) continue;
				
				for (String caseType : caseTypes) {					
					fields.put("casetype", caseType);
					
                    if (caseType.equals("CR  ") && phrase.equals("be")) continue;
					
					List<String> urls = null;
					InputStream is = MedCrawler3_19.getListByFields(s_url , "POST", fields);
					urls = MedCrawler3_19.extractNameLinks(is, phrase, s_url);
					
					while (urls == null) {
						System.out.println("Waiting for Session to be revalidated");
						is = MedCrawler3_19.getListByFields(s_url , "POST", fields);
						urls = MedCrawler3_19.extractNameLinks(is, phrase, s_url);	
					}					
					is.close();
					
					int k = 0;
					
					while (!urls.isEmpty()) {
						String u = urls.get(0);
						urls.remove(0);
						k++;
						//if (phrase.equals("de") && caseType.equals("CR  ") && k <= 200) continue;
						is = getCrawledData(u, "GET", s_url);
						if (is == null) {
							continue;
						}
						String line = extractDetails(is, u, s_url);
						is.close();
						if (line == null) {
							urls.add(u);
							k--;
							Thread.sleep(1000);
							continue;
						}
						
						System.out.println("Url - " + k);
						lineToWrite += line + "\n" ;
						linesAdded++;
						
						//System.out.println(line);
						if (linesAdded == 200) {
							writer.println(lineToWrite);
							writer.flush();
							System.out.println("Writing " + linesAdded + " to the file");
							linesAdded = 0;
							lineToWrite = "";
							
									
						}
						Thread.sleep(200);						
						
					}
					if (!lineToWrite.equals("")) {
						writer.println(lineToWrite);
						writer.flush();
						System.out.println("Writing " + linesAdded + " to the file after one case");
						linesAdded = 0;
						lineToWrite = "";
						
					}
					
					
				}
				Thread.sleep(5000);				
			}
		}
		writer.close();	
		
	}


}
