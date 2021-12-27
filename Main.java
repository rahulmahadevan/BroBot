import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;


public class Main {
    private static String url = Utils.URL;
    private static String latestNotice = "";
    private static String startTime = Utils.STARTTIME;
    private static String endTime = Utils.ENDTIME;

    public static void main(String args[]) {
        Bot bot = new Bot();
        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }

        DBOps db = new DBOps();
        latestNotice = db.getNoticeFromDB();
        db.DBClose();

        Thread noticeChecker = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        DBOps mongo = new DBOps();
                        LocalTime time = LocalTime.now();
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTime(new Date());
//                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        if(time.isAfter(LocalTime.parse(startTime)) && time.isBefore(LocalTime.parse(endTime))) {
                            //NOTICE
                            System.out.println("Thread Active, checking for events");
                            String checkLatestNotice = getLatestNotice();
                            if (!latestNotice.equals(checkLatestNotice)) {
                                latestNotice = checkLatestNotice;
                                mongo.setNoticeInDB(latestNotice);
                                bot.sendLatestNotice(latestNotice);
                            }

                            //Class Link

                            //Announcements

                            //Reminders
                            ArrayList<String> announcements = (ArrayList<String>) mongo.getAnnouncements();
                            for(int i=0; i<announcements.size();i++){
                                bot.sendReminder(announcements.get(i));
                            }
                        }
                        Thread.sleep(1000*60*60);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        };
        noticeChecker.start();
    }

    public static String getLatestNotice(){
        String link= "", text="";
        try {
            Document doc = Jsoup.connect(url).get();
            Element latestNotice = doc.getElementsByClass("demo1").first().child(0);
            text = latestNotice.text();
            link = latestNotice.child(0).attr("href");
            if(link.charAt(0) == '/'){
                link = "https://www.nitw.ac.in" + link;
            }
        }catch (HttpStatusException e){
            System.out.println("Http Status Exception while loading "+url);
        }catch (IOException e){
            System.out.println("IO Exception while loading "+url);
        }
        String response = "Notice Announcement:\uD83D\uDCE8 \n\n"+ text + "\n\nLink: " + link;
        System.out.println("Fetched latest notice");
        return response;
    }
}
