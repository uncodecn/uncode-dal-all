package cn.uncode.dal.log.asynlog;


/**
 * 异步Log接口
 * 
 * @author juny.ye
 */
public interface IWriter<T> {

    /**
     * 单个写
     * 
     * @param content
     */
    public void write(T content);

}
