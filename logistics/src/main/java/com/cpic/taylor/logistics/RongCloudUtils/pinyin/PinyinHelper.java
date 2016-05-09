package com.cpic.taylor.logistics.RongCloudUtils.pinyin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongContext;


/**
 * 描述： 汉字转换成拼音工具类（pinyin4j-2.5.0-lite.jar）<br>
 * 支持全部20902个汉字，只对传入的字符串进行循环，对应的拼音是通过Ascii码表索引位置相减得出，效率比较高；
 * 
 * @author zhjchen
 * @version 1.0
 * @since JDK1.5
 */
public class PinyinHelper {
	/** Ascii码表中第一个汉字：‘一’ */
	public static final char First_HANZI = '\u4E00';
	/** Ascii码表中第后一个汉字：‘龥’ */
	public static final char LAST_HANZI = '\u9FA5';

	private static PinyinHelper mInstance;
	private static String[] PINYINS;
	private static final String NONE = "none";
	private static final String FILE_PATH = "unicode_to_hanyu_pinyin.dat";

	public static PinyinHelper getInstance() {
		synchronized (PinyinHelper.class) {
			if (mInstance == null) {
				mInstance = new PinyinHelper();
			}
		}
		return mInstance;
	}

	public PinyinHelper() {

        BufferedReader br = null;
        try {
            InputStream inputStream = RongContext.getInstance().getResources().getAssets().open(FILE_PATH);
            br = new BufferedReader( new InputStreamReader(inputStream));
        }catch (IOException e){
            throw new RuntimeException("no find pinyin assets file");
        }

		try {
			PINYINS = new String[20902];
			String line;
			for (int i = 0; (line = br.readLine()) != null && i < PINYINS.length; i++) {
				PINYINS[i] = line;
			}
			line = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取字符串的拼音，以字符串List的形式返回，返回的拼音字母都是小写字母；
	 * 
	 * @param input
	 * @return
	 */
	public List<String> getPinyins(String input) {
		List<String> pinyins = null;
		if (PINYINS != null && input != null) {
			char[] chars = input.toCharArray();
			pinyins = new ArrayList<String>();
			String pinyin = null;
			for (int i = 0, len = chars.length; i < len; i++) {
				if (isHanzi(chars[i])) {
					pinyin = PINYINS[chars[i] - First_HANZI];
					pinyins.add(NONE.equals(pinyin) ? String.valueOf(chars[i]) : pinyin);
				} else {
					if (i < len - 1 && !isHanzi(chars[i + 1])) {
						pinyin = new String(new char[] { chars[i], chars[i + 1] });
						i++;
					} else {
						pinyin = String.valueOf(chars[i]);
					}
					pinyins.add(pinyin);
				}
				pinyin = null;
			}
		}
		return pinyins;
	}

	/**
	 * 获取字符串的拼音，返回的拼音字母都是小写字母；
	 * 
	 * @param input
	 * @param separator
	 *            转换后每个拼音间的分割字符
	 * @return
	 */
	public String getPinyins(String input, String separator) {
		String result = null;
		if (PINYINS != null && input != null) {
			char[] chars = input.toCharArray();
			StringBuffer pinyins = new StringBuffer(chars.length);
			String pinyin = null;
			for (int i = 0, len = chars.length; i < len; i++) {
				if (isHanzi(chars[i])) {
					pinyin = PINYINS[chars[i] - First_HANZI];
					pinyins.append(NONE.equals(pinyin) ? String.valueOf(chars[i]) : pinyin);
				} else {
					pinyins.append(String.valueOf(chars[i]));
				}
				if (i < len - 1 && (isHanzi(chars[i]) || isHanzi(chars[i + 1]))) {
					pinyins.append(separator);
				}
				pinyin = null;
			}
			result = pinyins.toString();
			pinyins.setLength(0);
		}
		return result;
	}

	/**
	 * 获取汉字的所有拼音首字母，返回的拼音字母都是小写字母；
	 * 
	 * @param input
	 * @return
	 */
	public String getFirstPinyins(String input) {
		String result = null;
		if (PINYINS != null && input != null) {
			char[] chars = input.toCharArray();
			StringBuffer pinyins = new StringBuffer(chars.length);
			String pinyin = null;
			for (int i = 0, len = chars.length; i < len; i++) {
				if (isHanzi(chars[i])) {
					pinyin = PINYINS[chars[i] - First_HANZI];
					pinyins.append(NONE.equals(pinyin) ? String.valueOf(chars[i]) : pinyin.charAt(0));
				} else {
					pinyins.append(String.valueOf(chars[i]));
				}
				pinyin = null;
			}
			result = pinyins.toString();
			pinyins.setLength(0);
		}
		return result;
	}

	/**
	 * 是否是汉字字符，即正则表达式匹配：\\u4E00-\\u9fA5
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isHanzi(char c) {
		return c >= First_HANZI && c <= LAST_HANZI;
	}

	/**
	 * 是否是汉字字符串
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isHanzi(String input) {
		if (input != null && input.length() > 0) {
			for (int i = input.length() - 1; i >= 0; i--) {
				if (!isHanzi(input.charAt(i))) {
					return false;
				} else if (i == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否包含汉字字符
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isContainsHanzi(String input) {
		if (input != null && input.length() > 0) {
			for (int i = input.length() - 1; i >= 0; i--) {
				if (isHanzi(input.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}
}