package com.baidu.aip.asrwakeup3.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ChineseNumberUtil {

    public static String convertEnglishString(String numberStr) {
        /**
         * negative, zero, one, two, three, four, five, six, seven,
         * eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen,
         *  sixteen, seventeen, eighteen, nineteen, twenty, thirty, forty, fifty,
         *  sixty, seventy, eighty, ninety, hundred, thousand, million
         */
        Map map = new HashMap();
        map.put("negative", -1);
        map.put("zero", 0);
        map.put("point", -2);
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);
        map.put("five", 5);
        map.put("six", 6);
        map.put("seven", 7);
        map.put("eight", 8);
        map.put("nine", 9);
        map.put("ten", 10);
        map.put("eleven", 11);
        map.put("twelve", 12);
        map.put("thirteen", 13);
        map.put("fourteen", 14);
        map.put("fifteen", 15);
        map.put("sixteen", 16);
        map.put("seventeen", 17);
        map.put("eighteen", 18);
        map.put("nineteen", 19);
        map.put("twenty", 20);
        map.put("thirty", 30);
        map.put("forty", 40);
        map.put("fifty", 50);
        map.put("sixty", 60);
        map.put("seventy", 70);
        map.put("eighty", 80);
        map.put("ninety", 90);
        map.put("hundred", 100);
        map.put("thousand", 1000);
        map.put("million", 1000000);


            int sum = 0;//总数
            int acc = 0;//累加到某一步的数

            int flag = 1;
            String str1 = numberStr;

            str1 = str1.trim();
            if(str1.length()==0){
                return str1;
            }
            String[] strs = str1.split(" ");

            String result = "";

            for(String str : strs){
                if(str.equals("hundred")){
                    sum += acc* (Integer)map.get(str);
                    acc = 0;
                }else if(str.equals("negative")){
                    flag = -1;
                    acc = 0;
                }else if(str.equals("thousand") || str.equals("million")){
                    sum += acc*((Integer)map.get(str));
                    acc = 0;
                } else if(str.equals("point")) {
                    sum += acc;
                    result = sum + ".";
                    acc = 0;
                }else {
                    if (map.get(str) != null){
                        acc = (Integer)map.get(str);
                        if (acc >= 10) {
                            sum += acc;
                            acc = 0;
                        }
                        if (result.contains(".")) {
                            result += acc;
                        }
                    }
                }
            }

        if (result.contains(".")) {
            return result;
        }

        return String.valueOf(sum + acc);
    }

    public static String convertString(String string) {
        StringBuilder builder = new StringBuilder();
        List<NumberEnum> tempList = new ArrayList<>();
        // 这个个标识位，用来判断当前数字 只有1-9
        boolean isSimple = true;
        for (int i = 0; i < string.length(); i++) {
            NumberEnum numberEnum = NumberEnum.getByChar(string.charAt(i));
            if (numberEnum == null && tempList.size() != 0) {
                if (isSimple) {
                    builder.append(convert2Simple(tempList));
                } else if (tempList.size() == 1 && tempList.get(0).type > 2) {
                    builder.append(string.charAt(i - 1));
                } else {
                    builder.append(convert2Number(tempList));
                }
                tempList = new ArrayList<>();
            }
            if (numberEnum == null) {
                isSimple = true;
                builder.append(string.charAt(i));
                continue;
            }
            if (numberEnum.type > 1 && isSimple) {
                isSimple = false;
                if (tempList.size() >= 2) {
                    builder.append(convert2Simple(tempList.subList(0, tempList.size() - 1)));
                    NumberEnum temp = tempList.get(tempList.size() - 1);
                    // G友反映的Bug 如果前一段末位为0 ，则计算总数时不正确
                    if (NumberEnum.ZERO.equals(temp)) {
                        temp = NumberEnum.OPT_ZERO;
                    }
                    tempList = new ArrayList<>();
                    tempList.add(temp);
                }
            }
            tempList.add(numberEnum);
            if (i == string.length() - 1) {
                if (isSimple) {
                    builder.append(convert2Simple(tempList));
                } else if (tempList.size() == 1 && tempList.get(0).type > 2) {
                    builder.append(string.charAt(i));
                } else {
                    builder.append(convert2Number(tempList));
                }
            }
        }
        return builder.toString();
    }

    private static String convert2Simple(List<NumberEnum> tempList) {
        StringBuilder builder = new StringBuilder();
        for (NumberEnum numberEnum : tempList) {
            builder.append(numberEnum.value);
        }
        return builder.toString();
    }

    private static String convert2Number(List<NumberEnum> numberList) {
        boolean optZero = false;
        if (numberList.size() >= 1 && numberList.get(0).equals(NumberEnum.OPT_ZERO)) {
            optZero = true;
            numberList.set(0, NumberEnum.ONE);
        }
        List<NumberEnum> tempList = new ArrayList<ChineseNumberUtil.NumberEnum>();
        Long result = 0L;
        for (int i = 0; i < numberList.size(); i++) {
            NumberEnum numberEnum = numberList.get(i);
            if (numberEnum.type == 4) {
                if (result >= NumberEnum.TEN_THOUSAND.value) {
                    result = (result + convert2BasicNum(tempList)) * numberEnum.value;
                } else {
                    result = result + convert2BasicNum(tempList) * numberEnum.value;
                }
                tempList = new ArrayList<>();
            } else {
                tempList.add(numberList.get(i));
            }
            if (i == numberList.size() - 1) {
                result = result + convert2BasicNum(tempList);
            }
        }
        return optZero ? result.toString().replaceFirst("1", "0") : result.toString();
    }

    private static Long convert2BasicNum(List<NumberEnum> numberList) {
        // 默认设置
        NumberEnum last = NumberEnum.ONE;
        Long result = 0L;
        for (int i = 0; i < numberList.size(); i++) {
            NumberEnum numberEnum = numberList.get(i);
            if (numberEnum.type == 2 || numberEnum.type == 3) {
                if (last == NumberEnum.ZERO) {
                    last = NumberEnum.ONE;
                }
                result = result + last.value * numberEnum.value;
            }
            if (i == numberList.size() - 1 && numberEnum.type == 1) {
                if (last == NumberEnum.ZERO || numberList.size() == 1) {
                    result = result + numberEnum.value;
                } else {
                    result = result + (numberEnum.value * last.value) / 10;
                }
            }
            last = numberEnum;
        }
        return result;
    }

    public static void main(String[] args) {
        String number = "三百零十一";
        System.out.println(ChineseNumberUtil.convertString(number));
    }

    enum NumberEnum {
        // OPT_ZERO 用于标识前段末位为0
        OPT_ZERO(null, null, null), ZERO("零〇", 0L, 1), ONE("一壹", 1L, 1), TWO("二两贰", 2L, 1), THREE("三叁", 3L, 1), FOUR("四肆事", 4L, 1), FIVE("五伍", 5L, 1), SIX("六陆",
                6L, 1), SEVEN("七柒", 7L, 1), EIGHT("八捌", 8L, 1), NINE("九玖", 9L, 1), TEN("十拾实", 10L,
                2), HUNDRED("百佰", 100L, 3), THOUSAND("千仟", 1000L, 3), TEN_THOUSAND("万萬", 10000L, 4), HUNDRED_MILLION("亿億", 100000000L, 4);

        String key;
        Long value;
        Integer type;
        private static Map<Character, NumberEnum> map;

        public static NumberEnum getByChar(Character character) {
            if (map == null) {
                map = new HashMap<>();
                for (NumberEnum number : NumberEnum.values()) {
                    if (number.equals(OPT_ZERO)) {
                        continue;
                    }
                    for (int i = 0; i < number.key.length(); i++) {
                        map.put(number.key.charAt(i), number);
                    }
                    if (number.type == 1) {
                        map.put(number.value.toString().charAt(0), number);
                    }
                }
            }
            return map.get(character);
        }

        private NumberEnum(String key, Long value, Integer type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }
    }

    /**
     * 是否是数字或小数
     * @tags @return
     * @exception
     * @author wanghc
     * @date 2015-9-16 下午5:50:15
     * @return boolean
     */
    public static boolean isNumber(String str){
        if(TextUtils.isEmpty(str)){
            return false;
        }
        String reg = "\\d+(\\.\\d+)?";
        return str.matches(reg);

    }
}
