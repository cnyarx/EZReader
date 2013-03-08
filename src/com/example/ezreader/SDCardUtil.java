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
		// 得到当前外部存储设备的目录
		// /SDCARD
		SDPATH = Environment.getExternalStorageDirectory() + "/";
	}

	public static String getSDPATH() {

		return SDPATH;
	}

	/**
	 * 检查SD卡是否插入
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
	 * 创建文件到SDCard中
	 * 
	 * @param path
	 * @param fileName
	 * @return true:创建成功 false:创建失败（文件已经存在）
	 * 
	 */
	public static File createFile2SDCard(String path, String fileName) {

		// ///////////////////////////////////////
		// 创建SD卡目录
		// ///////////////////////////////////////
		File dir = new File(path);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		// //////////////////////////////////////////
		// 创建SD卡文件
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
	 * 判断文件是否存在在SDCard卡上
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
	 * 查找某目录下所有fileType类型的文件
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
