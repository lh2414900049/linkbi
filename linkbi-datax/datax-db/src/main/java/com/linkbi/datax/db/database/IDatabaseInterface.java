
package com.linkbi.datax.db.database;

import java.util.List;
import java.util.Map;

import com.linkbi.datax.db.model.ColumnDescription;
import com.linkbi.datax.db.model.ColumnMetaData;
import com.linkbi.datax.db.model.JdbcSourceData;
import com.linkbi.datax.db.model.TableDescription;

/**
 * 数据库访问通用业务接口
 * 
 * @author
 *
 */
public interface IDatabaseInterface /*extends AutoCloseable*/ {

	/**
	 * 建立数据库连接
	 * 
	 * @param jdbcSourceData  JDBC信息
	 */
	public void connect(JdbcSourceData jdbcSourceData);
	public void getDataSource(JdbcSourceData jdbcSourceData);
	/**
	 * 断开数据库连接
	 */
	//@Override
	public void close();


	/**
	 * 获取数据库的模式schema列表
	 * 
	 * @return 模式名列表
	 */
	public List<String> querySchemaList();

	/**
	 * 获取指定模式Schema内的所有表列表
	 * 
	 * @param schemaName 模式名称
	 * @return 表及视图名列表
	 */
	public List<TableDescription> queryTableList(String schemaName,String tableName);

	/**
	 * 获取指定模式表的元信息
	 * 
	 * @param schemaName 模式名称
	 * @param tableName  表或视图名称
	 * @return 字段元信息列表
	 */
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);

	public String queryModelSQL(String schemaName, String tableName);
	/**
	 * 获取指定查询SQL的元信息
	 * 
	 * @param sql SQL查询语句
	 * @return 字段元信息列表
	 */
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql);

	public long queryTableMaxId(String tableName, String pkName);
	/**
	 * 获取指定模式表的主键字段列表
	 * 
	 * @param schemaName 模式名称
	 * @param tableName  表名称
	 * @return 主键字段名称列表
	 */
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName);

	/**
	 * 通过sql查询语句获取数据
	 *
	 * @param schemaName 模式名称
	 * @return 表及视图名列表
	 */
	public Map<String,Object> queryDataList(String schemaName, String tableName, String querySql);

	/**
	 * 根据数据库类型格式化sql语句
	 *
	 * @param schemaName 模式名称
	 * @return 表及视图名列表
	 */
	public String getQuerySql(String schemaName, String tableName, String querySql);


	/**
	 * 测试查询SQL语句的有效性
	 * 
	 * @param sql 待验证的SQL语句
	 */
	public void testQuerySQL(String sql);

	/**
	 * 测试查询SQL语句的有效性
	 *
	 * @param sql 待验证的SQL语句
	 */
	public void executeUpdate(String sql);

	/**
	 * 获取数据库的表全名
	 * 
	 * @param schemaName 模式名称
	 * @param tableName  表名称
	 * @return 表全名
	 */
	public String getQuotedSchemaTableCombination(String schemaName, String tableName);

	/**
	 * 获取字段列的结构定义
	 * 
	 * @param v      值元数据定义
	 * @param pks    主键字段名称列表
	 * @param addCr  是否结尾换行
	 * @param useAutoInc 是否自增
	 * @return 字段定义字符串
	 */
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr);

	/**
	 * 主键列转换为逗号分隔的字符串
	 * 
	 * @param pks 主键字段列表
	 * @return 主键字段拼接串
	 */
	public String getPrimaryKeyAsString(List<String> pks);

	/**
	 * 主键列转换为逗号分隔的字符串
	 *
	 * @param tableName 主键字段列表
	 * @return 主键字段拼接串
	 */
	public String getTableMaxIdSql(String tableName, String pkName);
	/**
	 * SQL语句格式化
	 * 
	 * @param sql SQL的语句
	 * @return 格式化后的SQL语句
	 */
	public String formatSQL(String sql);
}
