package cn.jarkata.mybatis.page;

import org.junit.Test;

public class SqlUtilsTest {

    @Test
    public void genCountSql() {
        String countSql = SqlUtils.genCountSql("select id from user order by id desc");
        System.out.println(countSql);
    }

    @Test
    public void testGenCountSql() {
        String countSql = SqlUtils.genCountSql("select id from user order \n\r    by id desc");
        System.out.println(countSql);
    }
}