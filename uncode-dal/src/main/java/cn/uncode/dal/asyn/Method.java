package cn.uncode.dal.asyn;

public enum Method {
	
	SELECT_BY_CRITERIA(1),
	
	COUNT_BY_CRITERIA(5),
	
	SELECT_BY_PRIMARY_KEY(6),
	
	UPDATE(7),
	
	DELETE(8),
	
	INSERT(2), INSERT_TABLE(3), INSERT_DATABASE_TABLE(4);

	public final int type;

	Method(int type) {
		this.type = type;
	}

}
