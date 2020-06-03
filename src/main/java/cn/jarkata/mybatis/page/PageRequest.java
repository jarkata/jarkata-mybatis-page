package cn.jarkata.mybatis.page;

import org.apache.ibatis.session.RowBounds;

/**
 * 分页请求信息
 */
public class PageRequest extends RowBounds {

    private int pageNo = 1;
    private int pageSize = 10;

    public PageRequest() {
    }

    public PageRequest(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }


    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public int getOffset() {
        if (pageNo <= 0) {
            return 0;
        }
        return (pageNo - 1) * pageSize;
    }

    @Override
    public int getLimit() {
        return pageSize;
    }
}
