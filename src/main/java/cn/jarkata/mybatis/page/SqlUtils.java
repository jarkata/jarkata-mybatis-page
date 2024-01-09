package cn.jarkata.mybatis.page;

public class SqlUtils {

    public static String genCountSql(String sql) {
        sql = trimSql(sql);
        int fromIndex = sql.toLowerCase().lastIndexOf("from");
        if (fromIndex <= 0) {
            throw new IllegalArgumentException("");
        }
        int orderIndex = sql.toLowerCase().lastIndexOf("order");
        String tmpSuffix;
        if (orderIndex > 0) {
            tmpSuffix = sql.substring(fromIndex, orderIndex);
        } else {
            tmpSuffix = sql.substring(fromIndex);
        }

        return "SELECT count(1) " + tmpSuffix;
    }

    private static String trimSql(String sql) {
        sql = sql.replaceAll("\n", "");
        sql = sql.replaceAll("\\s+", " ");
        return sql;
    }
}
