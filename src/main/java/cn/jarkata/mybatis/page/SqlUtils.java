package cn.jarkata.mybatis.page;

import cn.jarkata.commons.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

    private static final Pattern compile9 = Pattern.compile("\\r");
    private static final Pattern compile10 = Pattern.compile("\\s+");

    public static String genCountSql(String sql) {
        sql = trimSql(sql);
        int fromIndex = sql.toLowerCase().lastIndexOf("from");
        if (fromIndex <= 0) {
            throw new IllegalArgumentException("");
        }
        int orderIndex = sql.toLowerCase().lastIndexOf("order by");
        String tmpSuffix;
        if (orderIndex > 0) {
            tmpSuffix = sql.substring(fromIndex, orderIndex);
        } else {
            tmpSuffix = sql.substring(fromIndex);
        }

        return "SELECT count(1) " + tmpSuffix;
    }

    private static String trimSql(String sql) {
        Matcher matched = compile9.matcher(sql);
        if (matched.find()) {
            sql = matched.replaceAll("");
        }
        Matcher spaceMatcher = compile10.matcher(sql);
        if (spaceMatcher.find()) {
            sql = spaceMatcher.replaceAll(" ");
        }
        sql = StringUtils.trimSpecialChar(sql).trim();
        return sql;
    }
}
