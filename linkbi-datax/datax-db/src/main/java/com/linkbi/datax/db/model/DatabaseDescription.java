
package com.linkbi.datax.db.model;

import com.linkbi.datax.db.constant.DBTypeEnum;

/**
 * 数据库连接描述符信息定义(Database Description)
 * @author
 *
 */
public class DatabaseDescription {
	protected DBTypeEnum type;
	protected String host;
	protected int port;
	protected String mode;//对于Oracle数据库的模式，可取范围为：sid,servicename,tnsname三种
	protected String dbname;
	protected String charset;
	protected String username;
	protected String password;
	
	public DatabaseDescription(String dbtype, String host, int port, String mode, String dbname, String charset,String username, String password) {
		this.type = DBTypeEnum.valueOf(dbtype.toUpperCase());
		this.host = host;
		this.port = port;
		this.mode = mode;
		this.dbname = dbname;
		this.charset = charset;
		this.username = username;
		this.password = password;
	}
	
	public DBTypeEnum getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getMode() {
		return mode;
	}
	
	public String getDbname() {
		return dbname;
	}

	public String getCharset() {
		return charset;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "DatabaseDescription [type=" + type + ", host=" + host + ", port=" + port + ", mode=" + mode
				+ ", dbname=" + dbname + ", charset=" + charset + ", username=" + username + ", password=" + password
				+ "]";
	}

}
