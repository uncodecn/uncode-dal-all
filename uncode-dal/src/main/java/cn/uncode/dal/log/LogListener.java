package cn.uncode.dal.log;


import org.apache.commons.lang3.StringUtils;

import cn.uncode.dal.listener.OprateInfo;
import cn.uncode.dal.listener.OprateListener;
import cn.uncode.dal.listener.Oprator;
import cn.uncode.dal.log.asynlog.AsynWriter;

/**
 * 
 * @author juny.ye
 */
public class LogListener implements OprateListener {
	
	private String methods;

    private AsynWriter<String> writer = new AsynWriter<String>();


    @Override
    public void oprate(Oprator oprator, OprateInfo oprateInfo) {
        writer.write(buildLog(oprator, oprateInfo));
    }

    /**
     * 日志格式
     * 
     * @param type
     * @param isHit
     * @param useTime
     * @param ip
     * @param key
     * @return
     */
    private String buildLog(Oprator oprator, OprateInfo oprateInfo) {
    	StringBuilder sb = new StringBuilder();
    	if(StringUtils.isNotEmpty(methods)){
    		if(methods.indexOf(oprator.type) != -1){
    			if(null != oprateInfo.getFields()){
    				sb.append("fields:{");
    				sb.append(oprateInfo.getFields());
    				sb.append("},");
    			}
    			if(null != oprateInfo.getResult()){
    				sb.append("result:{");
    				sb.append(oprateInfo.getResult());
    				sb.append("},");
    			}
    			if(null != oprateInfo.getId()){
    				sb.append("id:");
    				sb.append(oprateInfo.getId());
    			}
    			if(oprateInfo.getSeconds() > 0){
    				sb.append("seconds:");
    				sb.append(oprateInfo.getSeconds());
    			}
    			if(null != oprateInfo.getResultList()){
    				sb.append("resultList:{");
    				sb.append(oprateInfo.getResultList());
    				sb.append("},");
    			}
    			if(null != oprateInfo.getResultMap()){
    				sb.append("resultMap:{");
    				sb.append(oprateInfo.getResultMap());
    				sb.append("},");
    			}
    			if(null != oprateInfo.getTable()){
    				sb.append("table:");
    				sb.append(oprateInfo.getTable());
    			}
    			if(null != oprateInfo.getQueryCriteria()){
    				sb.append("queryCriteria:{");
    				sb.append(oprateInfo.getQueryCriteria());
    				sb.append("},");
    			}
    		}
    	}

        return sb.toString();
    }

	public String getMethods() {
		return methods;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}
    
    

}