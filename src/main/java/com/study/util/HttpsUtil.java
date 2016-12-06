package com.study.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http工具类，包含GET，POST,PUT,DELETE等操作 Created by lancey on 2016/6/1.
 */
public class HttpsUtil {

	private static final Logger LOG = LoggerFactory.getLogger(HttpsUtil.class);

	public static final String CHARACTER_ENCODING = "UTF-8";
	public static final String CONTENT_TYPE = "Content-Type";

	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int SO_TIMEOUT = 30000;

	/**
	 * HttpResponse包装类 需要使用方自己控制httpClient的关闭
	 * 
	 * @author lancey
	 */
	public static class HttpResponseWrapper implements Closeable {
		private HttpResponse httpResponse;
		private HttpClient httpClient;

		public HttpResponseWrapper(HttpClient httpClient,
				HttpResponse httpResponse) {
			this.httpClient = httpClient;
			this.httpResponse = httpResponse;
		}

		public HttpResponseWrapper(HttpClient httpClient) {
			this.httpClient = httpClient;
		}

		public HttpResponse getHttpResponse() {
			return httpResponse;
		}

		public void setHttpResponse(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		/**
		 * 获得流类型的响应
		 */
		public InputStream getResponseStream() throws IllegalStateException,
				IOException {
			return httpResponse.getEntity().getContent();
		}

		/**
		 * 获得字符串类型的响应
		 */
		public String getResponseString(String responseCharacter)
				throws ParseException, IOException {
			HttpEntity entity = getEntity();
			String responseStr = EntityUtils
					.toString(entity, responseCharacter);
			if (entity.getContentType() == null) {
				responseStr = new String(responseStr.getBytes("iso-8859-1"),
						responseCharacter);
			}
			EntityUtils.consume(entity);
			return responseStr;
		}

		public String getResponseString() throws ParseException, IOException {
			return getResponseString(CHARACTER_ENCODING);
		}

		/**
		 * 获得响应状态码
		 */
		public int getStatusCode() {
			return httpResponse.getStatusLine().getStatusCode();
		}

		/**
		 * 获得响应状态码并释放资源
		 */
		public int getStatusCodeAndClose() {
			close();
			return getStatusCode();
		}

		public HttpEntity getEntity() {
			return httpResponse.getEntity();
		}

		/**
		 * 释放资源
		 */
		@Override
		public void close() {
			if (httpClient instanceof CloseableHttpClient) {
				try {
					((CloseableHttpClient) httpClient).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * http get请求，如果存在参数直接拼装在url后面
	 * 
	 * @param url
	 * @param requestParas
	 * @return
	 */
	public static HttpResponseWrapper requestGetMethod(final String url,
			Map<String, String> requestParas) {
		CloseableHttpClient httpClient = getCloseableHttpClient(url);
		try {
			StringBuffer urlStringBuffer = new StringBuffer(url);
			if (MapUtils.isNotEmpty(requestParas)) {
				StringBuffer sb = new StringBuffer();
				for (String key : requestParas.keySet()) {
					sb.append(key);
					sb.append("=");
					sb.append(requestParas.get(key));
					sb.append("&");
				}
				sb.setLength(sb.length() - 1);
				if (url.contains("?")) {
					urlStringBuffer.append("&");
				} else {
					urlStringBuffer.append("?");
				}
				urlStringBuffer.append(sb.toString());
			}
			HttpGet httpGet = new HttpGet(urlStringBuffer.toString());
			HttpResponse response = httpClient.execute(httpGet);
			return new HttpResponseWrapper(httpClient, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static CloseableHttpClient getCloseableHttpClient(String url) {
		CloseableHttpClient httpClient = null;
		if (url.startsWith("https://")) {
			httpClient = createHttpsClient();
		} else {
			httpClient = createHttpClient();
		}
		return httpClient;
	}

	public static HttpResponseWrapper requestPostMethod(final String url,
			Map<String, String> requestParas) {
		return requestPostMethod(url, null, requestParas, CHARACTER_ENCODING,
				null);
	}

	/**
	 * 请求一个post请求
	 * 
	 * @param url
	 * @param requestHeaders
	 *            http头部
	 * @param requestParas
	 *            参数列表
	 * @param requestCharacter
	 *            编码
	 * @param xForWardedFor
	 * @return
	 */
	public static HttpResponseWrapper requestPostMethod(final String url,
			Map<String, String> requestHeaders,
			Map<String, String> requestParas, String requestCharacter,
			String xForWardedFor) {
		CloseableHttpClient httpClient = getCloseableHttpClient(url);
		HttpPost httpPost = new HttpPost(url);
		try {
			if (StringUtils.isNotEmpty(xForWardedFor)) {
				httpPost.addHeader("X-Forwarded-For", xForWardedFor);
			}
			initHeader(httpPost, requestHeaders);
			if (MapUtils.isNotEmpty(requestParas)) {
				List<NameValuePair> formParams = initNameValuePair(requestParas);
				httpPost.setEntity(new UrlEncodedFormEntity(formParams,
						requestCharacter));
			}
			HttpResponse httpResponse = httpClient.execute(httpPost); // 执行POST请求
			return new HttpResponseWrapper(httpClient, httpResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void initHeader(HttpMessage httpPost,
			Map<String, String> requestHeaders) {
		if (MapUtils.isNotEmpty(requestHeaders)) {
			for (String key : requestHeaders.keySet()) {
				httpPost.addHeader(key, requestHeaders.get(key));
			}
		}
	}

	public static HttpResponseWrapper requestPostMethod(final String url,
			Map<String, String> requestParas, String requestCharacter,
			String xForWardedFor) {
		return requestPostMethod(url, null, requestParas, requestCharacter,
				null);
	}

	/**
	 * 发起一个put的请求
	 * 
	 * @param url
	 * @param requestParas
	 * @param requestCharacter
	 * @return
	 */
	public static HttpResponseWrapper requestPutMethod(final String url,
			Map<String, String> requestParas, String requestCharacter) {
		CloseableHttpClient httpClient = getCloseableHttpClient(url);
		HttpPut httpPut = new HttpPut(url);
		try {
			if (MapUtils.isNotEmpty(requestParas)) {
				List<NameValuePair> formParams = initNameValuePair(requestParas);
				httpPut.setEntity(new UrlEncodedFormEntity(formParams,
						requestCharacter));
			}
			HttpResponse httpResponse = httpClient.execute(httpPut); // 执行PUT请求
			return new HttpResponseWrapper(httpClient, httpResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发起一个delete的请求
	 * 
	 * @param url
	 * @return
	 */
	public static HttpResponseWrapper requestDeleteMethod(final String url) {
		CloseableHttpClient httpClient = getCloseableHttpClient(url);
		HttpDelete httpDelete = new HttpDelete(url);
		try {

			HttpResponse httpResponse = httpClient.execute(httpDelete);
			return new HttpResponseWrapper(httpClient, httpResponse);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static List<NameValuePair> initNameValuePair(
			Map<String, String> params) {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		if (params != null && params.size() > 0) {
			// 对key进行排序
			List<String> keys = new ArrayList<String>(params.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				formParams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		return formParams;
	}

	private void close(CloseableHttpClient httpClient) {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static CloseableHttpClient createHttpsClient() {
		return createHttpsClient(CONNECTION_TIMEOUT, SO_TIMEOUT);
	}

	public static CloseableHttpClient createHttpsClient(int connectionTimeout,
			int soTimeout) {
		SSLContext sslcontext = SSLContexts.createDefault();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectTimeout(connectionTimeout)
				.setSocketTimeout(soTimeout).build();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setDefaultRequestConfig(requestConfig).build();
		return httpclient;
	}

	public static CloseableHttpClient createHttpClient() {
		return createHttpsClient(CONNECTION_TIMEOUT, SO_TIMEOUT);
	}

	public static CloseableHttpClient createHttpClient(int connectionTimeout,
			int soTimeout) {
		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectTimeout(connectionTimeout)
				.setSocketTimeout(soTimeout).build();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig).build();
		return httpclient;
	}

	public static void main(String[] args) throws IOException {
		String url = "https://www.baidu.com/";
		Map<String, String> maps = new HashMap<>();
		maps.put("name", "11111");
		HttpResponseWrapper wrapper = requestGetMethod(url, null);
		System.out.println(wrapper.getResponseString());

		wrapper.close();
	}
}
