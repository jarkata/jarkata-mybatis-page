package cn.jarkata.mybatis.page.interceptor;

import cn.jarkata.mybatis.page.DynamicPageSqlSource;
import cn.jarkata.mybatis.page.PageRequest;
import cn.jarkata.mybatis.page.PageResponse;
import cn.jarkata.mybatis.page.ReflectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 分页查询的拦截器
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class JarkataPageInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(JarkataPageInterceptor.class);

    /**
     * 拦截方法
     *
     * @param invocation 拦截的参数对象
     * @return 返回分页查询返回等对象
     * @throws Throwable 查询发生异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Object rowBounds = args[2];
        if (!(rowBounds instanceof PageRequest)) {
            logger.debug("No Use Page");
            return invocation.proceed();
        }
        long start = System.currentTimeMillis();
        PageResponse<Object> pageResponse = new PageResponse<>();
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
     * @throws Exception 查询异常
     */
    private PageResponse<Object> findPage(Invocation invocation) throws Exception {
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
        MappedStatement pageStatement = createMappedStatement(statement, pageRequest);
        args[0] = pageStatement;
        args[1] = parameter;
        args[2] = RowBounds.DEFAULT;
        Object proceed = invocation.proceed();
        pageResponse.setData((List) proceed);
        return pageResponse;
    }


    /**
     * @return 映射句柄
     */
    private MappedStatement createMappedStatement(MappedStatement statement, PageRequest pageRequest) {
        // 配置
        Configuration configuration = statement.getConfiguration();
        SqlSource sqlSource = statement.getSqlSource();
        SqlSource pageBoundSql = new DynamicPageSqlSource(configuration, sqlSource, pageRequest);
        return makeStatement(statement, pageBoundSql);
    }

    /**
     * 获取总记录数
     *
     * @return 查询汇总结果
     * @throws SQLException 查询异常
     */
    private long getTotalCount(MappedStatement statement, BoundSql boundSql) throws SQLException {
        Object parmeterObject = null;
        String countSql = null;
        long count = -1L;
        long start = System.currentTimeMillis();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepareStatement = null;
        try {
            Configuration configuration = statement.getConfiguration();
            Environment environment = configuration.getEnvironment();
            DataSource dataSource = environment.getDataSource();
            connection = dataSource.getConnection();
            parmeterObject = boundSql.getParameterObject();
            countSql = genCountSql(boundSql);
            prepareStatement = connection.prepareStatement(countSql);
            bindParameter(boundSql, prepareStatement);
            resultSet = prepareStatement.executeQuery();
            if (Objects.nonNull(resultSet) && resultSet.next()) {
                count = resultSet.getLong(1);
            }
        } finally {
            if (Objects.nonNull(resultSet)) {
                resultSet.close();
            }
            if (Objects.nonNull(prepareStatement)) {
                prepareStatement.close();
            }
            if (Objects.nonNull(connection)) {
                connection.close();
            }
            long dur = System.currentTimeMillis() - start;
            logger.info("dur={}ms,sql={},param:{}", dur, countSql, parmeterObject);
        }
        return count;
    }

    private String genCountSql(BoundSql boundSql) {
        String sql = boundSql.getSql();
        sql = trimSql(sql);
        int fromIndex = sql.toLowerCase().lastIndexOf("from");
        if (fromIndex <= 0) {
            throw new IllegalArgumentException("");
        }
        String tmpSuffix = sql.substring(fromIndex);
        return "SELECT count(1) " + tmpSuffix;
    }


    private void bindParameter(BoundSql boundSql, PreparedStatement preparedStatement) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int index = 0, len = parameterMappings.size(); index < len; index++) {
            ParameterMapping parameterMapping = parameterMappings.get(index);
            String mappingProperty = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(mappingProperty)) {
                Object parameter = boundSql.getAdditionalParameter(mappingProperty);
                preparedStatement.setObject(index + 1, parameter);
                continue;
            }
            Object parameterObject = boundSql.getParameterObject();
            Object paramVal;
            if (parameterObject instanceof Map) {
                Map<String, Object> parameterObjectMap = (Map<String, Object>) parameterObject;
                paramVal = parameterObjectMap.get(mappingProperty);
            } else {
                paramVal = ReflectionUtils.getFieldValue(parameterObject, mappingProperty);
            }
            preparedStatement.setObject(index + 1, paramVal);
        }
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
     * @param statement    statement对象
     * @param pageBoundSql SqlSource对象
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