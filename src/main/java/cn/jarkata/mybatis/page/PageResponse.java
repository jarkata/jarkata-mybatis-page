package cn.jarkata.mybatis.page;

import java.util.ArrayList;

/**
 * 分页响应对象
 *
 * @param <T> 范型对象
 */
public class PageResponse<T> extends ArrayList<T> {

    private int pageNo;
    private int pageSize;
    private int totalCount;
    private int totalPage;

    public PageResponse() {
    }

    public PageResponse(int pageNo, int pageSize) {
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

}
