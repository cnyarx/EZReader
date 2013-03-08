package com.example.ezreader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Environment;

public class SDCardUtil {

	private static String SDPATH;

	static {
		// �õ���ǰ�ⲿ�洢�豸��Ŀ¼
		// /SDCARD
		SDPATH = Environment.getExternalStorageDirectory() + "/";
	}

	public static String getSDPATH() {

		return SDPATH;
	}

	/**
	 * ���SD���Ƿ����
	 * 
	 * @return
	 *
	 */
	public static boolean checkSDCARD() {

		String status = Environment.getExternalStorageState();
		
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}

		return false;
	}

	/**
	 * �����ļ���SDCard��
	 * 
	 * @param path
	 * @param fileName
	 * @return true:�����ɹ� false:����ʧ�ܣ��ļ��Ѿ����ڣ�
	 * 
	 */
	public static File createFile2SDCard(String path, String fileName) {

		// ///////////////////////////////////////
		// ����SD��Ŀ¼
		// ///////////////////////////////////////
		File dir = new File(path);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		// //////////////////////////////////////////
		// ����SD���ļ�
		// ///////////////////////////////////////////
		File file = new File(path + fileName);

		if (file.exists()) {

			file.delete();
		}
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
	}

	/**
	 * �ж��ļ��Ƿ������SDCard����
	 * 
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static boolean checkFileExist(String path, String fileName) {

		File file = new File(path + fileName);

		return file.exists();
	}
	
	/**
	 * ����ĳĿ¼������fileType���͵��ļ�
	 * 
	 * @param path
	 * @param fileType
	 * @return
	 * 
	 */
	public static File[] findSDCardFile(String path, final String fileType) {

		File dir = new File(path);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {

					return (filename.endsWith(fileType));
				}
			});

			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File str1, File str2) {

					return str2.getName().compareTo(str1.getName());
				}
			});

			return files;
		}

		return null;
	}
}
