package com.example.C08_abstract_interface;

import java.util.ArrayList;
import java.util.List;

class RpcRequest { }

class RpcException extends Exception { }

// 接口
interface Filter {
    void doFilter(RpcRequest request) throws RpcException;
}

// 接口实现类：鉴权过滤器
class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(RpcRequest req) throws RpcException { System.out.println("AuthenticationFilter"); }
}
// 接口实现类：限流过滤器
class RateLimitFilter implements Filter {
    @Override
    public void doFilter(RpcRequest req) throws RpcException { System.out.println("RateLimitFilter"); }
}

// 使用过滤器
public class FilterApp {
    private List<Filter> filterList;
    public FilterApp(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public void handleRpcRequest(RpcRequest req) {
        try {
            for (Filter filter: filterList) {  // 调用使用的过滤器
                filter.doFilter(req);
            }
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<Filter> filterList = new ArrayList<>();
        filterList.add(new AuthenticationFilter());
        filterList.add(new RateLimitFilter());
        FilterApp filterApp = new FilterApp(filterList);
        filterApp.handleRpcRequest(new RpcRequest());
    }
}
