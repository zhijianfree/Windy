package com.zj.client.handler.feature.ability.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.client.utils.ExceptionUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class MysqlFeature implements Feature {
    private String driver = "org.mariadb.jdbc.Driver";
    private String jdbcUrl = "jdbc:mysql://%s/%s";
    public ExecuteDetailVo executeQuery(String connect,String dbName,String user,String password,String executeSql){
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String jdbc = String.format(this.jdbcUrl, connect, dbName);
        try {
            //加载驱动
            Class.forName(driver);
            //获取数据库连接
            connection = DriverManager.getConnection(jdbc, user, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(executeSql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            JSONArray jsonArray = new JSONArray();
            while (resultSet.next()){
                JSONObject jsonObject = new JSONObject();
                for (int position = 1; position <=  metaData.getColumnCount(); position ++){
                    String columnName = metaData.getColumnName(position);
                    Object columnValue = resultSet.getObject(position);
                    jsonObject.put(columnName, columnValue);
                }

                jsonArray.add(jsonObject);
            }

            executeDetailVo.setStatus(true);
            executeDetailVo.setResBody(jsonArray);
        } catch (Exception e) {
            log.info("execute mysql query sql error={}", ExceptionUtils.getSimplifyError(e));
            executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
            executeDetailVo.setStatus(false);
        }

        executeDetailVo.addRequestInfo("url: " + jdbcUrl);
        executeDetailVo.addRequestInfo("user: " + user);
        executeDetailVo.addRequestInfo("password: " + user);
        executeDetailVo.addRequestInfo("sql: " + sql);

        return executeDetailVo;
    }

    @Override
    public List<FeatureDefine> scanFeatureDefines() {

        List<ParameterDefine> parameterDefines = new ArrayList<>();
        ParameterDefine connect = new ParameterDefine();
        connect.setParamKey("connect");
        connect.setType(ParamValueType.String.name());
        parameterDefines.add(connect);

        ParameterDefine dbName = new ParameterDefine();
        dbName.setParamKey("dbName");
        dbName.setType(ParamValueType.String.name());
        parameterDefines.add(dbName);

        ParameterDefine user = new ParameterDefine();
        user.setParamKey("user");
        user.setType(ParamValueType.String.name());
        parameterDefines.add(user);

        ParameterDefine password = new ParameterDefine();
        password.setParamKey("password");
        password.setType(ParamValueType.String.name());
        parameterDefines.add(password);

        ParameterDefine sql = new ParameterDefine();
        sql.setParamKey("sql");
        sql.setType(ParamValueType.String.name());
        parameterDefines.add(sql);

        FeatureDefine featureDefine = new FeatureDefine();
        featureDefine.setSource(MysqlFeature.class.getName());
        featureDefine.setDescription("数据库操作");
        featureDefine.setMethod("executeQuery");
        featureDefine.setName("Mysql");
        featureDefine.setParams(parameterDefines);
        return Collections.singletonList(featureDefine);
    }

}
