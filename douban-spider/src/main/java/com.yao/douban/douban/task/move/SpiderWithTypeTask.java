package com.yao.douban.douban.task.move;

import com.yao.douban.douban.constants.DBConstants;
import com.yao.douban.douban.task.AbstractTask;
import com.yao.douban.douban.task.DouBanInfoListPageTask;
import com.yao.douban.proxytool.entity.Page;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by user on 2018/2/8.
 */
public class SpiderWithTypeTask extends AbstractTask implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(SpiderWithTypeTask.class);
    private String typeName;
    private String typeValue;
    private int persentRecoder;
    private int startNumber;


    public SpiderWithTypeTask(String typeName, String typeValue, boolean isUseProxy) {
        this.typeName = typeName;
        this.typeValue = typeValue;
        super.isUseProxy = isUseProxy;
    }

    public void run() {
        try {
            //获取该标签的总条数
            for (int persent = 100; persent > 0; persent -= 10) {
                this.persentRecoder = persent;
                String url = String.format(DBConstants.MOVE_PERSENT_COUNT_URL, typeValue, persent, persent - 10);
                Page page = getPage(url);
//                System.out.println(page);
                if (page != null) {
                    JSONObject object = JSONObject.fromObject(page.getHtml());
                    if (object != null) {
                        int total = object.getInt("total");
                        for (int start = 0; start < total; start += 20) {
                            this.startNumber = start;
                            String listURL = String.format(DBConstants.MOVE_TOP_LIST_URL, typeValue, persent, persent - 10, start);
                            doubanHttpClient.getDownLoadMoveListExector().execute(new DouBanInfoListPageTask(listURL, true));
                            Thread.sleep(1000);
                        }
                    }
                    System.out.println(page.getHtml());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void retry() {
        logger.info("重试次数=" + retryTimes + "--开始编号：" + startNumber + "---重试代理：" + currentProxy.getProxyStr() + "---代理失败/成功次数：" + currentProxy.getFailureTimes()+ "/" + currentProxy.getSuccessfulTimes());
        doubanHttpClient.getDownLoadMoveListExector().execute(new SpiderWithTypeTask(typeName, typeValue, true));
    }

    public void handle(Page page){

    }
}
