package cn.jarkata.mybatis.page;

import org.apache.ibatis.session.RowBounds;

/**
 * 分页请求信息
 */
public class PageRequest extends RowBounds {
    
    public PageRequest() {
    }

    public PageRequest(int offset, int limit) {
        super(offset, limit);
    }
}
