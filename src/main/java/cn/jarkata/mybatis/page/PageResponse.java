package cn.jarkata.mybatis.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页响应对象
 *
 * @param <T> 范型对象
 */
public class PageResponse<T> extends ArrayList<T> {

    private int pageNo;
    private int pageSize;
    private long totalCount;

    public PageResponse() {
        pageNo = 1;
        pageSize = 10;
    }


    /**
     * 赋值请求对象当前页码，每页的大小
     *
     * @param pageRequest 请求对象
     */
    public PageResponse(PageRequest pageRequest) {
        this.pageNo = pageRequest.getPageNo();
        this.pageSize = pageRequest.getPageSize();
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalPage() {
        if (pageSize <= 0) {
            return 0;
        }
        if (totalCount <= 0) {
            return 0;
        }
        return totalCount / pageSize + 1;
    }

    public void setData(List<T> data) {
        this.addAll(data);
    }

    public List<T> getData() {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder("PageResponse{");
        buffer.append("pageNo=").append(pageNo);
        buffer.append(", pageSize=").append(pageSize);
        buffer.append(", totalCount=").append(totalCount);
        buffer.append(", totalPage=").append(getTotalPage());
        buffer.append(", modCount=").append(modCount);
        buffer.append('}');
        return buffer.toString();
    }
}