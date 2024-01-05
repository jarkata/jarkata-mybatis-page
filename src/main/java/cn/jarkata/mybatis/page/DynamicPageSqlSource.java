package cn.jarkata.mybatis.page;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Map;
import java.util.Objects;

public class DynamicPageSqlSource implements SqlSource {

    private final Configuration configuration;
    private final PageRequest pageRequest;
    private final SqlNode rootSqlNode;

    public DynamicPageSqlSource(Configuration configuration, SqlSource dynamicSqlSource, PageRequest pageRequest) {
        this.configuration = configuration;
        this.rootSqlNode = (SqlNode) ReflectionUtils.getFieldValue(dynamicSqlSource, "rootSqlNode");
        this.pageRequest = pageRequest;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context = new DynamicContext(this.configuration, parameterObject);
        this.rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(this.configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        String contextSql = context.getSql();
        contextSql = genPageSql(contextSql, pageRequest);
        SqlSource sqlSource = sqlSourceParser.parse(contextSql, parameterType, context.getBindings());
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        Map<String, Object> contextBindings = context.getBindings();
        Objects.requireNonNull(boundSql);
        contextBindings.forEach(boundSql::setAdditionalParameter);
        return boundSql;
    }

    private String genPageSql(String sql, PageRequest pageRequest) {
        sql = sql + " limit " + pageRequest.getOffset() + "," + pageRequest.getLimit();
        return sql;
    }
}
