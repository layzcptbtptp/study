package study;

import java.util.HashMap;
import java.util.Map;

import com.study.util.HttpsUtil;

public class StudyTest {
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();

		HttpsUtil.requestPostMethod("http://localhost:8080/save", map);
	}
}
