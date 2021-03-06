package com.bowen.middleware.utils.model;

import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {
    private static ConcurrentHashMap<String,RpcFuture> processingRpc = new ConcurrentHashMap<>();
    public static void put(String requestId, RpcFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static RpcFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
