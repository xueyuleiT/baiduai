package com.baidu.aip.asrwakeup3.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*Task: 英文和阿拉伯数字之间的转换
 * */
public class Number2EnglishUtil {

    public static final String ZERO = "zero";

    public static SplitNum splitNum = new SplitNum();

        public static final String NEGATIVE = "negative";
        public static final String SPACE = " ";
        public static final String MILLION = "million";
        public static final String THOUSAND = "thousand";
        public static final String HUNDRED = "hundred";
        public static final String[] INDNUM = {"zero", "one", "two", "three", "four", "five", "six",
                "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen",
                "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
        public static final String[] DECNUM = {"twenty", "thirty", "forty", "fifty", "sixty",
                "seventy", "eighty", "ninety"};

        //数字转换英文
        public static String format(int i) {

            StringBuilder sb = new StringBuilder();

            if(i == 0) {
                return ZERO;
            }

            if(i < 0) {
                sb.append(NEGATIVE).append(SPACE);
                i *= -1;
            }


            if(i >= 1000000) {
                sb.append(numFormat(i / 1000000)).append(SPACE).append(MILLION).append(SPACE);
                i %= 1000000;

            }

            if(i >= 1000) {
                sb.append(numFormat(i / 1000)).append(SPACE).append(THOUSAND).append(SPACE);

                i %= 1000;
            }

            if(i < 1000){
                sb.append(numFormat(i));
            }

            return sb.toString();
        }

        // 3位数转英文
        public static String numFormat(int i) {

            StringBuilder sb = new StringBuilder();

            if(i >= 100) {
                sb.append(INDNUM[i / 100]).append(SPACE).append(HUNDRED).append(SPACE);
            }

            i %= 100;

            if(i != 0) {
                if(i >= 20) {
                    sb.append(DECNUM[i / 10 -2]).append(SPACE);
                    if(i % 10 != 0) {
                        sb.append(INDNUM[i % 10]);
                    }
                }else {
                    sb.append(INDNUM[i]);
                }
            }

            return sb.toString();
        }
        //英文转数字
        public int parse(String str) {
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            hm.put("zero", 0);
            hm.put("one", 1);
            hm.put("two", 2);
            hm.put("three", 3);
            hm.put("four", 4);
            hm.put("five", 5);
            hm.put("six", 6);
            hm.put("seven", 7);
            hm.put("eight", 8);
            hm.put("nine", 9);
            hm.put("ten", 10);
            hm.put("eleven", 11);
            hm.put("twelve", 12);
            hm.put("thirteen", 13);
            hm.put("fourteen", 14);
            hm.put("fifteen", 15);
            hm.put("sixteen", 16);
            hm.put("seventeen", 17);
            hm.put("eighteen", 18);
            hm.put("nineteen", 19);
            hm.put("twenty", 20);
            hm.put("thirty", 30);
            hm.put("forty", 40);
            hm.put("fifty", 50);
            hm.put("sixty", 60);
            hm.put("seventy", 70);
            hm.put("eighty", 80);
            hm.put("ninety", 90);
            hm.put("hundred", 100);
            hm.put("thousand", 1000);
            hm.put("million", 1000000);
            int i = 0;
            int b = 0;
            int c = 0;
            String[] k = str.split(" ");
            for (String string : k) {
                if("hundred".equals(string)){
                    i *= hm.get("hundred");
                }else if("thousand".equals(string)){
                    b = i;
                    b *= hm.get("thousand");
                    i = 0;
                }else if("million".equals(string)){
                    c = i;
                    c *= hm.get("million");
                    i = 0;
                }else if("negative".equals(string)){
                    i = 0;
                }else {
                    i += hm.get(string);
                }
            }
            i += c + b;
            for (String string2 : k) {
                if("negative".equals(string2)){
                    i = -i;
                }
            }
            return i;
        }


        public static String num2English(String numStr) {
            numStr = splitNum.subZeroAndDot(numStr);
            if (splitNum.isInteger(numStr)) {
                // 整数
                int integer = Integer.parseInt(numStr);

                return format(integer);
            } else {
                Double num = Double.valueOf(numStr);
                String integerNum = splitNum.splitInteger(num);

                int integer = Integer.parseInt(integerNum);

                numStr = format(integer);

                String decimalNum = splitNum.splitNum(num);

                String decimal = splitNum.decimalToEnlish(decimalNum);

                return numStr + " point " + decimal;
            }
        }


    public static class SplitNum {

        /**
         * 小数部分转换成英文
         *
         */
        public String decimalToEnlish(String decimalNum) {

            String[] enlishNum = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

            String decimal = "";
            int num = Integer.parseInt(decimalNum);
            int numLength = decimalNum.length();
            int[] numArr = new int[numLength];
            for (int i = 0; i < numLength; i++) {

                numArr[i] = (int) (num / (Math.pow(10, numLength - 1 - i)));

                num = (int) (num % (Math.pow(10, numLength - 1 - i)));

                if (numArr[i] == 0) {
                    decimal += enlishNum[0] + " ";
                } else if (numArr[i] == 1) {
                    decimal += enlishNum[1] + " ";
                } else if (numArr[i] == 2) {
                    decimal += enlishNum[2] + " ";
                } else if (numArr[i] == 3) {
                    decimal += enlishNum[3] + " ";
                } else if (numArr[i] == 4) {
                    decimal += enlishNum[4] + " ";
                } else if (numArr[i] == 5) {
                    decimal += enlishNum[5] + " ";
                } else if (numArr[i] == 6) {
                    decimal += enlishNum[6] + " ";
                } else if (numArr[i] == 7) {
                    decimal += enlishNum[7] + " ";
                } else if (numArr[i] == 8) {
                    decimal += enlishNum[8] + " ";
                } else if (numArr[i] == 9) {
                    decimal += enlishNum[9] + " ";
                }
            }

            return decimal;
        }

        /**
         * 取出整数部分
         */
        public String splitInteger(double num) {
            String str = String.valueOf(num);
            String result = str.substring(0, str.indexOf('.'));
            return result;
        }

        /**
         * 取出小数部分
         */
        public  String splitNum(double num) {
            String str = String.valueOf(num);
            String result = str.substring(str.indexOf('.') + 1);
            return result;
        }

        /**
         * 功能：检查请求isInteger方法的参数是否为整数
         *
         * @param str
         *            String
         * @return 返回boolean类型，false表示不是整数，true表示是整数
         */
        public  boolean isInteger(String str) {
            int begin = 0;
            if (str == null || str.trim().equals("")) {
                return false;
            }
            str = str.trim();
            if (str.startsWith("+") || str.startsWith("-")) {
                if (str.length() == 1) {
                    return false;
                }
                begin = 1;
            }
            for (int i = begin; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 使用java正则表达式去掉多余的.与0
         *
         * @param s
         * @return
         */
        public  String subZeroAndDot(String s) {
            if (s.indexOf(".") > 0) {
                s = s.replaceAll("0+?$", "");// 去掉多余的0
                s = s.replaceAll("[.]$", "");// 如最后一位是.则去掉
            }
            return s;
        }

    }
}
