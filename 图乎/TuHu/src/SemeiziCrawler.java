
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
 





import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * @author Hao Tian
 *
 */
public class SemeiziCrawler {
	public static int i=1;
    private static final String BASEHOST = "https://www.douban.com/group/lvxing/discussion?start=0";
    private static DefaultHttpClient client = ConnectionManager.getHttpClient();
    static String url = "https://www.douban.com/group/lvxing/discussion?start=0";
    private static String IMGPATH = "D:\\图乎"+File.separator+StringUtil.getDate();
    static int STARTPAGE = 1;
    static int PAGECOUNT = 20;
 
    public static void main(String[] args) {
        File f = new File(IMGPATH);
        if(!f.exists()){
            f.mkdirs();
        }
        String host = BASEHOST ;
        for(int i=STARTPAGE;i<PAGECOUNT;i++){
            if(i != 1){
                host = "https://www.douban.com/group/lvxing/discussion?start="+(i-1)*25;
               // System.out.println(host);
            }
            System.out.println("进入第"+i+"页");
            String pageContext = getResultByUrl(host);
            //System.out.println(pageContext);
            List<String>articleURLS = getArticleURL(pageContext);
            //System.out.println(articleURLS);
            for(String articleURL:articleURLS){
            	//System.out.println(articleURL);
                String articleContext = getResultByUrl(articleURL);
                List<String> ImgURLS = getImgURLS(articleContext);
                for(String ImgURL:ImgURLS){
                	System.out.println("图片地址"+ImgURL);
                	saveImageToDisk(ImgURL);
                }
            }
        }
//      String articleContext = getResultByUrl(url);
//      List<String> strs = getImgURLS(articleContext);
//      for(String str:strs){
//          System.out.println(str);
//      }
    }
    /**
     * 根据url获取页面
     * @param url
     * @return
     */
    public static String getResultByUrl(String url){
        System.out.println("打开网页"+url);
        HttpGet get = new HttpGet(url);
        HttpEntity entity = null;
        HttpResponse response = null;
        try {
            response = client.execute(get);
            entity = response.getEntity();
            if(entity != null){
                InputStream is = entity.getContent();
                StringWriter sw = new StringWriter();
                IOUtils.copy(is, sw, "UTF-8");
                is.close();
                sw.close();
                return sw.toString();
            }
        } catch (Exception e) {
            System.out.println("网页打开出错");
            return null;
        }finally{
            get.abort();
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 找出当前页面中所有帖子的地址
     * @param pageStr  网页字符串
     * @return
     */
    public static List<String> getArticleURL(String pageContext){
        if(pageContext == null){
            return null;
        }
        List<String> articleURLS = new ArrayList<String>();
        System.out.println("寻找帖子...........");
        try {
            Document doc = Jsoup.parseBodyFragment(pageContext);
            //Elements es = doc.select("div"); 
            Elements es = doc.select("table[class=olt]").select("tbody").select("tr"); 
            es = es.select("td[class=title]");
            es = es.select("a");
        	//String relHref = es.attr("href"); // == "/"
        	//String absHref = es.attr("abs:href"); // "http://www.open-open.com/"
           // es = es.select("div.meta a:containsOwn(全文)");
            
            for(Element e:es){
            	//String text=e.text();
            	articleURLS.add(e.attr("abs:href"));
               // articleURLS.add(e.attr("href"));
            	//System.out.println(absHref);
               // System.out.println(articleURL);
               // System.out.println("href");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return articleURLS;
    }
    /**
     * 获取帖子的图片地址
     * @param articleURLS
     * @return
     */
    public static List<String> getImgURLS(String articleContext){
        List<String>ImgURLS = new ArrayList<String>();
        if(articleContext == null){
            return null;
        }
        System.out.println("获取图片地址-----------");
        Document doc = Jsoup.parse(articleContext);
        //System.out.println(articleContext);
        Elements es = doc.select("div[class=topic-content]").select("div[class=topic-figure cc]").select("img");
        System.out.println(es.text());
         for(Iterator<Element> i=es.iterator();i.hasNext();){
                Element e = i.next();
                //System.out.println(e.attr("src"));
                ImgURLS.add(e.attr("src"));
             }
        return ImgURLS;
    }
    /**
     * 保存图片
     * @param ImgURL
     */
    public static InputStream getInputStream(String url1) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
          URL url = new URL(url1);
          httpURLConnection = (HttpURLConnection) url.openConnection();
          // 设置网络连接超时时间
          httpURLConnection.setConnectTimeout(3000);
          // 设置应用程序要从网络连接读取数据
          httpURLConnection.setDoInput(true);

          httpURLConnection.setRequestMethod("GET");
          int responseCode = httpURLConnection.getResponseCode();
          if (responseCode == 200) {
            // 从服务器返回一个输入流
            inputStream = httpURLConnection.getInputStream();

          }

        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        return inputStream;

      }
    public static void saveImageToDisk(String url) {

        InputStream inputStream = getInputStream(url);
        byte[] data = new byte[1024];
        int len = 0;
        FileOutputStream fileOutputStream = null;
        try {
          String fileName = String.valueOf(i);
          
          String savepath =	IMGPATH+File.separator+fileName+".jpg";
          i++;
          fileOutputStream = new FileOutputStream(savepath);
          while ((len = inputStream.read(data)) != -1) {
            fileOutputStream.write(data, 0, len);

          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {

          if (inputStream != null) {
            try {
              inputStream.close();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
          if (fileOutputStream != null) {
            try {
              fileOutputStream.close();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }

        }

      }
}
