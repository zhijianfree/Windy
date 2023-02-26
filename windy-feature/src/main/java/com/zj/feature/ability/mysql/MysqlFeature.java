package com.zj.feature.ability.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.Constant;
import com.zj.feature.ability.Feature;
import com.zj.feature.ability.FeatureDefine;
import com.zj.feature.ability.ParameterDefine;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.executor.compare.ParamValueType;
import com.zj.feature.utils.ExceptionUtils;
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
    public ExecuteDetail executeQuery(String connect,String dbName,String user,String password,String sql){
        ExecuteDetail executeDetail = new ExecuteDetail();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String jdbcUrl = String.format(this.jdbcUrl, connect, dbName);
        try {
            //加载驱动
            Class.forName(driver);
            //获取数据库连接
            connection = DriverManager.getConnection(jdbcUrl, user, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
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

            executeDetail.setStatus(true);
            executeDetail.setResBody(jsonArray);
        } catch (Exception e) {
            log.info("execute mysql query sql error={}", ExceptionUtils.getSimplifyError(e));
            executeDetail.setErrorMessage(ExceptionUtils.getSimplifyError(e));
            executeDetail.setStatus(false);
        }

        executeDetail.addRequestInfo("url: " + jdbcUrl);
        executeDetail.addRequestInfo("user: " + user);
        executeDetail.addRequestInfo("password: " + user);
        executeDetail.addRequestInfo("sql: " + sql);

        return executeDetail;
    }

    @Override
    public List<FeatureDefine> scanFeatureDefines() {

        List<ParameterDefine> parameterDefines = new ArrayList<>();
        ParameterDefine connect = new ParameterDefine();
        connect.setParamKey("connect");
        connect.setType(ParamValueType.String.getType());
        parameterDefines.add(connect);

        ParameterDefine dbName = new ParameterDefine();
        dbName.setParamKey("dbName");
        dbName.setType(ParamValueType.String.getType());
        parameterDefines.add(dbName);

        ParameterDefine user = new ParameterDefine();
        user.setParamKey("user");
        user.setType(ParamValueType.String.getType());
        parameterDefines.add(user);

        ParameterDefine password = new ParameterDefine();
        password.setParamKey("password");
        password.setType(ParamValueType.String.getType());
        parameterDefines.add(password);

        ParameterDefine sql = new ParameterDefine();
        sql.setParamKey("sql");
        sql.setType(ParamValueType.String.getType());
        parameterDefines.add(sql);

        FeatureDefine featureDefine = new FeatureDefine();
        featureDefine.setSource(MysqlFeature.class.getName());
        featureDefine.setDescription("数据库操作");
        featureDefine.setMethod("executeQuery");
        featureDefine.setName("Mysql");
        featureDefine.setParams(parameterDefines);
        return Collections.singletonList(featureDefine);
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(new MysqlFeature().scanFeatureDefines()));
    }
}
