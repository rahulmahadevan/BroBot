import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.ParseException;

public class Bot extends TelegramLongPollingBot {
    private static long groupId = Utils.GROUPID;
    private static long adminId = Utils.ADMINID;

    @Override
    public String getBotUsername() {
        return Utils.BOTUSERNAME;
    }

    @Override
    public String getBotToken() {
        return Utils.BOTTOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageIN = update.getMessage().getText();
            String messageOUT = "";
            int messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();

            System.out.println("Message received from "+chatId);
            SendMessage sendMessage = new SendMessage();

            if(chatId == groupId){
                messageIN = messageIN.toLowerCase();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setReplyToMessageId(messageId);

                if(messageIN.equals("/info") || messageIN.equals("/info@CSISCRBot")){
                    messageOUT = "\uD83D\uDC7D Bro bot is a Maven Project using " +
                            "telegram bot API for Java and MongoDB as the data store \uD83D\uDCBB \n" +
                            "\n" +
                            "View project on GitHub : https://github.com/rahulmahadevan/CRBot";

                }else if(messageIN.equals("/notice") || messageIN.equals("/notice@CSISCRBot")){
                    messageOUT = Main.getLatestNotice();
                }else if(messageIN.charAt(0)== '/'){
                    messageOUT = "I'm still learning bro \uD83D\uDE1C";
                }
                sendMessage.setText(messageOUT);
            }else if(chatId == adminId){
                sendMessage.setChatId(String.valueOf(adminId));
                if(messageIN.equals("/format")){
                    messageOUT = "Title\nDescription\nDate\nTime (String)";
                    sendMessage.setText(messageOUT);
                }else {
                    /*
                    Data Mining Minor 1
                    Syllabus from topic 1 to todays lecture
                    25/01/2021
                    5:00PM
                     */
                    String[] data = messageIN.split("\n");
                    DBOps mongo = new DBOps();
                    try {
                        String announce = mongo.setAnnouncement(data);
                        sendMessage.setText("Noted! \uD83D\uDC4D");
                        if(!announce.equals("")){
                            sendReminder(announce);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mongo.DBClose();
                }
            }
            try{
                execute(sendMessage);
                System.out.println("Message: "+messageOUT+" ;Sent to: "+chatId+"; User ID: ");
            }catch (TelegramApiException e){
                e.printStackTrace();
            }

        }
    }

    public void sendLatestNotice(String latestNotice){
        SendMessage sendNotice = new SendMessage();
        sendNotice.setChatId(String.valueOf(groupId));
        sendNotice.setText(latestNotice);
        try{
            execute(sendNotice);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    public void sendReminder(String reminder){
        SendMessage sendReminder = new SendMessage();
        sendReminder.setChatId(String.valueOf(groupId));
        sendReminder.setText(reminder);
        try{
            execute(sendReminder);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

}