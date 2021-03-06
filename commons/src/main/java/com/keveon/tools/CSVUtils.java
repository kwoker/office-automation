package com.keveon.tools;

import lombok.experimental.UtilityClass;
import org.apache.commons.beanutils.BeanUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Keveon on 2017/2/18.
 * 文件操作  
 */
@UtilityClass
public class CSVUtils {
	@SuppressWarnings("rawtypes")
	public static File createCSVFile(List exportData, LinkedHashMap map, String outPutPath, String fileName) {
		File csvFile = null;
		BufferedWriter csvFileOutputStream = null;
		try {
			File file = new File(outPutPath);
			if (!file.exists()) {
				file.mkdir();
			}
			// 定义文件名格式并创建
			csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));
			System.out.println("csvFile：" + csvFile);
			// UTF-8使正确读取分隔符","
			csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"),
					1024);
			System.out.println("csvFileOutputStream：" + csvFileOutputStream);
			// 写入文件头部
			for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext(); ) {
				Map.Entry propertyEntry = (Map.Entry) propertyIterator.next();
				csvFileOutputStream.write(
						"" + propertyEntry.getValue() != null ? (String) propertyEntry.getValue() : "" + "");
				if (propertyIterator.hasNext()) {
					csvFileOutputStream.write(",");
				}
			}
			csvFileOutputStream.newLine();
			// 写入文件内容
			for (Iterator iterator = exportData.iterator(); iterator.hasNext(); ) {
				Object row = iterator.next();
				for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext(); ) {
					Map.Entry propertyEntry = (Map.Entry) propertyIterator.next();
					csvFileOutputStream.write(BeanUtils.getProperty(row, (String) propertyEntry.getKey()));
					if (propertyIterator.hasNext()) {
						csvFileOutputStream.write(",");
					}
				}
				if (iterator.hasNext()) {
					csvFileOutputStream.newLine();
				}
			}
			csvFileOutputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				csvFileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return csvFile;
	}

	public static void exportFile(HttpServletResponse response, String csvFilePath, String fileName)
			throws IOException {
		response.setContentType("application/csv;charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
		InputStream in = null;
		try {
			in = new FileInputStream(csvFilePath);
			int len = 0;
			byte[] buffer = new byte[1024];
			response.setCharacterEncoding("UTF-8");
			OutputStream out = response.getOutputStream();
			while ((len = in.read(buffer)) > 0) {
				out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
				out.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
		}
	}

	/**
	 * 删除该目录filePath下的所有文件
	 *
	 * @param filePath 文件目录路径    
	 */
	public static void deleteFiles(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				}
			}
		}
	}

	/**
	 * 删除单个文件    
	 *
	 * @param filePath 文件目录路径
	 * @param fileName 文件名称    
	 */
	public static void deleteFile(String filePath, String fileName) {
		File file = new File(filePath);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					if (files[i].getName().equals(fileName)) {
						files[i].delete();
						return;
					}
				}
			}
		}
	}

	/**
	 * 文件导出CVS 將文件
	 *
	 * @param exportData .
	 * @param map        .
	 * @param response   .
	 */
	public static void createCVSAndOutput(List<Map<String, Object>> exportData, LinkedHashMap<String, String> map, HttpServletResponse response) {
		String path = "C:" + File.separator + "export" + File.separator;
		String fileName = "文件导出";
		File file = CSVUtils.createCSVFile(exportData, map, path, fileName);
		String fileName2 = file.getName();
		try {
			OutputStream os = response.getOutputStream();
			os.write((fileName2).getBytes());
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}