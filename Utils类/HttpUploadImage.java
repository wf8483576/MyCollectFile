package com.lhdz.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HttpUploadImage {

	/**
	 * 文件上传
	 * 
	 * @param urlStr
	 *            图片上传路径
	 * @param filePath
	 *            本地图片路径
	 * @return
	 */
	public static String uploadImage(String urlStr, String filePath) {
		String rsp = "";// 返回结果
		HttpURLConnection conn = null;
		String BOUNDARY = "|"; // request头和上传文件内容分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			// conn.setRequestProperty("User-Agent",
			// "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());
			File file = new File(filePath);
			// 拿到文件名 IMG_20151210_105649.jpg;
			String filename = file.getName();
			System.out.println(filename + "文件名");
			String contentType = "";
			if (filename.endsWith(".png")) {
				contentType = "image/png";
			} else if (filename.endsWith(".jpg")) {
				contentType = "image/jpg";
			} else if (filename.endsWith(".gif")) {
				contentType = "image/gif";
			} else if (filename.endsWith(".bmp")) {
				contentType = "image/bmp";
			} else if (contentType == null || contentType.equals("")) {
				contentType = "application/octet-stream";
			}
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"uploadedfile"
					+ filePath + "\"; filename=\"" + filename + "\"\r\n");
			strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
			out.write(strBuf.toString().getBytes());// 写入文件名
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);// 写入图片流
			}
			in.close();
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取响应数据
			int code = conn.getResponseCode();
			String sCurrentLine = "";
			// 存放响应结果
			String sTotalString = "";
			if (code == 200) {
				InputStream is = conn.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				while ((sCurrentLine = reader.readLine()) != null)
					if (sCurrentLine.length() > 0)
						sTotalString = sTotalString + sCurrentLine.trim();
			} else {
				sTotalString = "远程服务器连接失败,错误代码:" + code;
				System.out.println(sTotalString);
			}
			return sTotalString;

			// // 读取返回数据
			// StringBuffer buffer = new StringBuffer();
			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// conn.getInputStream(), "utf-8"));
			// String line = null;
			// while ((line = reader.readLine()) != null) {
			// buffer.append(line).append("\n");
			// }
			// rsp = buffer.toString();
			// reader.close();
			// reader = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return rsp;
	}

	public static String uploadFile(String uploadUrl, String srcPath) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		String sTotalString = "";
		HttpURLConnection httpURLConnection = null;
		try {
			URL url = new URL(uploadUrl);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			// httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
					+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
					+ "\""
					+ end);
			dos.writeBytes(end);// 写文件名

			FileInputStream fis = new FileInputStream(srcPath);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			// 读取文件
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);// 写
			}
			fis.close();

			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();
			String sCurrentLine = "";

			int code = httpURLConnection.getResponseCode();
			if (code == 200) {
				InputStream is = httpURLConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "utf-8");
				BufferedReader reader = new BufferedReader(isr);
				while ((sCurrentLine = reader.readLine()) != null)
					if (sCurrentLine.length() > 0)
						sTotalString = sTotalString + sCurrentLine.trim();
			} else {
				sTotalString = "远程服务器连接失败,错误代码:" + code;
				System.out.println(sTotalString);
			}

			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
				httpURLConnection = null;
			}
		}
		return sTotalString;
	}

	public static void saveToDisk(String urlPath) {
		// 获取输入流
		InputStream inputStream = getInputStream(urlPath);

		byte[] date = new byte[1024];
		int len = 0;
		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(urlPath);

			while ((len = inputStream.read(date)) != -1) {
				fileOutputStream.write(date, 0, len);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (inputStream != null) {
					inputStream.close();

				}

				if (fileOutputStream != null) {
					fileOutputStream.close();

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * @return
	 */
	public static InputStream getInputStream(String urlPath) {
		InputStream inputStream = null;
		HttpURLConnection httpURLConnection = null;

		try {
			URL url = new URL(urlPath);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			// 设置连接网络的超时时间
			httpURLConnection.setConnectTimeout(3000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setRequestMethod("post");

			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode == 200) {
				inputStream = httpURLConnection.getInputStream();

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return inputStream;
	}

	public static ByteArrayInputStream compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		Log.e("byte", baos.toByteArray().length + "");
		if (baos.toByteArray().length > 3000) {
			while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				options -= 20;// 每次都减少10
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				Log.e("baosss", baos.toByteArray().length + "比例" + options);
				Log.e("options", options + "");
				if (options <= 0) {
					break;
				}
			}
		} else {
			while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				options -= 10;// 每次都减少10
			}
		}
		Log.e("fdsdfas", baos.toByteArray().length + "");
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return isBm;
	}

}
