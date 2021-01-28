package com.oppo.cdo.instant.platform.user.core.util;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oppo.cdo.instant.platform.common.base.util.DateUtils;

public class OverSeaDataDealUtil {
    private static final Logger logger = LoggerFactory.getLogger(OverSeaDataDealUtil.class);

    public static  String getOverSeaSex(String sex) {
        if (StringUtils.isEmpty(sex)) {
            return null;
        }
        if ("male".equals(sex)){
            return "M";
        }else if ("female".equals(sex)) {
            return "F";
        }
        return null;
    }

 public static  Integer getOverSeaAge(String birthday) {
     if (StringUtils.isEmpty(birthday)) {
         return null;
     }
      try{
          return DateUtils.getAge(Long.parseLong(birthday));
      }catch (Exception e){
          logger.error("[getOverSeaAge] error,birthday:{}",birthday);

      }

        return null;
 }


    public static String getOverSeaBirthDay(String birthday) {
        if (StringUtils.isEmpty(birthday)) {
            return null;
        }
        try {
            String[] birthArray = StringUtils.split(birthday, "/");
            if (birthArray.length == 3) {
                String year = birthArray[2];
                String month = birthArray[0];
                String day = birthArray[1];
                String dataStr = StringUtils.joinWith("-", year, month, day)+" 23:00:00";
                Long dateMilltime = DateUtils.getDateFromString(dataStr, "yyyy-MM-dd HH:mm:ss").getTime();
                return String.valueOf(dateMilltime);

            }
        } catch (Exception e) {
            logger.error("[getOverSeaBirthDay] error,birthday:{}",birthday);
        }
      return null;
    }

    public static String parseGenderJSON(JSONObject debugResult) {
        try {
            JSONArray genders = (JSONArray) debugResult.get("genders");
            String gender = null;
            if (genders != null) {
                for (Object data : genders) {
                    Map metadata = (Map) data;
                    if (metadata != null && metadata.get("value") != null) {
                        gender = (String) metadata.get("value");
                        break;
                    }
                }
            }
            return gender;
        } catch (Exception e) {
            logger.error("[parseBirthdayJSON] end error debugrResult:{}", debugResult, e);
            return null;
        }
    }
    public static String parseEmailJSON(JSONObject debugResult) {
        try {
            JSONArray emailAddresses = (JSONArray) debugResult.get("emailAddresses");
            String email = null;
            if (emailAddresses != null) {
                for (Object data : emailAddresses) {
                    Map metadata = (Map) data;
                    if (metadata != null && metadata.get("value") != null) {
                        email = (String) metadata.get("value");
                        break;
                    }
                }
            }
            return email;
        } catch (Exception e) {
            logger.error("[parseBirthdayJSON] end error debugrResult:{}", debugResult, e);
            return null;
        }
    }
    public static String parseBirthdayJSON(JSONObject debugResult) {
        try {
            JSONArray birthdays = (JSONArray) debugResult.get("birthdays");
            String birthday = null;
            if (birthdays != null) {
                for (Object data : birthdays) {
                    Map metadata = (Map) data;
                    if (metadata != null && metadata.get("date") != null) {
                        Map dataMap = (Map) metadata.get("date");
                        String year = ObjectUtils.toString(dataMap.get("year"));
                        String month = ObjectUtils.toString(dataMap.get("month"));
                        String day = ObjectUtils.toString(dataMap.get("day"));
                        if (StringUtils.length(month) == 1) {
                            month = "0"+month;
                        }
                        if (StringUtils.length(day) == 1) {
                            day = "0"+day;
                        }
                        String dataStr = StringUtils.joinWith("-", year, month, day)+" 23:00:00";
                        Long dateMilltime = DateUtils.getDateFromString(dataStr, "yyyy-MM-dd HH:mm:ss").getTime();
                        birthday = String.valueOf(dateMilltime);
                        break;
                    }
                }
            }

            return birthday;
        }catch(Exception e) {
            logger.error("[parseGenderJSON] end error debugrResult:{}",debugResult,e);
            return null;
        }
    }
    public static  void main(String[] args){
        System.out.println(getOverSeaBirthDay("05/10/1995"));
        System.out.println(DateUtils.getYYMMDD("800118000000"));

        String str = "{\n" +
                "  \"resourceName\": \"people/113049657731635058241\",\n" +
                "  \"etag\": \"%EgYBBxBANy4aBAECBQc=\",\n" +
                "  \"birthdays\": [\n" +
                "    {\n" +
                "      \"metadata\": {\n" +
                "        \"primary\": true,\n" +
                "        \"source\": {\n" +
                "          \"type\": \"PROFILE\",\n" +
                "          \"id\": \"113049657731635058241\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"date\": {\n" +
                "        \"year\": 1998,\n" +
                "        \"month\": 3,\n" +
                "        \"day\": 1\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"metadata\": {\n" +
                "        \"source\": {\n" +
                "          \"type\": \"ACCOUNT\",\n" +
                "          \"id\": \"113049657731635058241\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"date\": {\n" +
                "        \"year\": 1998,\n" +
                "        \"month\": 3,\n" +
                "        \"day\": 1\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
         System.out.println("google birthday:"+parseBirthdayJSON(JSONObject.parseObject(str)));
        System.out.println("google birthday praseToStr:"+DateUtils.getYYMMDD("899222400000"));
    }
}
