package com.oppo.cdo.instant.platform.user.core.util;

import java.util.Stack;

public class BinaryUtils {

	/** 64进制数组 */
	private static final char[] array = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v',
			'b', 'n', 'm', '8', '5', '2', '7', '3', '6', '4', '0', '9', '1', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H',
			'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M' };

	// 10进制转为其他进制，除留取余，逆序排列
	public static String _10_to_N(long rest, int N) {
		if (N == 10) {
			return String.valueOf(rest);
		}
		// 创建栈
		Stack<Character> stack = new Stack<Character>();
		StringBuilder result = new StringBuilder(0);
		while (rest >= 1) {
			// 进栈,
			// 也可以使用(rest - (rest / 62) * 62)作为求余算法
			stack.add(array[new Double(rest % N).intValue()]);
			rest = rest / N;
		}
		for (; !stack.isEmpty();) {
			// 出栈
			result.append(stack.pop());
		}
		return result.length() == 0 ? "0" : result.toString();

	}

	// 其他进制转为10进制，按权展开
	public static long N_to_10(String str, int N) {
		if (N == 10) {
			return Long.parseLong(str);
		}
		int multiple = 1;
		long result = 0;
		Character c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(str.length() - i - 1);
			result += decodeChar(c) * multiple;
			multiple = multiple * N;
		}
		return result;
	}

	private static int decodeChar(Character c) {
		for (int i = 0; i < array.length; i++) {
			if (c == array[i]) {
				return i;
			}
		}
		return -1;
	}
}
