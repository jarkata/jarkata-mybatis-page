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

    /**
     * 请求对象
     *
     * @param pageNo   当前页码
     * @param pageSize 每页查询数据大小
     */
    public PageRequest(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    /**
     * 获取页码
     *
     * @return 页码
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 每页数据集合大小
     *
     * @return 每页显示数据大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 开始数据偏移量
     *
     * @return 开始数据偏移量
     */
    @Override
    public int getOffset() {
        if (pageNo <= 0) {
            return 0;
        }
        return (pageNo - 1) * pageSize;
    }

    /**
     * 每页显示数据大小
     *
     * @return 每页显示数据大小
     */
    @Override
    public int getLimit() {
        return pageSize;
    }
}
