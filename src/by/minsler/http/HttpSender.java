package by.minsler.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import by.minsler.boundary.BoundaryGenerator;

public class HttpSender {

	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Please use two parameters(delimiter by space):"
					+ "\n\t1 - url for destination"
					+ "\n\t2 - path to soap file"
					+ "\n\t3 - path to file for attachment");
			return;
		}
		File fileSoap = new File(args[1]);
		File fileAttachment = new File(args[2]);
		String url = args[0];

		URL destinationUrl;
		HttpURLConnection connection = null;
		try {
			destinationUrl = new URL(url);

			connection = (HttpURLConnection) destinationUrl.openConnection();
			connection.setRequestMethod("POST");

		} catch (MalformedURLException e) {
			System.out.println("Incorrect URL address");
			return;
		} catch (ProtocolException e) {
			System.out.println("Invalid method for HTTP request");
			return;
		} catch (IOException e) {
			System.out.println("Open connection: IO exception");
			return;
		}
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		String boundary = BoundaryGenerator.generateBoundary();
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("Cache-Control", "no-cache");
		StringBuilder sb = new StringBuilder();
		String soapHeaders = "--" + boundary + "\n"
				+ "Content-Type: text/xml; charset=UTF-8\n"
				+ "Content-Id: <soappart>\n\n";
		String attachmentHeaders = "\n--" + boundary + "\n"
				+ "Content-Type: text/plain; charset=\"utf-8\"\n"
				+ "Content-ID: Payload-0\n\n";
		String multipartEnd = "--" + boundary + "--\n";

		sb.append(soapHeaders).append(attachmentHeaders).append(multipartEnd);

		long lentghOfStream = fileSoap.length() + fileAttachment.length()
				+ new String(sb).getBytes().length;
		// Set contentLength
		connection.setFixedLengthStreamingMode(lentghOfStream);

		System.out.println("length of files with multipart info: "
				+ lentghOfStream);
		DataOutputStream dos = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		BufferedReader in = null;
		try {
			dos = new DataOutputStream(connection.getOutputStream());
			fos = new FileOutputStream(new File("request.txt"));

			dos.writeBytes(soapHeaders);
			fos.write(soapHeaders.getBytes());

			fis = new FileInputStream(fileSoap);

			// set buffer of byte for read and write stream
			byte array[] = new byte[8 * 1024];
			int nread;

			while ((nread = fis.read(array)) >= 0) {
				dos.write(array, 0, nread);
				fos.write(array, 0, nread);
			}

			dos.writeBytes(attachmentHeaders);
			fos.write(attachmentHeaders.getBytes());

			fis.close();
			fis = new FileInputStream(fileAttachment);

			while ((nread = fis.read(array)) >= 0) {
				dos.write(array, 0, nread);
				fos.write(array, 0, nread);
			}

			dos.writeBytes(multipartEnd);
			fos.write(multipartEnd.getBytes());

			dos.flush();
			fos.flush();

			System.out.println("response code " + connection.getResponseCode());

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
			}

		} catch (IOException e) {
			System.out.println("IO exception occurred: " + e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					System.out.println("Error of close stream: " + e);
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.out.println("Error of close stream: " + e);
				}
			}
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					System.out.println("Error of close stream: " + e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.out.println("Error of close stream: " + e);
				}
			}
		}

	}

}