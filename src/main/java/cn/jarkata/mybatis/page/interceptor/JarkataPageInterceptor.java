package cn.jarkata.mybatis.page.interceptor;

import cn.jarkata.mybatis.page.PageRequest;
import cn.jarkata.mybatis.page.PageResponse;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


/**
 * 分页查询的拦截器
 */
@Intercepts(@Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
)
public class JarkataPageInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(JarkataPageInterceptor.class);

    /**
     * 拦截方法
     *
     * @param invocation 拦截的参数对象
     * @return 返回分页查询返回等对象
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        Object rowBounds = args[2];
        if (!(rowBounds instanceof PageRequest)) {
            logger.debug("No Use Page");
            return invocation.proceed();
        }
        long start = System.currentTimeMillis();
        PageResponse<Object> pageResponse = null;
        try {
            pageResponse = findPage(invocation);
        } finally {
            long dur = System.currentTimeMillis() - start;
            logger.info("dur={}ms,ReturnObject：{}", dur, pageResponse);
        }
        return pageResponse;
    }

    /**
     * 分页查询数据
     *
     * @param invocation 反射的代理对象
     * @return 分页对象
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private PageResponse<Object> findPage(Invocation invocation) throws SQLException, InvocationTargetException, IllegalAccessException {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        Object rowBounds = args[2];
        PageRequest pageRequest = (PageRequest) rowBounds;
        BoundSql boundSql = statement.getBoundSql(parameter);
        PageResponse<Object> pageResponse = new PageResponse<>(pageRequest);
        //查询总数据
        long totalCount = getTotalCount(statement, boundSql);
        pageResponse.setTotalCount(totalCount);
        if (totalCount <= 0) {
            logger.warn("SelectResultEmpty，boundSql={},parameter={}", boundSql, parameter);
            return pageResponse;
        } else {
            logger.info("TotalCount：{}", totalCount);
        }
        MappedStatement pageStatment = createMappedStatement(statement, boundSql, pageRequest);
        args[0] = pageStatment;
        args[1] = parameter;
        args[2] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
        Object proceed = invocation.proceed();
        pageResponse.setData((List) proceed);
        return pageResponse;
    }


    /**
     * @param statement
     * @param boundSql
     * @param pageRequest
     * @return
     */
    private MappedStatement createMappedStatement(MappedStatement statement, BoundSql boundSql, PageRequest pageRequest) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql();
        sql = sql + " limit " + pageRequest.getOffset() + "," + pageRequest.getLimit();
        sql = trimSql(sql);
        // 配置
        Configuration configuration = statement.getConfiguration();
        SqlSource pageBoundSql = new StaticSqlSource(configuration, sql, parameterMappings);
        return makeStatement(statement, pageBoundSql);
    }

    /**
     * 获取总记录数
     *
     * @param statement
     * @param boundSql
     * @return
     * @throws SQLException
     */
    private long getTotalCount(MappedStatement statement, BoundSql boundSql) throws SQLException {
        Map<String, Object> parameterObjectMap = null;
        String countSql = null;
        long count = -1;
        long start = System.currentTimeMillis();
        try {
            Configuration configuration = statement.getConfiguration();
            Environment environment = configuration.getEnvironment();
            DataSource dataSource = environment.getDataSource();
            Connection connection = dataSource.getConnection();
            String sql = boundSql.getSql();
            sql = trimSql(sql);
            Object parmeterObject = boundSql.getParameterObject();
            countSql = "select count(1) from (" + sql + ") count";
            PreparedStatement prepareStatement = connection.prepareStatement(countSql);
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            parameterObjectMap = (Map<String, Object>) parmeterObject;
            for (int index = 0, len = parameterMappings.size(); index < len; index++) {
                ParameterMapping parameterMapping = parameterMappings.get(index);
                String property = parameterMapping.getProperty();
                Object paramVal = parameterObjectMap.get(property);
                prepareStatement.setObject(index + 1, paramVal);
            }
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
        } finally {
            long dur = System.currentTimeMillis() - start;
            logger.info("dur={}ms,sql={},param:{}", dur, countSql, parameterObjectMap);
        }
        return count;
    }

    /**
     * 去除sql的格式
     *
     * @param sql sql语句
     * @return 格式化之后的sql语句
     */
    private String trimSql(String sql) {
        sql = sql.replaceAll("\n", "");
        sql = sql.replaceAll("\\s+", " ");
        return sql;
    }

    /**
     * 赋值查询的statement对象
     *
     * @param statement
     * @param pageBoundSql SqlSource对象
     * @return
     */
    private MappedStatement makeStatement(MappedStatement statement, SqlSource pageBoundSql) {
        Configuration configuration = statement.getConfiguration();
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, statement.getId(), pageBoundSql, statement.getSqlCommandType());
        builder.fetchSize(statement.getFetchSize());
        builder.flushCacheRequired(statement.isFlushCacheRequired());
        builder.parameterMap(statement.getParameterMap());
        builder.resultOrdered(statement.isResultOrdered());
        builder.useCache(statement.isUseCache());
        builder.timeout(statement.getTimeout());
        builder.databaseId(statement.getDatabaseId());
        builder.resultSetType(statement.getResultSetType());
        builder.statementType(statement.getStatementType());
        builder.resultMaps(statement.getResultMaps());
        builder.resource(statement.getResource());
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
