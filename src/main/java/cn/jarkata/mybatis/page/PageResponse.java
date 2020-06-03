package cn.jarkata.mybatis.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 分页响应对象
 *
 * @param <T> 范型对象
 */
public class PageResponse<T> extends ArrayList<T> {

    private final int pageNo;
    private final int pageSize;
    private long totalCount;
    private int totalPage;

    public PageResponse(PageRequest pageRequest) {
        this.pageNo = pageRequest.getPageNo();
        this.pageSize = pageRequest.getPageSize();
    }

    public PageResponse(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
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

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalPage() {
        if (totalCount <= 0) {
            return 0;
        }
        return totalCount / pageSize + 1;
    }


    public void setData(List<T> data) {
        this.addAll(data);
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer("PageResponse{");
        buffer.append("pageNo=").append(pageNo);
        buffer.append(", pageSize=").append(pageSize);
        buffer.append(", totalCount=").append(totalCount);
        buffer.append(", totalPage=").append(getTotalPage());
        buffer.append(", modCount=").append(modCount);
        buffer.append('}');
        return buffer.toString();
    }
}
