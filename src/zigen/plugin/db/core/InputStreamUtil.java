/*
 * 著作権: Copyright (c) 2007−2008 ZIGEN
 * ライセンス：Eclipse Public License - v 1.0 
 * 原文：http://www.eclipse.org/legal/epl-v10.html
 */

package zigen.plugin.db.core;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

public class InputStreamUtil {

	/**
	 * InputStream→byte[]変換
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		byte[] out = null;
		ByteArrayOutputStream sw = null;
		byte[] buf = new byte[1024];
		int i;
		try {
			sw = new ByteArrayOutputStream();
			while ((i = inputStream.read(buf, 0, buf.length)) != -1) {
				sw.write(buf, 0, i);
			}
			sw.flush();
			sw.close();

			out = sw.toByteArray();
		} catch (IOException e) {
			throw e;
		}
		return out;

	}

	/**
	 * <BLOB型用>inputStreamからFileに出力する
	 * 
	 * @param file
	 * @param inputStream
	 * @throws IOException
	 */
	public static void save(File file, InputStream inputStream) throws IOException {
		DataInputStream dis = null;
		FileOutputStream fos = null;
		byte[] buf = new byte[1024];
		int i;
		try {
			dis = new DataInputStream(inputStream);
			fos = new FileOutputStream(file);
			while ((i = dis.read(buf, 0, buf.length)) != -1) {
				fos.write(buf, 0, i);
			}
			fos.flush();

		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
			if (dis != null)
				dis.close();
		}

	}

	/**
	 * <BLOB型用>inputStreamからStringに出力する(コード変換：JISAutoDetect)
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	public static String toString(InputStream inputStream, String charsetName) throws IOException {
		String out = null;
		InputStreamReader br = null;
		StringWriter sw = null;
		char[] buf = new char[1024];
		int i;
		try {
			br = new InputStreamReader(inputStream, charsetName);
			sw = new StringWriter();
			while ((i = br.read(buf, 0, buf.length)) != -1) {
				sw.write(buf, 0, i);
			}
			sw.flush();
			sw.close();

			out = sw.toString();
		} catch (IOException e) {
			throw e;
		} finally {
			if (br != null)
				br.close();

		}
		return out;

	}

	/**
	 * <BLOB型用>inputStreamからStringに出力する(コード変換：JISAutoDetect)
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	// public static String toString(InputStream inputStream) throws IOException {
	// return toString(inputStream, "JISAutoDetect"); //$NON-NLS-1$
	// }
	/**
	 * <CLOB型用>inputStreamからFileに出力する
	 * 
	 * @param file
	 * @param inputStream
	 * @throws IOException
	 */
	public static void save(File file, Reader reader) throws IOException {
		FileWriter fw = null;
		char[] buf = new char[1024];
		int i;
		try {
			fw = new FileWriter(file);
			while ((i = reader.read(buf, 0, buf.length)) != -1) {
				fw.write(buf, 0, i);
			}

			fw.flush();
		} catch (IOException e) {
			throw e;
		} finally {

			if (fw != null)
				fw.close();

			if (reader != null)
				reader.close();
		}

	}

	/**
	 * <CLOB型用>ReaderからStringに出力する
	 * 
	 * @param reader
	 * @throws IOException
	 */
	public static String toString(Reader reader) throws IOException {
		String out = null;
		StringWriter sw = null;
		char[] buf = new char[1024];
		int i;
		try {
			sw = new StringWriter();
			while ((i = reader.read(buf, 0, buf.length)) != -1) {
				sw.write(buf, 0, i);
			}
			sw.flush();
			sw.close();
			out = sw.toString();
		} catch (IOException e) {
			throw e;
		} finally {
			reader.close();

		}
		return out;

	}

	/**
	 * <CLOB型用>Reader→char[]変換
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static char[] toCharArray(Reader reader) throws IOException {
		char[] out = null;
		CharArrayWriter cw = null;
		char[] buf = new char[1024];
		int i;
		try {
			cw = new CharArrayWriter();
			while ((i = reader.read(buf, 0, buf.length)) != -1) {
				cw.write(buf, 0, i);
			}
			cw.flush();
			cw.close();

			out = cw.toCharArray();
		} catch (IOException e) {
			throw e;
		}
		return out;

	}

}
