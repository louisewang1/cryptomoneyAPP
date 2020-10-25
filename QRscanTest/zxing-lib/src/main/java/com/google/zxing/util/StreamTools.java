package com.google.zxing.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamTools {
	/**
	 * ��������������ת�����ַ���
	 * @param is
	 * @return
	 * @throws IOException 
	 */
	public static String readInputStream(InputStream is){
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while((len=is.read(buffer))!=-1){
				stream.write(buffer, 0, len);
			}
			is.close();
			stream.close();
			byte[] result = stream.toByteArray();
			//���Ž��� result ������ַ���
			String temp = new String(result);
			return temp;
		} catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}

	}
}
