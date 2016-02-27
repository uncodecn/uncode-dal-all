package cn.uncode.dal.router;

public interface MasterSlaveRouter {
    
    public void routeToMaster();
    
    public void routeToSlave();
    
    public void routeToShard(String shard);

}
